package google_api.observers;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.Duration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static google_api.SpeechToTextInfinite.STREAMING_LIMIT;

public class S2TResponseObserverInfinite implements ResponseObserver<StreamingRecognizeResponse> {
    private final ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();
    private int resultEndTimeInMS = 0;
    private int restartCounter = 0;
    private double bridgingOffset = 0;
    private boolean lastTranscriptWasFinal = false;
    private int isFinalEndTime = 0;
    private int finalRequestEndTime = 0;
    public boolean newStream = true;

    @Override
    public void onStart(StreamController controller) {
        System.out.println(controller);
    }

    @Override
    public void onResponse(StreamingRecognizeResponse response) {
        responses.add(response);
        StreamingRecognitionResult result = response.getResultsList().get(0);
        Duration resultEndTime = result.getResultEndTime();
        resultEndTimeInMS = (int) ((resultEndTime.getSeconds() * 1000) + (resultEndTime.getNanos() / 1000000));
        double correctedTime = resultEndTimeInMS - bridgingOffset + (STREAMING_LIMIT * restartCounter);

        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
        if (result.getIsFinal()) {
            System.out.printf(
                    "%s: %s [confidence: %.2f]\n",
                    convertMillisToDate(correctedTime),
                    alternative.getTranscript(),
                    alternative.getConfidence());
            isFinalEndTime = resultEndTimeInMS;
            lastTranscriptWasFinal = true;
        } else {
            System.out.printf( "%s: %s", convertMillisToDate(correctedTime), alternative.getTranscript());
            lastTranscriptWasFinal = false;
        }
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(t.getMessage());
    }

    @Override
    public void onComplete() {
        for (StreamingRecognizeResponse response : responses) {
            StreamingRecognitionResult result = response.getResultsList().get(0);
            SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
            System.out.printf("Transcript : %s\n", alternative.getTranscript());
        }
    }

    private String convertMillisToDate(double milliSeconds) {
        long millis = (long) milliSeconds;
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(2);
        return String.format(
                "%s:%s /",
                format.format(TimeUnit.MILLISECONDS.toMinutes(millis)),
                format.format(TimeUnit.MILLISECONDS.toSeconds(millis)
                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
    }

    public int calculateChunks(double chunkTime, int audioInputSize){
        if (bridgingOffset < 0) {
            // bridging Offset accounts for time of resent audio
            // calculated from last request
            bridgingOffset = 0;
        }
        if (bridgingOffset > finalRequestEndTime) {
            bridgingOffset = finalRequestEndTime;
        }
        int chunksFromMs = (int) Math.floor((finalRequestEndTime - bridgingOffset) / chunkTime);
        // chunks from MS is number of chunks to resend
        bridgingOffset = (int) Math.floor(( audioInputSize - chunksFromMs) * chunkTime);
        return chunksFromMs;
    }


}
