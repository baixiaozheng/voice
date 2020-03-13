package com.cxb.voice.service;

import com.cxb.voice.dao.VoiceDao;
import com.cxb.voice.model.Voice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VoiceServiceImpl implements VoiceService {

    @Autowired
    private VoiceDao voiceDao;

    @Override
    @Transactional
    public Voice save(Voice voice) {
        return voiceDao.save(voice);
    }

    @Override
    public List<Voice> findAll() {
        return voiceDao.findAll();
    }

    @Override
    public Voice getByTaskId(String taskId) {
        return voiceDao.getByTaskId(taskId);
    }
}
