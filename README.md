# Multimodal Analytics Audio
Multimodal Audio interface with TASCAM audio.mixer, Java Sound, Google Speech-to-text API, and Apache Kafka (Producer)

Multimodal audio interface application to collect audio data from multiple microphones. It uses Java Sound API (
linux/mac - `not being maintained anymore!`) or JasioHost API (windows). We use Google Speech-to-text API (linux only - `not maintained`), and Apache Kafka for data
streaming (in progress).

## Quick Setup:
For optimal setup please ensure you're using `IntelliJ Ultimate` Edition and are running `Java 17`.

To get started quickly, follow these steps:

1. Open the terminal within your preferred IDE, ensuring that you're located within the ROOT directory of the project.

2. Run the following command:
`mvn clean install -U`

Make sure you are in the same directory as the `pom.xml` file.
This command will rebuild and install all dependencies required for a smooth startup process.

Don't forget to update the `AUDIO_DRIVER_BAND_NAME` variable in `Constants.java` to match the name of the ASIO Driver
associated with your audio interface. This ensures proper configuration for capturing audio on your system.

Please also ensure you have also installed the ASIO Driver for the given Audio Interface and ensure the ASIO Driver 
isn't being used by any applications. Please note: The Application installed with the Audio Interface may need to be 
removed as this library will break if the ASIO Driver is already in use.

Once all of the above has been completed: Run the `AudioWebApp` SpringBoot application


## Getting Started

Please note that we have decided to stop maintaining `LinuxAudioServiceAPI` (e.g., Java Sound API/linux based) and all
its dependencies because we are using Windows machine in our experiment. The set up below will focus mainly on Windows
environment.

This application uses `Java 17`.

1. Pull all dependencies with Maven via `pom.xml` (at the root).
2. Follow the installation of JasioHost in [here](https://github.com/mhroth/jasiohost).
3. Install [ASIO4ALL](https://www.asio4all.org/) driver and off-line setting app. This third-party helps you to manage
   multiple audio inputs if you have several physical audio interfaces.
4. Connect your machine with the audio interface (we use TASCAM 16x8)
5. Open `ASIO4ALL v2 Off-line settings` app, and turn on relevant input & output drivers. Make sure that you disable
   your computer's input drivers (e.g., `Realtek HD Audio Stereo input`). Here, you can also redirect the audio output
   to another devices (e.g., bluetooth speaker).

## Running application

1. Make sure that Eureka-Server (main server) is running
2. Run the `AudioWebApp` SpringBoot application
3. Follow the REST-API command below

### API Documentation

Port number: **7501**

- `http://localhost:7501/audio/`: initialise microphone.
- `http://localhost:7501/audio/start-baseline/{sessionId}`: start the baseline recording.
- `http://localhost:7501/audio/start/{sessionId}`: start the recording.
- `http://localhost:7501/audio/stop`: close/pause the recording.
- `http://localhost:7501/audio/start-time/{sessionId}`: for synchronisation with the main server.
- `http://localhost:7501/audio/stop-time/{sessionId}`: for synchronisation with the main server.

## Known Issues

1. Make sure that the audio web application (`AudioWebApp`) is stopped appropriately before restarting the service (
   use `audio/stop` command above). Otherwise, the ASIO driver will lock the Window's audio services, and you might want
   to restart the whole computer.
2. We have tried to save the audio file while it is streaming, but to no avail. It happens because of the nature of `WAV` format. It requires `size` info in its file's heading that cannot be determined when the audio is still streaming. It can only write audio file **after** all data has arrived ([ref1](https://stackoverflow.com/a/33519834), [ref2](https://forums.ni.com/t5/LabVIEW/writing-WAV-file-contyinuously-streaming/td-p/471098)). It poses another challenge that is not worthed to pursue. To cope this issue, the application is currently designed to save all audio files whenever the application crashes. There could be a possibility that the application will crash and it cannot save the audio files as intended. Fortunately, we didn't see such problem in our experiments.      
3. In the previous development, we used JavaFX for the UI and uses Linux environment. However, the team decided to
   discard it. So, we leave the following documentation if we decide to use JavaFX again:

> DEPRECATED INFORMATION
>
> copy the following set-up in your Intellij's Configuration setting.
> ```bash
> # VM Options:
> --module-path
> /path/to/directory/javafx-sdk-11.0.2/lib
> --add-modules
> javafx.controls,javafx.fxml
> --add-exports
> javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED
> # Environment Variables
> GOOGLE_APPLICATION_CREDENTIALS=/path/to/directory/google-speech-api.json
> ```
> ```properties
> com.monash.analytics.audio.AudioWebApp
> ```
> If you have not downloaded JavaFX sdk, please do so and re-place `/path/to/directory` with the correct path.
> We might want to upgrade it with maven dependency instead.
> For Google Speech API, we will create a new main user account. The main class that runs as a Audio Service server (connected via Eureka Server):
>
> Use this property if we decide to be controlled by a front-end (JavaFX)
> ```properties
> spring.main.web-application-type=none
> ```
> NOTE: at the moment, the initialisation part is fully hard-coded:
> ```
> - usingTranscription: `false`
> - usingPlayback: `false`
> - selectedChannels: `<0,"channel 1">, <8,"channel 9">`
> - sessionName: Date and time format, `{yyyyMMddHHmmss}`
> ```

## Contact Person

If you need any help with the set-up, found any issues, or anything, please don't hesitate contacting:

Riordan Dervin Alfredo (e: [riordan.alfredo1@monash.edu]()).
Antoni Robert Erdeg (e: [antonierdeg@gmail.com]()).
