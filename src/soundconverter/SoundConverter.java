package soundconverter;

import engine.Core;
import engine.Input;
import graphics.Camera;
import graphics.Graphics2D;
import graphics.Window2D;
import graphics.data.Framebuffer;
import graphics.data.Framebuffer.TextureAttachment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.lwjgl.input.Keyboard;
import soundconverter.fft.Complex;
import soundconverter.fft.FFT;
import soundconverter.wavfile.WavFile;
import soundconverter.wavfile.WavFileException;
import util.Color4;
import util.Mutable;
import util.Vec2;

public class SoundConverter {

    public static double[][] results = new double[0][0];
    public static Framebuffer fft;
    public static int sampleRate, jump;

    public static void main(String[] args) {

        Core.init();

        // Draw the fft
        fft = new Framebuffer(new TextureAttachment());
        double pixelSize = 2;
//        loadFile("sounds/classical_solo.wav");
//        drawFFT(fft, pixelSize);

        Mutable<Long> initialTime = new Mutable(System.currentTimeMillis());

        Window2D.viewPos = Window2D.viewSize.multiply(.5);
        Input.whileKeyDown(Keyboard.KEY_W).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, 100 * dt)));
        Input.whileKeyDown(Keyboard.KEY_A).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(100 * -dt, 0)));
        Input.whileKeyDown(Keyboard.KEY_S).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, 100 * -dt)));
        Input.whileKeyDown(Keyboard.KEY_D).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(100 * dt, 0)));

        Recorder r = new Recorder();
        Input.whenKey(Keyboard.KEY_SPACE, true).onEvent(() -> {
            try {
                r.beginRecording(new File("sounds/recording"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Recording started");
        });
        Input.whenKey(Keyboard.KEY_SPACE, false).onEvent(() -> {
            r.endRecording();
            loadFile("sounds/recording");
            drawFFT(fft, pixelSize);
            playFile("sounds/recording");
            initialTime.o = System.currentTimeMillis();
            System.out.println("Recording finished");
        });

        Core.render.onEvent(() -> {
            fft.render();
            Camera.setProjection2D(Window2D.LL(), Window2D.UR());
            double lineX = (double) sampleRate / jump * pixelSize * (System.currentTimeMillis() - initialTime.o) / 1000;
            Graphics2D.drawLine(new Vec2(lineX, 0), new Vec2(lineX, 800), Color4.RED, 1);
        });

        Core.run();

    }

    public static void loadFile(String filename) {
        try {
            WavFile wavFile = WavFile.openWavFile(new File(filename));
            sampleRate = (int) wavFile.getSampleRate();
            int bufferSize = 1024 * 4;
            jump = 256 * 4;
            results = runFFT(wavFile, bufferSize, jump);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playFile(String filename) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawFFT(Framebuffer fft, double pixelSize) {
        fft.clear(Color4.TRANSPARENT);
        fft.with(() -> {
            Camera.setProjection2D(Window2D.LL(), Window2D.UR());

            double maxVal = 0;
            for (double[] result : results) {
                for (int y = 0; y < result.length / 2; y++) {
                    maxVal = Math.max(maxVal, result[y]);
                }
            }

            for (int x = 0; x < results.length; x++) {
                for (int y = 0; y < results[x].length / 2; y++) {
                    Graphics2D.fillRect(new Vec2(x, y).multiply(pixelSize), new Vec2(pixelSize), Color4.gray(Math.pow(results[x][y] / maxVal, .5)));
                }

                // Pitch detector 1
                double total = 0;
                for (int y = 0; y < results[x].length / 2; y++) {
                    if (results[x][y] * 4 > maxVal) {
                        Graphics2D.fillRect(new Vec2(x, y).multiply(pixelSize), new Vec2(pixelSize), Color4.GREEN);
                        total += results[x][y];
                    }
                }
                // Pitch detector 2
                int best = 0;
                for (int y = 0; y < results[x].length / 2; y++) {
                    if (results[x][y] * Math.pow(y, .2) > results[x][best] * Math.pow(best, .2)) {
                        best = y;
                    }
                }
                Graphics2D.fillRect(new Vec2(x, best).multiply(pixelSize), new Vec2(pixelSize), Color4.RED);
                // Pitch detector 3
                if (total != 0) {
                    double runningTotal = 0;
                    for (int y = 0; y < results[x].length / 2; y++) {
                        if (results[x][y] * 4 > maxVal) {
                            runningTotal += results[x][y];
                            if (runningTotal > total / 2) {
                                Graphics2D.fillRect(new Vec2(x, best).multiply(pixelSize), new Vec2(pixelSize), Color4.YELLOW);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public static double[][] runFFT(WavFile wavFile, int bufferSize, int jump) {
        try {

            // Display information about the wav file
            wavFile.display();

            // Create a buffer of bufferSize frames
            int numChannels = wavFile.getNumChannels();
            double[] buffer = new double[bufferSize * numChannels];

            Complex[] complexBuffer = new Complex[bufferSize];
            List<Complex[]> results = new ArrayList();

            while (true) {

                // Read frames into buffer
                System.arraycopy(buffer, jump, buffer, 0, bufferSize - jump);
                int framesRead = wavFile.readFrames(buffer, bufferSize - jump, jump);
                if (framesRead == 0) {
                    break;
                }

                // Do FFT
                for (int i = 0; i < bufferSize; i++) {
                    double windowVal = .54 - .46 * Math.cos(2 * Math.PI * i / bufferSize);
                    complexBuffer[i] = new Complex(buffer[i * numChannels] * windowVal, 0);
                }
                results.add(FFT.fft(complexBuffer));

            }

            // Close the wavFile
            wavFile.close();

            // Copy results to array
            double[][] r = new double[results.size()][results.get(0).length];
            for (int x = 0; x < results.size(); x++) {
                for (int y = 0; y < results.get(0).length; y++) {
                    r[x][y] = results.get(x)[y].abs();
                }
            }
            return r;

        } catch (IOException | WavFileException e) {
            e.printStackTrace();
        }
        return null;
    }
}
