package com.monash.analytics.audio.services.microphone;

import com.monash.analytics.audio.services.enums.MicrophoneState;
import com.monash.analytics.audio.services.exceptions.TDLUnavailableException;
import com.monash.analytics.audio.services.utils.Display;
import com.monash.analytics.audio.services.utils.Constants;

import javax.sound.sampled.*;
import java.io.*;

/***************************************************************************
 * Microphone class that contains methods to capture audio from audio.microphone
 *
 * @author originals: Luke Kuza, Aaron Gokaslan
 * @author Riordan Alfredo (riordan.alfredo@gmail.com)
 **************************************************************************/
public class Microphone implements Closeable, Listenable {

	/**
	 * TargetDataLine variable to receive data from audio.microphone
	 */
	private TargetDataLine targetDataLine;

	/**
	 * Variable for enum
	 */
	private MicrophoneState state;

	/**
	 * Microphone name
	 */
	private final String micName;


	/**
	 * Channel manager that contains everything for channel management
	 */
	private final ChannelManager channelManager;


	/**
	 * Constructor
	 * set the state to close and initialise target data line with a audio.mixer
	 */
	public Microphone(String micName, Mixer mixer) {
		this.state = MicrophoneState.CLOSED;
		this.micName = micName;
		this.channelManager = new ChannelManagerWithSpeaker(this);
		this.initTargetDataLineFromMixer(mixer); // when audio.microphone is created, we will initialise its tdl
	}

	/**
	 * Constructor
	 * without audio.mixer object
	 */
	public Microphone(String micName) {
		this.state = MicrophoneState.CLOSED;
		this.micName = micName;
		this.channelManager = new ChannelManagerWithSpeaker(this);
		this.initTargetDataLine(); // when audio.microphone is created, we will initialise its tdl
	}

	/**
	 * Deep Copy constructor
	 */
	public Microphone(Microphone oldMicrophone) {
		this.micName = oldMicrophone.micName;
		this.state = oldMicrophone.state;
		this.targetDataLine = oldMicrophone.targetDataLine;
		this.channelManager = oldMicrophone.channelManager;
	}

	/**
	 * Gets the current state of Microphone
	 *
	 * @return TRANSCRIBE_AUDIO is returned when the Thread is collecting Audio data
	 *         and sending data to SpeechToText API<br>
	 *         STARTING_CAPTURE is returned if the Thread is setting variables<br>
	 *         OPENED is returned if the target data line is opened for status checking <br>
	 *         CLOSED is returned if the Thread is not doing anything/not capturing
	 *         audio
	 */
	public MicrophoneState getState() {
		return state;
	}

	/**
	 * Target Data Line
	 * @return target data line getter
	 */
	public TargetDataLine getTargetDataLine() {
		return targetDataLine;
	}


	/**
	 * Getter of audio.microphone name
	 * @return String audio.microphone name
	 */
	public String getMicName() {
		return micName;
	}

	/**
	 * Initializes the target data line.
	 */
	public void initTargetDataLineFromMixer(Mixer mixer) {
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
		try {
			targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);// TODO: add a checking to test the audio format
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Initializes the target data line.
	 */
	public void initTargetDataLine() {
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);// TODO: add a checking to test the audio format
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Default audio format
	 * 16kHz, 16-bit sampleSize, mono channel, signed, little endian
	 * @return AudioFormat
	 */
	public AudioFormat getAudioFormat() {
		int channels = channelManager.getMaxNumOfChannel(); // 1 = mono (default), 2 = stereo, or more...
		return new AudioFormat(Constants.SAMPLE_RATE, Constants.BIT_DEPTH, channels, true, false);
	}

	/**
	 * Opens the audio.microphone.
	 * If it's already open, it does nothing.
	 */
	public void open() throws TDLUnavailableException {
		if (getTargetDataLine() == null) { throw new TDLUnavailableException(); }
		if (!getTargetDataLine().isOpen() && !getTargetDataLine().isRunning() && !getTargetDataLine().isActive()) {
			try {
				this.state = MicrophoneState.OPENED;
				getTargetDataLine().open(getAudioFormat());
				getTargetDataLine().start();

				Display.println(micName + ": has opened.");
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Close the audio.microphone capture, saving all processed audio to the specified
	 * file.<br>
	 * If already closed, this does nothing
	 */
	@Override
	public void close() {
		if (!this.state.equals(MicrophoneState.CLOSED)) {
			getTargetDataLine().stop();
			getTargetDataLine().close();
			this.state = MicrophoneState.CLOSED;
			Display.println(micName + ": stops listening & closed.");
		}
	}

	/**
	 * A complementary method of decorator pattern.
	 * It starts collecting & storing data into corresponding channel
	 * as ByteArrayOutputSystem
	 */
	@Override
	public void listen() {
		if(this.state.equals(MicrophoneState.CLOSED)) {
			this.state = MicrophoneState.LISTENING;

			// Run the thread
			new Thread(new MicBuffer(this)).start();
		}
	}

	public void save(){
		channelManager.saveAllChannelAudio(this.getAudioFormat());
	}

	/**
	 * Return a channel manager object to achieve Single-responsibility
	 * @return ChannelManager object (mutable)
	 */
	public ChannelManager getChannelManager(){
		return channelManager;
	}
}
