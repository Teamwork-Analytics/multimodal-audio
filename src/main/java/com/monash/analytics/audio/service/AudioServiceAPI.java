package com.monash.analytics.audio.service;

import static com.monash.analytics.audio.service.utils.Constants.SESSION_TIME;
import static com.monash.analytics.audio.service.utils.Constants.SESSION_TYPE;

public interface AudioServiceAPI {

    /**
     * Selecting channels. Can be combined with the initialiser
     *
     * @param channelNumber integer index in a list: 0 is channel 1, and 1 is channel 2, and so on
     * @param channelName   a String custom channel name if not provided
     */
    void selectAChannel(int channelNumber, String channelName) throws Exception;

    /**
     * Start the recording
     *
     * @throws Exception
     */
    void startRecording(String sessionId, String sessionType) throws Exception;

    /**
     * Pause recording
     *
     * @throws Exception
     */
    void stopRecording() throws Exception;

    /**
     * Completely stop the audio service (mixer).
     *
     * @throws Exception error message.
     */
    void shutdown() throws Exception;

    default String timestamp(String status) {
        return String.format("%s audio %s at %s", SESSION_TYPE, status, SESSION_TIME);
    }

    ;
}
