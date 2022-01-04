package com.monash.analytics.audio.service.features.speech.google_api;

import com.monash.analytics.audio.service.enums.MicrophoneState;
import com.monash.analytics.audio.service.exceptions.MicException;
import com.monash.analytics.audio.service.features.speech.SpeechToTextAPI;
import com.monash.analytics.audio.service.features.speech.TextTranscript;
import com.monash.analytics.audio.service.microphone.Microphone;
import com.monash.analytics.audio.service.microphone.channels.Channel;
import com.monash.analytics.audio.service.observers.TranscriptObservable;
import com.monash.analytics.audio.service.observers.TranscriptObserver;
import com.monash.analytics.audio.service.utils.Display;
import com.google.cloud.speech.v1.SpeechClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A concrete class from SpeechToTextAPI with Google Speech API
 * (a Facade pattern)
 */
public class GoogleSpeechAPI implements SpeechToTextAPI {

    /**
     * audio.microphone object
     */
    private final Microphone microphone;

    /**
     * Channel and observable (passed to the API)
     */
    private final Map<Integer, TranscriptObservable> channelObservable;

    /**
     * Channel & observer
     */
    private final Map<Integer, TranscriptObserver> channelObservers;

    /**
     * Constructor
     * @param microphone audio.microphone object from client
     */
    public GoogleSpeechAPI(Microphone microphone) throws MicException {
        this.microphone = microphone;
        channelObservable = new HashMap<>();
        channelObservers = new HashMap<>();

        // assign observable & audio.observers
        for(int channelId: microphone.getChannelManager().getSelectedChannelKeys()) {
            channelObservable.put(channelId, new TranscriptObservable());
            channelObservers.put(channelId, new TranscriptObserver(microphone.getChannelManager().getChannelName(channelId)));
        }
    }

    /**
     * Basically, it is the client of Google Speech API
     * TODO: need to work on the infinite streaming (auto-restart with empty data)
     */
    @Override
    public void startTranscription() {
        microphone.listen();

        // for each channel, spawn a new thread
        for(int channelId: channelObservable.keySet()){
            Channel channel = microphone.getChannelManager().getChannel(channelId);
            new Thread(new ChannelThread(channel)).start();
        }
    }

    /**
     * Get a combined transcriptions from available channels that are sorted by timestamp
     * @return a concatenated list that are sorted by timestamps
     */
    @Override
    public List<TextTranscript> getTranscriptions() {
        List<TextTranscript> allTranscripts = new ArrayList<>();

        channelObservers.forEach((k,v)->{
            allTranscripts.addAll(v.getTextTranscripts());
        });

        return allTranscripts.stream()
                .sorted(Comparator.comparing(TextTranscript::getTimestamp))
                .collect(Collectors.toList());
    }


    /**
     * A new channel thread to transcribes text
     */
    private class ChannelThread implements Runnable{

        /**
         * A channel object (passed on from mic - DIP)
         */
        private final Channel channel;

        /**
         * Constructor
         * @param channel the channel object
         */
        private ChannelThread(Channel channel){
            this.channel = channel;
        }

        @Override
        public void run() {
            // Set-up
            String channelName = channel.getChannelName();
            TranscriptObservable transcriptObservable = channelObservable.get(channel.getId());
            TranscriptObserver transcriptObserver = channelObservers.get(channel.getId());

            // Connect the observable & observer (1-on-1 at the moment)
            transcriptObservable.subscribe(transcriptObserver);

            // Create the new Google API
            GoogleS2TImplementation googleAPI = new GoogleS2TImplementation(channelName);

            // Wrap & run the transcription
            try (SpeechClient speechClient = SpeechClient.create()) {
                googleAPI.initConfig(speechClient, transcriptObservable);
                Display.println(channelName + " is transcribing with Google Speech-to-text API...");
                while(microphone.getState().equals(MicrophoneState.OPENED)){
                    byte[] audioData = channel.writeDataToBaos();
                    googleAPI.sendRequest(audioData);
                }
                googleAPI.onComplete();
                googleAPI.close();
            } catch (IOException | InterruptedException e) {
                Display.println("Error at channel thread.");
                e.printStackTrace();
            }
        }
    }


}
