package enums;

/**
 * Microphone state enums
 */
public enum MicrophoneState {
    LISTENING, // it listens() & transcribes text.
    SAVE_AUDIO, // the microphone is saving its audio to files.
    OPENED, // The microphone is opened.
    CLOSED; // The microphone is closed.
}
