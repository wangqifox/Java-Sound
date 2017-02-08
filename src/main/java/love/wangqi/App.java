package love.wangqi;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        try {
            AudioInputStream currentSound = AudioSystem.getAudioInputStream(new File("welcome.wav"));
            AudioFormat format = currentSound.getFormat();

            DataLine.Info info = new DataLine.Info(
                    SourceDataLine.class,
                    format
            );

            SourceDataLine auline = (SourceDataLine)AudioSystem.getLine(info);
            auline.open(format);
            auline.start();

            int nBytesRead = 0;
            byte[] abData = new byte[512];
            try {
                while (nBytesRead != -1) {
                    nBytesRead = currentSound.read(abData, 0, abData.length);
                    if (nBytesRead >= 0)
                        auline.write(abData, 0, nBytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {
                auline.drain();
                auline.close();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
