package microphone;

import net.sourceforge.javaflacencoder.FLACFileWriter;

import javax.sound.sampled.LineUnavailableException;

public class MicAvailabilityChecker {

	public static void validateMicAvailability() throws MicUnaccessibleException {
		Microphone mic = new Microphone(FLACFileWriter.FLAC, "");
		try {
			if (mic.getState() != Microphone.CaptureState.CLOSED) {
				throw new MicUnaccessibleException("Mic didn't successfully initialized");
			}
			mic.captureAudioToFile("test.mp3", null);
			if (mic.getState() != Microphone.CaptureState.PROCESSING_AUDIO || mic.getState() != Microphone.CaptureState.STARTING_CAPTURE) {
				throw new MicUnaccessibleException("Mic is in use and can't be accessed");
			}
			mic.close();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} finally {
			mic.close();
			mic = null;
		}
	}

	public static void main(String[] args) {
		try {
			MicAvailabilityChecker.validateMicAvailability();
		} catch (MicUnaccessibleException e) {
			e.printStackTrace();
		}
	}

}
