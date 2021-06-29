package gui;

import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

public class MultiModalTextArea{
    private final PipedInputStream pipeIn = new PipedInputStream();
    private final TextArea txtArea;
    private Thread reader;
    private boolean quit;

    public MultiModalTextArea(){
        txtArea = new TextArea();
        txtArea.setStyle("-fx-font-size: 1.1em");
        this.executeReaderThreads();
    }

    public TextArea getTextArea(){
        return txtArea;
    }

    public void executeReaderThreads() {
        try {
            PipedOutputStream pout = new PipedOutputStream(this.pipeIn);
            System.setOut(new PrintStream(pout, true));
        }
        catch (IOException | SecurityException e) {
            e.printStackTrace();
        }



        ReaderThread obj = new ReaderThread(pipeIn, reader, quit, txtArea);
    }

    //method to handle thread closing on stage closing
    public synchronized void closeThread() {
        System.out.println("Message: Stage is closed.");
        this.quit = true;
        notifyAll();
        try { this.reader.join(1000L); this.pipeIn.close(); } catch (Exception e) {
            e.printStackTrace(); }
        System.exit(0);
    }

    public static class ReaderThread implements Runnable{

        private final PipedInputStream pipeIn ;
        Thread errorThrower;
        private Thread reader;
        private Thread reader2;
        private boolean quit;

        private final TextArea txtArea;

        ReaderThread(PipedInputStream pinInput1,
                     Thread reader11,
                     boolean newflag,
                     TextArea txtArea1) {
            pipeIn = pinInput1;
            reader = reader11;
            quit =  newflag;
            txtArea = txtArea1;

            this.quit = false;
            this.reader = new Thread(this);
            this.reader.setDaemon(true);
            this.reader.start();

            this.reader2 = new Thread(this);
            this.reader2.setDaemon(true);
            this.reader2.start();

            this.errorThrower = new Thread(this);
            this.errorThrower.setDaemon(true);
            this.errorThrower.start();
        }

        public synchronized void run() {
            try {
                while (Thread.currentThread() == this.reader) {
                    try {
                        wait(100L);
                    }
                    catch (InterruptedException ie) {
                        System.out.println("I am in thread 1");
                    }

                    if (this.pipeIn.available() != 0) {
                        String input = readLine(this.pipeIn); //reading console output stream from pipedinputstream
                        this.txtArea.appendText(input);
                    }
                    if (this.quit) return;
                }

                //while loop starting
                while (Thread.currentThread() == this.reader2) {
                    try {
                        wait(100L);
                    }
                    catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }

                    if (this.quit) return; //if some one closed the stage then this check will be performed every time, if true
                    //thread execution will be stopped.
                } //while loop ending here
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (Thread.currentThread() == this.errorThrower) {
                try {
                    wait(800L);
                }
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        public synchronized String readLine(PipedInputStream in) throws IOException {
            String input = "";
            do {
                int available = in.available();
                if (available == 0) break;
                byte[] b = new byte[available];
                in.read(b);
                input = input + new String(b, 0, b.length);
            }while ((!input.endsWith("\n")) && (!input.endsWith("\r\n")) && (!this.quit));
            return input;
        }
    }
}
