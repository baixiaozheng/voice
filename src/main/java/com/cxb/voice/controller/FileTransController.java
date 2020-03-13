package com.cxb.voice.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.cxb.voice.enums.Status;
import com.cxb.voice.model.TransResult;
import com.cxb.voice.model.Voice;
import com.cxb.voice.service.VoiceService;
import com.cxb.voice.util.FileTransUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author baixiaozheng
 */
@Controller
public class FileTransController {

    @Autowired
    private VoiceService voiceService;

    // Endpoint以北京为例，其它Region请按实际情况填写。
    String endpoint = "http://oss-cn-beijing.aliyuncs.com";
    // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
    String accessKeyId = "your accessKeyId";
    String accessKeySecret = "your accessKeySecret";
    String bucketName = "your bucketName";
    String appKey = "your appKey";

    @RequestMapping("toIndex")
    public String toIndex(){
        return "index";
    }

    /**
     * 上传文件
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("upload")
    @ResponseBody
    public JSONObject upload(HttpServletRequest request) throws Exception {

        JSONObject json = new JSONObject();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        // 获取上传的文件
        MultipartFile multiFile = multipartRequest.getFile("file");
        //获得原始文件名;
        String fileRealName = multiFile.getOriginalFilename();

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        //把上传的MultipartFile转换成File
        File file = multipartFileToFile(multiFile);

        // 创建PutObjectRequest对象。
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileRealName, file);

        // 上传文件。
        PutObjectResult result = ossClient.putObject(putObjectRequest);

        String url = getUrl(fileRealName);

        // 关闭OSSClient。
        ossClient.shutdown();

        json.put("url", url);

        FileTransUtil fileTransUtil = new FileTransUtil(accessKeyId, accessKeySecret);
        String taskId = fileTransUtil.submitFileTransRequest(appKey, url);

        deleteTempFile(file);

        Voice voice = new Voice();
        voice.setFileName(fileRealName);
        voice.setTaskId(taskId);
        voice.setUrl(url);
        voice.setStatus(Status.WAITING);
        voiceService.save(voice);
        return json;
    }

    @GetMapping("result")
    @ResponseBody
    public JSONObject getResult(String taskId){

        JSONObject json = new JSONObject();
        String result = "";
        Voice voice = voiceService.getByTaskId(taskId);
        if(StringUtils.isEmpty(voice.getResult())){
            if(Status.PROGRESS.equals(voice.getStatus())){
                result = "转换中，请稍后再试";
                List<TransResult> list = new ArrayList<>();
                TransResult r = new TransResult();
                r.setResult(result);
                list.add(r);
                json.put("date",list);
                json.put("code",0);
                return json;
            }
            else {
                try {
                    voice.setStatus(Status.PROGRESS);
                    voiceService.save(voice);

                    FileTransUtil fileTransUtil = new FileTransUtil(accessKeyId, accessKeySecret);
                    result = fileTransUtil.getFileTransResult(taskId);
                    System.out.println(result);
                    voice.setStatus(Status.COMPLETE);
                    voice.setResult(result);
                    voiceService.save(voice);
                } catch (Exception e) {
                    voice.setStatus(Status.WAITING);
                    voiceService.save(voice);
                }

            }
        } else {
            result = voice.getResult();
        }

        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONArray sentences = jsonObject.getJSONArray("Sentences");
        List<TransResult> list = new ArrayList<>();
        for(int i=0;i<sentences.size();i++){
            JSONObject s = sentences.getJSONObject(i);
            TransResult r = new TransResult();
            r.setStartTime(timeStamp2Date(s.getLong("BeginTime")));
            r.setEndTime(timeStamp2Date(s.getLong("EndTime")));
            r.setResult(s.getString("Text"));
            list.add(r);
        }
        json.put("data",list);
        json.put("code",0);
        return json;


    }

    /**
     * 时间多8小时，减掉处理
     * @param timestamp
     * @return
     */
    public String timeStamp2Date(Long timestamp){
        String date = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(timestamp-8*60*60*1000));
        return date;
    }

    @RequestMapping("toList")
    public String toList(){
        return "list";
    }

    @GetMapping("list")
    @ResponseBody
    public JSONObject list(){
        List<Voice> list = voiceService.findAll();
        JSONObject json = new JSONObject();
        json.put("code",0);
        json.put("data",list);
        return json;
    }

    /**
     * MultipartFile 转 File
     *
     * @param file
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile file) throws Exception {

        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            file = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }

    /**
     * 获取流文件
     *
     * @param ins
     * @param file
     */
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除本地临时文件
     *
     * @param file
     */
    public static void deleteTempFile(File file) {
        if (file != null) {
            File del = new File(file.toURI());
            del.delete();
        }

    }

    /**
     * 获得url链接
     *
     * @param key
     * @return
     */
    public String getUrl(String key) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 设置URL过期时间为10年  3600l* 1000*24*365*10
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365);
        // 生成URL
        URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);
        if (url != null) {
            return url.toString();
        }
        return null;
    }
}
