package com.monash.analytics.audio.services.utils;

import com.monash.analytics.audio.services.mixer.MultimodalMixer;

import javax.sound.sampled.*;
import java.util.Arrays;

public class MixerInfo {
    /**
     * print the mixer information (complete)
     * @see MultimodalMixer#initMixerInputs() uncomment to print this information.
     */
    public static void printMixerInfo(){
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
                    if(thisLineInfo instanceof DataLine.Info){
                        final DataLine.Info dataLineInfo = (DataLine.Info) thisLineInfo;
                        Arrays.stream(dataLineInfo.getFormats()).forEach(format -> System.out.println(format.toString()));
                    }
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

    private static String analyzeControl(Control thisControl) {
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
}
