package com.monash.analytics.audio.controller;

import com.monash.analytics.audio.service.AudioServiceAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000"}) // TODO: make sure that this IP address is main PC's IP
@RequestMapping(value = "/audio")
public class AudioWebController {
    /**
     * AudioService API
     */
    @Autowired
    private List<AudioServiceAPI> audioServices;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient eurekaClient;

//    @Autowired
//    public AudioWebController(AudioServiceAPI audioServiceAPI) {
//        this.audioServiceAPI = audioServiceAPI;
//    }

    private String staticSessionId;

    private AudioServiceAPI findService(String serviceName){
        for(AudioServiceAPI serviceAPI : audioServices){
            if(serviceAPI.getServiceName().equals(serviceName)){
                return serviceAPI;
            }
        }
        return audioServices.get(0);
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
            staticSessionId = sessionId;
            sendRequestToMainLaptop("baseline audio start");
            AudioServiceAPI audioServiceAPI = findService("simulation");
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
            AudioServiceAPI audioServiceAPI = findService("simulation");
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
            AudioServiceAPI audioServiceAPI = findService("simulation");
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
            sendRequestToMainLaptop("audio start");
            AudioServiceAPI audioServiceAPI = findService("simulation");
            audioServiceAPI.stopRecording();
            audioServiceAPI.startRecording(sessionId, "simulation");

        } catch (Exception e) {
            e.printStackTrace();
            return "exception when starting the audio service:" + e.getMessage();
        }
        return "audio service starts recording...";
    }

    @RequestMapping(value = "/start-debrief/{sessionId}")
    public @ResponseBody
    String debriefRecording(@PathVariable String sessionId) {
        try {
            AudioServiceAPI audioServiceAPI = findService("simulation");
            audioServiceAPI.startRecording(sessionId, "debrief");
            sendRequestToMainLaptop("audio start");
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when starting the audio service:" + e.getMessage();
        }
        return "audio service starts recording...";
    }

    @RequestMapping(value = "/baseline-stop")
    public @ResponseBody
    String baselinePause() {
        try {
            AudioServiceAPI audioServiceAPI = findService("simulation");
            audioServiceAPI.stopRecording();
            sendRequestToMainLaptop("baseline audio stop");
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when stopping/pausing the audio service: " + e.getMessage();
        }
        return "audio has paused";
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
            AudioServiceAPI audioServiceAPI = findService("simulation");
            audioServiceAPI.stopRecording();
            sendRequestToMainLaptop("audio stop");
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when stopping/pausing the audio service: " + e.getMessage();
        }
        return "audio has paused";
    }


    /**
     * pause the recording & save data
     *
     * @return string of success/fail message
     */
    @RequestMapping(value = "/debrief-stop")
    public @ResponseBody
    String debriefPause() {
        try {
            AudioServiceAPI audioServiceAPI = findService("simulation");
            audioServiceAPI.stopRecording(0, "TEACHER"); // save only the first channel.
            sendRequestToMainLaptop("audio stop");
        } catch (Exception e) {
            e.printStackTrace();
            return "exception when stopping/pausing the audio service: " + e.getMessage();
        }
        return "audio has paused";
    }


    private void sendRequestToMainLaptop(String message) {
        ServiceInstance choose = eurekaClient.choose("position-service");
        String hostname = choose.getHost();
        int port = choose.getPort();
        String uri = "/audio/start-time/" + staticSessionId + "/" + message;

        String url = "http://" + hostname + ":" + port + uri;
        String returnMessage = restTemplate.getForObject(url, String.class);
        System.out.println("main laptop service return-----" + returnMessage);
    }
}
