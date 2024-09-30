package com.monash.analytics.audio.services.enums;

/**
 * Microphone state audio.enums
 */
public enum MicrophoneState {
    LISTENING, // it listens() & transcribes text.
    SAVE_AUDIO, // the audio.microphone is saving its audio to files.
    OPENED, // The audio.microphone is opened.
    CLOSED; // The audio.microphone is closed.
}
