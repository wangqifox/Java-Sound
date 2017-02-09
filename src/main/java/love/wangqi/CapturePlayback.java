package love.wangqi;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    public void run() {
        TargetDataLine line = null;

        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                sampleSizeInBits,
                channels,
                frameSize,
                frameRate,
                bigEndian
        );

        System.out.println(format);

        DataLine.Info info = new DataLine.Info(
                TargetDataLine.class,
                format
        );

        System.out.println(AudioSystem.isLineSupported(info));

        // get and open the target data line for capture.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        // play back the captured audio data
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        line.start();
        int i = 0;
        while (i < 20) {
            if((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            out.write(data, 0, numBytesRead);
            i++;
            System.out.println(i);
        }

        line.stop();
        line.close();

        // stop and close the output stream
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load bytes into the audio input stream for playback
        byte audioBytes[] = out.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);

        AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

        try {
            audioInputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new PlaySounds().play(audioInputStream);
    }

    public static void main(String[] args) {
        CapturePlayback capturePlayback = new CapturePlayback();
        capturePlayback.run();
    }
}
