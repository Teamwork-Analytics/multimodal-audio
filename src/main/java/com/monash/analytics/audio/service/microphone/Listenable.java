package com.monash.analytics.audio.service.microphone;

public interface Listenable {
    /**
     * A complementary method of decorator pattern.
     * It starts collecting & storing data into corresponding channel
     * as ByteArrayOutputSystem
     */
    void listen();

    /**
     * Close the audio.microphone
     */
    void close();
}
