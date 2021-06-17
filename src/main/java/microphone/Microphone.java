package microphone;

import google_api.SpeechToTextAPI;

import javax.sound.sampled.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static microphone.Microphone.CaptureState.STARTING_CAPTURE;

/***************************************************************************
 * Microphone class that contains methods to capture audio from microphone
 *
 * @author Luke Kuza, Aaron Gokaslan
 ***************************************************************************/
public class Microphone implements Closeable {

	/**
	 * TargetDataLine variable to receive data from microphone
	 */
	private TargetDataLine targetDataLine;

	/**
	 * Enum for current Microphone state
	 */
	public enum CaptureState {
		PROCESSING_AUDIO, STARTING_CAPTURE, CLOSED;
	}

	/**
	 * Variable for enum
	 */
	private CaptureState state;

	/**
	 * Variable for the audios saved file type
	 */
	private AudioFileFormat.Type fileType;

	/**
	 * Variable that holds the saved audio file
	 */
	private File audioFile;

	/**
	 * Constructor
	 *
	 * @param fileType File type to save the audio in<br>
	 *                 Example, to save as WAVE use AudioFileFormat.Type.WAVE
	 */
	public Microphone(AudioFileFormat.Type fileType) {
		setState(CaptureState.CLOSED);
		setFileType(fileType);
		// initTargetDataLine();
	}

	/**
	 * Gets the current state of Microphone
	 *
	 * @return PROCESSING_AUDIO is returned when the Thread is recording Audio
	 *         and/or saving it to a file<br>
	 *         STARTING_CAPTURE is returned if the Thread is setting variables<br>
	 *         CLOSED is returned if the Thread is not doing anything/not capturing
	 *         audio
	 */
	public CaptureState getState() {
		return state;
	}

	/**
	 * Sets the current state of Microphone
	 *
	 * @param state State from enum
	 */
	private void setState(CaptureState state) {
		this.state = state;
	}

	public File getAudioFile() {
		return audioFile;
	}

	public void setAudioFile(File audioFile) {
		this.audioFile = audioFile;
	}

	public AudioFileFormat.Type getFileType() {
		return fileType;
	}

	public void setFileType(AudioFileFormat.Type fileType) {
		this.fileType = fileType;
	}

	public TargetDataLine getTargetDataLine() {
		return targetDataLine;
	}

	public void setTargetDataLine(TargetDataLine targetDataLine) {
		this.targetDataLine = targetDataLine;
	}

	/**
	 * Initializes the target data line.
	 */
	void initTargetDataLine() {
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
		try {
			setTargetDataLine((TargetDataLine) AudioSystem.getLine(dataLineInfo));
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the target data line.
	 */
	public void initTargetDataLineFromMixer(Mixer mixer) {
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
		try {
			setTargetDataLine((TargetDataLine) mixer.getLine(dataLineInfo));
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void transcribeSpeechToText(SpeechToTextAPI s2t){
		setState(CaptureState.STARTING_CAPTURE);
		new Thread(() -> {
			AudioInputStream audio = new AudioInputStream(getTargetDataLine());
			long startTime = System.currentTimeMillis();
			try {
				while (state.equals(STARTING_CAPTURE)) {
					long estimatedTime = System.currentTimeMillis() - startTime;
					byte[] data = new byte[6400];
					audio.read(data);
					s2t.sendRequest(data); // send to Google API
				}
//              AudioSystem.write(audio, AudioFileFormat.Type.WAVE, audioFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Captures audio from the microphone and saves it a file
	 *
	 * @param audioFileName The fully path (String) to a file you want to save the audio
	 *                  in
	 * @throws LineUnavailableException
	 */
	public void captureAudioToFile(String audioFileName) throws LineUnavailableException {
		File file = new File(audioFileName);
		processAudioToFile(file);
	}

	/**
	 * Captures audio from the microphone and saves it a file
	 *
	 * @param audioFile The File to save the audio to
	 * @throws LineUnavailableException
	 */
	private void processAudioToFile(File audioFile) throws LineUnavailableException {
		setState(CaptureState.STARTING_CAPTURE);
		setAudioFile(audioFile);

		if (getTargetDataLine() == null) {
			initTargetDataLine();
		}
		// Get Audio
		new Thread(new CaptureThread()).start();
	}

	/**
	 * The audio format to save in
	 *
	 * @return Returns AudioFormat to be used later when capturing audio from
	 *         microphone
	 */
	public AudioFormat getAudioFormat() {
		float sampleRate = 16000;
		// 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		// 8,16
		int channels = 1;
		// 1,2
		boolean signed = true;
		// true,false
		boolean bigEndian = false;
		// true,false
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	/**
	 * Opens the microphone, starting the targetDataLine. If it's already open, it
	 * does nothing.
	 */
	public void open() {
		if (getTargetDataLine() == null) {
			initTargetDataLine();
		}
		if (!getTargetDataLine().isOpen() && !getTargetDataLine().isRunning() && !getTargetDataLine().isActive()) {
			try {
				setState(CaptureState.PROCESSING_AUDIO);
				getTargetDataLine().open(getAudioFormat());
				getTargetDataLine().start();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Close the microphone capture, saving all processed audio to the specified
	 * file.<br>
	 * If already closed, this does nothing
	 */
	public void close() {
		if (getState() != CaptureState.CLOSED) {
			getTargetDataLine().stop();
			getTargetDataLine().close();
			setState(CaptureState.CLOSED);
		}
	}

	/**
	 * Thread to capture the audio from the microphone and save it to a file
	 */
	private class CaptureThread implements Runnable {

		/**
		 * Run method for thread
		 */
		public void run() {
			try {
				AudioFileFormat.Type fileType = getFileType();
				File audioFile = getAudioFile();
				open();
				AudioSystem.write(new AudioInputStream(getTargetDataLine()), fileType, audioFile);
				// Will write to File until it's closed.
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
