package com.monash.analytics.audio.controller;

import com.monash.analytics.audio.service.AudioServiceAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(value = "/audio")
public class AudioWebController {
    /**
     * AudioService API
     */
    private final AudioServiceAPI audioServiceAPI;

    @Autowired
    public AudioWebController(AudioServiceAPI audioServiceAPI) {
        this.audioServiceAPI = audioServiceAPI;
    }

    /**
     * start the recording
     *
     * @return string of success/fail message
     */
    @RequestMapping(value = "/start-baseline/{sessionId}")
    public @ResponseBody
    String baselineRecording(@PathVariable String sessionId) {
        try {
            audioServiceAPI.startRecording(sessionId, "baseline");
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when starting the audio service:" + e.getMessage();
        }
        return "audio service starts recording...";
    }

    /**
     * Return start timestamp for the main server
     *
     * @param sessionId
     * @return
     */
    @RequestMapping(value = "/start-time/{sessionId}")
    public @ResponseBody
    String startTimestamp(@PathVariable String sessionId) {
        try {
            return audioServiceAPI.timestamp("starts");
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when starting the audio service:" + e.getMessage();
        }
    }

    /**
     * Start time
     *
     * @param sessionId
     * @return
     */
    @RequestMapping(value = "/stop-time/{sessionId}")
    public @ResponseBody
    String stopTimestamp(@PathVariable String sessionId) {
        try {
            return audioServiceAPI.timestamp("stops");
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when starting the audio service:" + e.getMessage();
        }
    }

    /**
     * start the recording
     *
     * @return string of success/fail message
     */
    @RequestMapping(value = "/start/{sessionId}")
    public @ResponseBody
    String recording(@PathVariable String sessionId) {
        try {
            audioServiceAPI.stopRecording();
            audioServiceAPI.startRecording(sessionId, "simulation");
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when starting the audio service:" + e.getMessage();
        }
        return "audio service starts recording...";
    }

    /**
     * pause the recording & save data
     *
     * @return string of success/fail message
     */
    @RequestMapping(value = "/stop")
    public @ResponseBody
    String pause() {
        try {
            audioServiceAPI.stopRecording();
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when stopping/pausing the audio service: " + e.getMessage();
        }
        return "audio has paused";
    }
}
