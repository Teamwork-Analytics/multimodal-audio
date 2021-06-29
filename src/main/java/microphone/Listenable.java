package microphone;

public interface Listenable {
    /**
     * A complementary method of decorator pattern.
     * It starts collecting & storing data into corresponding channel
     * as ByteArrayOutputSystem
     */
    void listen();

    /**
     * Close the microphone
     */
    void close();
}
