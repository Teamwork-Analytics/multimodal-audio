package com.monash.analytics.audio.controllers;

import com.monash.analytics.audio.services.LinuxAudioServiceAPI;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.Assert.assertTrue;


public class AudioServiceTest {

    @MockBean
    private LinuxAudioServiceAPI linuxAudioServiceAPI;

    @Test
    public void audioFileIsCreatedWhenRecording(){

    }

    @Test
    public void pauseRecording(){

    }

}
