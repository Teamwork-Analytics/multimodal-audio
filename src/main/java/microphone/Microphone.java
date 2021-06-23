package microphone;

import api.google_speech.SpeechToTextAPI;
import writers.AudioMicFileWriter;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static microphone.Microphone.CaptureState.CLOSED;
import static microphone.Microphone.CaptureState.STARTING_CAPTURE;

/***************************************************************************
 * Microphone class that contains methods to capture audio from microphone
 *
 * @author Luke Kuza, Aaron Gokaslan
 * @author Riordan Alfredo ()
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
	 * Microphone name
	 */
	private final String micName;

	/**
	 * selected flag
	 */
	private int numberOfChannels = 1;

	/**
	 * Constructor
	 *
	 * @param fileType File type to save the audio in<br>
	 *                 Example, to save as WAVE use AudioFileFormat.Type.WAVE
	 */
	public Microphone(AudioFileFormat.Type fileType, String micName) {
		setState(CaptureState.CLOSED);
		setFileType(fileType);
		this.micName = micName;
		if(micName.equals( "US-16x08")){
			numberOfChannels = 16;
		}
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
			TargetDataLine tdl = (TargetDataLine) mixer.getLine(dataLineInfo);
			// TODO: add a checking to test the audio format
			setTargetDataLine(tdl);

		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
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
		int channels = 16; // use 16 channels, change it to 1 if mic doesn't support
		// 1,2
		boolean signed = true;
		// true,false
		boolean bigEndian = false;
		// true,false
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	public AudioFormat getAudioFormat(int numChannels) {
		float sampleRate = 16000;
		// 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16;
		// 8,16
		int channels = numChannels; // use 16 channels, change it to 1 if mic doesn't support
		// 1,2
		boolean signed = true;
		// true,false
		boolean bigEndian = false;
		// true,false
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	public void transcribeSpeechToText(SpeechToTextAPI s2t){
		setState(CaptureState.STARTING_CAPTURE);
		new Thread(() -> {
			AudioInputStream audio = new AudioInputStream(getTargetDataLine());
			long startTime = System.currentTimeMillis();
			try {
				while (state.equals(STARTING_CAPTURE)) {
					long estimatedTime = System.currentTimeMillis() - startTime;
					byte[] data = new byte[8 * 1024];
					int numberOfBytes = audio.read(data);
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
	 * @param sessionName the session name, retrieved from the Main dashboard
	 * @throws LineUnavailableException
	 */
	public void captureAudioToFile(String sessionName, Mixer mixer) throws LineUnavailableException {
		processAudioToFile(new AudioMicFileWriter(sessionName, micName).getAudioFile(), mixer);
	}

	/**
	 * Captures audio from the microphone and saves it a file
	 *
	 * @param audioFile The File to save the audio to
	 */
	private void processAudioToFile(File audioFile, Mixer mixer) {
		setState(CaptureState.STARTING_CAPTURE);
		setAudioFile(audioFile);

		if (getTargetDataLine() == null) {
			initTargetDataLineFromMixer(mixer);
		}
		// Get Audio
		new Thread(new CaptureThread()).start();
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
				System.out.println(micName + ": listening...");
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
			System.out.println(micName + ": stops listening.");
		}
	}

//	/**
//	 * Thread to capture the audio from the microphone and save it to a file
//	 */
//	private class CaptureThread implements Runnable {
//
//		/**
//		 * Run method for thread
//		 */
//		public void run() {
//			try {
//				AudioFileFormat.Type fileType = getFileType();
//				File audioFile = getAudioFile();
//				open();
//				AudioInputStream audioInputStream = new AudioInputStream(getTargetDataLine());
//				System.out.println(audioInputStream);
//				AudioSystem.write(audioInputStream, fileType, audioFile);
//				System.out.println(micName + ": Writing audio at " + getAudioFile().getAbsolutePath());
//				// Will write to File until it's closed.
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//	}


	/**
	 * Thread to capture the audio from the microphone and save it to a file
	 */
	private class CaptureThread implements Runnable {

		/**
		 * Run method for thread
		 */
		public void run() {
			ArrayList<ByteArrayOutputStream> baos = new ArrayList<>();

			try {
				open();
				int totalFramesRead = 0;
				AudioInputStream audio = new AudioInputStream(getTargetDataLine());
				AudioFormat audioFormat = getAudioFormat();
				int bytesPerFrame = audio.getFormat().getFrameSize();
				// Set an arbitrary buffer size of 1024 frames.
				int numBytes = 1024 * bytesPerFrame;
				byte[] audioBytes = new byte[numBytes];

				for(int i = 0; i < numberOfChannels; i++){
					baos.add(new ByteArrayOutputStream());
				}
				int numBytesRead = 0;
				int numFramesRead = 0;
				// Try to read numBytes bytes from the microphone.
				while ((numBytesRead = audio.read(audioBytes)) != -1 & !state.equals(CLOSED)) {
					// Calculate the number of frames actually read.
					numFramesRead = numBytesRead / bytesPerFrame;
					totalFramesRead += numFramesRead;
					// FIXME: check multiple channel
					if (numberOfChannels > 1) {
						ChannelDivider channelDivider = new ChannelDivider(audioBytes, numberOfChannels);
						ArrayList<byte[]> channelBytes = channelDivider.extract16BitsSingleChannels();
						for(int i = 0; i < channelBytes.size(); i++){
							baos.get(i).write(channelBytes.get(i),0,channelBytes.get(i).length);
						}
					}
				}
				AudioFileFormat.Type targetFileType = getFileType();
				AudioFormat outFormat = new AudioFormat(audioFormat.getEncoding(), audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits(), 1, audioFormat.getFrameSize() / 16, audioFormat.getFrameRate(), audioFormat.isBigEndian());

				for(int i = 0; i < numberOfChannels; i++){
					byte[] byteData = baos.get(i).toByteArray();
					File micTargetFile = new AudioMicFileWriter("TEST", micName+"_"+i).getAudioFile();
					ByteArrayInputStream micBais = new ByteArrayInputStream(byteData);
					AudioInputStream outputAIS = new AudioInputStream(micBais, outFormat, byteData.length / outFormat.getFrameSize());
					AudioSystem.write(outputAIS, targetFileType, micTargetFile);
				}

			}	catch (Exception e) {
				// Handle the error...
				e.printStackTrace();
			}

			System.out.println("END OF CONVERSATION!");
		}

	}

}
