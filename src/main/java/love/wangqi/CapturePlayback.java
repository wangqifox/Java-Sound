package love.wangqi;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by wangqi on 2017/2/9.
 */
public class CapturePlayback {
    float sampleRate = 44100;
    int sampleSizeInBits = 16;
    int channels = 2;
    int frameSize = (sampleSizeInBits/8)*channels;
    float frameRate = sampleRate;
    boolean bigEndian = false;

    AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sampleRate,
            sampleSizeInBits,
            channels,
            frameSize,
            frameRate,
            bigEndian
    );

    class Data {
        byte[] data;
        int numBytesRead;

        public Data(byte[] data, int numBytesRead) {
            this.data = data;
            this.numBytesRead = numBytesRead;
        }
    }
    LinkedBlockingQueue<Data> queue = new LinkedBlockingQueue<>();


    class Capture implements Runnable {

        @Override
        public void run() {
            TargetDataLine targetDateLine = null;
            DataLine.Info targetInfo = new DataLine.Info(
                    TargetDataLine.class,
                    format
            );

            // get and open the target data line for capture.
            try {
                targetDateLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
                targetDateLine.open(format, targetDateLine.getBufferSize());
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }

            // play back the captured audio data
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = targetDateLine.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;

            targetDateLine.start();

            int i = 0;
            while (true) {
                if((numBytesRead = targetDateLine.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }

                try {
                    queue.put(new Data(data, numBytesRead));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println(i++);
            }

            targetDateLine.stop();
            targetDateLine.close();

        }

        public void start() {
            Thread thread = new Thread(this);
            thread.setName("Capture");
            thread.start();
        }
    }

    class Playback implements Runnable {

        @Override
        public void run() {
            SourceDataLine sourceDataLine = null;
            DataLine.Info sourceInfo = new DataLine.Info(
                    SourceDataLine.class,
                    format
            );

            try {
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
                sourceDataLine.open(format);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }

            sourceDataLine.start();

            while (true) {
                Data data = null;
                try {
                    data = queue.take();
                    sourceDataLine.write(data.data, 0, data.numBytesRead);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


//            sourceDataLine.drain();
//            sourceDataLine.close();

        }

        public void start() {
            Thread thread = new Thread(this);
            thread.setName("Playback");
            thread.start();
        }
    }

    public void run() {
        System.out.println(format);
        new Capture().start();
        new Playback().start();
    }

    public static void main(String[] args) {
        CapturePlayback capturePlayback = new CapturePlayback();
        capturePlayback.run();
    }
}
