package com.cxb.voice.service;

import com.cxb.voice.model.Voice;

import java.util.List;

public interface VoiceService {

    Voice save(Voice voice);

    List<Voice> findAll();

    Voice getByTaskId(String taskId);

}
