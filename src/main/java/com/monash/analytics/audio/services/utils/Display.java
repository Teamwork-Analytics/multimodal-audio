package com.monash.analytics.audio.services.utils;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;


public class Display {

    public static final String AUTOCORRECT = "\033[2K\r";

    public enum ColourEnum{
        RED("\033[0;31m"),
        GREEN ("\033[0;32m"),
        YELLOW ("\033[0;33m"),
        WHITE("\033[0m");

        private ColourEnum(String label) {
            this.label = label;
        }

        public final String label;
    }

    public static void println(Object text){
        System.out.println(text);
    }

    public static void printColour(Object text, ColourEnum colourEnum){
        System.out.print(colourEnum.label);
        System.out.print(AUTOCORRECT);
        System.out.printf("%s: %s", Instant.now().toString(),text);
    }


    public static class CheckMemory extends TimerTask{

        @Override
        public void run() {
            long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            System.out.println("MEMORY: " + memory);
        }
    }

    public static void runInterval(){
        Timer timer = new Timer();
        timer.schedule(new CheckMemory(), 0, 5000);
    }


}
