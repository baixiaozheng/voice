package com.cxb.voice.dao;

import com.cxb.voice.model.Voice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceDao extends JpaRepository<Voice,Integer> {

    Voice getByTaskId(String taskId);

    @Override
    List<Voice> findAll();

    @Override
    Voice save(Voice voice);
}
