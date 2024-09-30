package com.monash.analytics.audio.services;

import static com.monash.analytics.audio.services.utils.Constants.SESSION_TIME;
import static com.monash.analytics.audio.services.utils.Constants.SESSION_TYPE;

public interface AudioServiceAPI {

    /**
     * Selecting channels. Can be combined with the initialiser
     *
     * @param channelNumber integer index in a list: 0 is channel 1, and 1 is channel 2, and so on
     * @param channelName   a String custom channel name if not provided
     */
    void addChannel(int channelNumber, String channelName) throws Exception;

    /**
     * Start the recording
     *
     * @throws Exception
     */
    void startRecording(String sessionId, String sessionType) throws Exception;

    /**
     * Stop recording, save all
     *
     * @throws Exception
     */
    void stopRecording() throws Exception;

    /**
     * Stop recording, save one
     *
     * @throws Exception
     */
    void stopRecording(Integer channelIndex, String customName) throws Exception;

    /**
     * Completely stop the audio service (mixer).
     *
     * @throws Exception error message.
     */
    void shutdown() throws Exception;


    String getServiceName();

    default String timestamp(String status) {
        return String.format("%s audio %s at %s", SESSION_TYPE, status, SESSION_TIME);
    }


}
