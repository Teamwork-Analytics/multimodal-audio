package mixer;

import api.google_speech.SpeechToTextAPI;
import microphone.Microphone;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * The multimodal audio mixer that can only detect microphone (target data line) inputs
 * @author Riordan Dervin Alfredo (riordan.alfredo@gmail.com)
 */
public class MultimodalMixer {
    /**
     * A hashmap that contains mixer name and mixer object
     */
    private final HashMap<String, Mixer> mixerHashMap = new HashMap<>();
    /**
     * List of checked/selected microphones
     */
    private final HashMap<String, Microphone>  microphoneHashMap= new HashMap<>();

    /**
     * The session name
     * FIXME: create a GUI to insert session name
     */
    public static final String sessionName = "PENINSULA_TEST";
    /**
     * Speech to text api
     */
    private SpeechToTextAPI s2t;

    /**
     * Constructor
     * @param s2t speech to text api (dependency injection)
     */
    public MultimodalMixer(SpeechToTextAPI s2t){
        this.initMixerInputs();
        this.s2t = s2t;
    }

    /**
     * Constructor
     */
    public MultimodalMixer(){
        this.initMixerInputs();
    }

    /**
     * Initialise mixer and collect all audio inputs sources
     */
    public void initMixerInputs(){
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo(); // get all mixers data
        printMixerInfo();
//        getAllAvailableLines();
        for(Mixer.Info info : mixerInfos){
            Mixer mixer = AudioSystem.getMixer(info);
//            System.out.println(mixer.getTargetLines().length);
            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            String name = info.getName();
            if(lineInfos.length>=1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)){//Only prints out info is it is a Microphone
                mixerHashMap.put(name, mixer);
            }
        }

//        mockMicrophones();
    }

    private ArrayList<Line.Info> outputLines = new ArrayList<>();
    private ArrayList<Port.Info> outputPorts = new ArrayList<>();

    private void mockMicrophones() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            System.out.println("Found Mixer: " + mixerInfo);

            Mixer m = AudioSystem.getMixer(mixerInfo);

            Line.Info[] sourceLines = m.getSourceLineInfo();
            for (Line.Info li : sourceLines) {
                System.out.println("Found source line: " + li + " " + li.getClass());

                if (li instanceof Port.Info) {
                    Port.Info portInfo = (Port.Info) li;
                    System.out.println("port found " + portInfo.getName() + " is source " + portInfo.isSource());
//                    sourceDataLines.add(portInfo);
                }
            }

            Line.Info[] targetLines = m.getTargetLineInfo();

            for (Line.Info li : targetLines) {
                System.out.println("Found target line: " + li + " " + li.getClass());
                outputLines.add(li);

                if (li instanceof Port.Info) {
                    Port.Info portInfo = (Port.Info) li;
                    System.out.println("port found " + portInfo.getName() + " is source " + portInfo.isSource());
                    outputPorts.add(portInfo);
                }
            }
        }
    }

    private void getAllAvailableLines(){
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        List<Line.Info> availableLines = new ArrayList<Line.Info>();
        for (Mixer.Info mixerInfo : mixers){
            System.out.println("Found Mixer: " + mixerInfo);

            Mixer m = AudioSystem.getMixer(mixerInfo);

            Line.Info[] lines = m.getTargetLineInfo();

            for (Line.Info li : lines){
                System.out.println("Found target line: " + li);
                try {
                    m.open();
                    availableLines.add(li);
                } catch (LineUnavailableException e){
                    System.out.println("Line unavailable.");
                }
            }
        }

        System.out.println("Available lines: " + availableLines);
    }

    public void printMixerInfo(){
        try{
            System.out.println("OS: "+System.getProperty("os.name")+" "+
                    System.getProperty("os.version")+"/"+
                    System.getProperty("os.arch")+"\nJava: "+
                    System.getProperty("java.version")+" ("+
                    System.getProperty("java.vendor")+")\n");
            for (Mixer.Info thisMixerInfo : AudioSystem.getMixerInfo()) {
                System.out.println("Mixer: "+thisMixerInfo.getDescription()+
                        " ["+thisMixerInfo.getName()+"]");
                Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);
                for (Line.Info thisLineInfo:thisMixer.getSourceLineInfo()) {
                    System.out.println("----" + thisLineInfo);
                    if (thisLineInfo.getLineClass().getName().equals(
                            "javax.sound.sampled.Port")) {
                        Line thisLine = thisMixer.getLine(thisLineInfo);
                        thisLine.open();
                        System.out.println("  Source Port: " +thisLineInfo.toString());
                        for (Control thisControl : thisLine.getControls()) {
                            System.out.println(analyzeControl(thisControl));
                        }
                        thisLine.close();
                    }
                }
                thisMixer.getTargetLines();

                for (Line.Info thisLineInfo:thisMixer.getTargetLineInfo()) {
                    if (thisLineInfo.getLineClass().getName().equals(
                            "javax.sound.sampled.Port")) {
                        Line thisLine = thisMixer.getLine(thisLineInfo);
                        thisLine.open();
                        System.out.println("  Target Port: " +thisLineInfo.toString());
                        for (Control thisControl : thisLine.getControls()) {
                            System.out.println(analyzeControl(thisControl));}
                        thisLine.close();
                    }
                }
            }
        } catch (Exception e) {e.printStackTrace();}
    }
    private String analyzeControl(Control thisControl) {
        String type = thisControl.getType().toString();
        if (thisControl instanceof BooleanControl) {
            return "    Control: "+type+" (boolean)"; }
        if (thisControl instanceof CompoundControl) {
            System.out.println("    Control: "+type+
                    " (compound - values below)");
            String toReturn = "";
            for (Control children:
                    ((CompoundControl)thisControl).getMemberControls()) {
                toReturn+="  "+ analyzeControl(children)+"\n";
            }
            return toReturn.substring(0, toReturn.length()-1);
        }
        if (thisControl instanceof EnumControl) {
            return "    Control:"+type+" (enum: "+thisControl.toString()+")";
        }
        if (thisControl instanceof FloatControl) {
            return "    Control: "+type+" (float: from "+
                    ((FloatControl) thisControl).getMinimum()+" to "+
                    ((FloatControl) thisControl).getMaximum()+")";
        }
        return "    Control: unknown type";
    }


    /**
     * Spawn a new microphone/selected one
     * @param micName microphone name = mixer name
     * @return able to add new microphone or not based on the hashmap
     */
    public boolean createNewMicrophone(String micName){
        if(microphoneHashMap.containsKey(micName)) return false;
        Microphone microphone = new Microphone(AudioFileFormat.Type.WAVE, micName);
        microphoneHashMap.put(micName, microphone);
        return true;
    }


    /**
     * Record using microphone
     * @param micName microphone name
     */
    public void record(String micName){
        new Thread(()->{
            try {
                microphoneHashMap.get(micName).captureAudioToFile(sessionName, mixerHashMap.get(micName));
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Stop listening from a microphone
     * @param micName microphone name
     */
    public void stop(String micName){
        Line[] lines = mixerHashMap.get(micName).getTargetLines();
        for(Line line : lines){
            Line.Info lineInfo = line.getLineInfo();
            System.out.println("line info: " + lineInfo);

        }
        microphoneHashMap.get(micName).close();
    }

    /**
     * Getting all name of microphones that are connected to the mixer.
     * @return a list of microphones names
     */
    public ArrayList<String> getAllMicNames(){
        return new ArrayList<>(mixerHashMap.keySet());
    }

    /**
     * Using the Speech-to-text API to transcribe speech to text
     * @param mixerName the mixer key (name) from hashmap
     */
    public void listen(String mixerName) {
        if(this.selectAMixer(mixerName)){
            microphoneHashMap.get(mixerName).open();
            System.out.println("Start listening...");
            File audioFile = new File("test.wav");
            microphoneHashMap.get(mixerName).transcribeSpeechToText(s2t);
        }
    }

    /**
     * Choose one mixer input
     * @param mixerName the name of mixer
     * @return true/false if the system can find the mixer or not respectively
     */
    private boolean selectAMixer(String mixerName){
        Mixer selectedMixer = mixerHashMap.get(mixerName);
        if(selectedMixer == null) {System.out.println("Cannot find mixer"); return false;}
        microphoneHashMap.get(mixerName).initTargetDataLineFromMixer(selectedMixer);
        return true;
    }

    /**
     * Delete a microphone object from the hashmap
     * @param micName the name/key of microphone
     */
    public void deleteMicrophone(String micName) {
        microphoneHashMap.remove(micName);
    }

    /**
     * Get the mixer name
     * @param mixer mixer object
     * @return mixer name
     */
    public String getMixerName(Mixer mixer){
        if(mixer == null) return "";
        Mixer.Info info = mixer.getMixerInfo();
        return info.getName();
    }
}
