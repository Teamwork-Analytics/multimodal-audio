# Multimodal Analytics Audio
Multimodal Audio interface with TASCAM audio.mixer, Java Sound, Google Speech-to-text API, and Apache Kafka (Producer)
##  Set-up
Must use `Java 15` due to JavaFX configuration. 

Copy the following set-up in your Intellij. 
> If you have not downloaded JavaFX sdk, please do so and re-place `/path/to/directory` with the correct path. We might want to upgrade it with maven dependency instead.

> For Google Speech API, we will create a new one under Monash account. At the moment, Rio is using his own personal account for testing.
```bash
# VM Options:

--module-path
/path/to/directory/javafx-sdk-11.0.2/lib
--add-modules
javafx.controls,javafx.fxml
--add-exports
javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED

# Environment Variables
GOOGLE_APPLICATION_CREDENTIALS=/path/to/directory/google-speech-api.json
```

The main class that runs as a Audio Service server (connected via Eureka Server):
```properties
com.monash.analytics.audio.AudioWebApp
```

### Optionals
Use this property if we decide to be controlled by a front-end (JavaFX)
```properties
spring.main.web-application-type=none
```

## Running application

1. Make sure that Eurea-Server is running
2. Run the AudioWebApp SpringBoot application
3. Follow the REST-API command below


###  API Documentation
Port number: **7501**

- `http://localhost:7501/audio/`: initialise microphone.
- `http://localhost:7501/audio/start`: start the recording.
- `http://localhost:7501/audio/stop`: close/pause the recording.

> NOTE: at the moment, the initialisation part is fully hard-coded
> - usingTranscription: `false`
> - usingPlayback: `false`
> - selectedChannels: `<0,"channel 1">, <8,"channel 9">`
> - sessionName: Date and time format, `{yyyyMMddHHmmss}`