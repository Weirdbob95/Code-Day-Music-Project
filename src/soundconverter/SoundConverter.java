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
    public static double[][] confidence = new double[0][0];
    public static List<Integer>[] notes = new List[0];
    public static Framebuffer fft;
    public static int sampleRate, bufferSize, jump;

    public static void main(String[] args) {

        Core.init();

        // Draw the fft
        fft = new Framebuffer(new TextureAttachment());
        double pixelSize = 4;
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
            bufferSize = 1024 * 4;
            jump = 256 * 4;
            results = runFFT(wavFile, bufferSize, jump);
            process();
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
                for (int y = 0; y < result.length; y++) {
                    maxVal = Math.max(maxVal, result[y]);
                }
            }

            for (int x = 0; x < results.length; x++) {
                for (int y = 0; y < results[x].length; y++) {
                    Graphics2D.fillRect(new Vec2(x, y).multiply(pixelSize), new Vec2(pixelSize), Color4.gray(Math.pow(results[x][y] / maxVal, .5)));
                }

                // Pitch detector 1
                double total = 0;
                for (int y = 0; y < results[x].length; y++) {
                    if (results[x][y] * 4 > maxVal) {
                        total += results[x][y];
                    }
                }
                if (total != 0) {
                    double runningTotal = 0;
                    for (int y = 0; y < results[x].length; y++) {
                        if (results[x][y] * 4 > maxVal) {
                            runningTotal += results[x][y];
                            if (runningTotal > total / 2) {
                                Graphics2D.fillRect(new Vec2(x, y).multiply(pixelSize), new Vec2(pixelSize), Color4.YELLOW);
                                break;
                            }
                        }
                    }
                }

                // Pitch detector 2
                int best = 0;
                for (int y = 0; y < confidence[x].length; y++) {
                    if (confidence[x][y] > confidence[x][best]) {
                        best = y;
                    }
                    if (confidence[x][y] > .5) {
                        Graphics2D.fillRect(new Vec2(x, (double) noteToFrequency(best / 10.) * bufferSize / sampleRate).multiply(pixelSize), new Vec2(pixelSize), Color4.RED.withA(.1));
                    } else if (confidence[x][y] > .1) {
                        Graphics2D.fillRect(new Vec2(x, (double) noteToFrequency(best / 10.) * bufferSize / sampleRate).multiply(pixelSize), new Vec2(pixelSize), Color4.BLUE.withA(.1));
                    }
                }
                if (confidence[x][best] > .05) {
                    Graphics2D.fillRect(new Vec2(x, (double) noteToFrequency(best / 10.) * bufferSize / sampleRate).multiply(pixelSize), new Vec2(pixelSize), Color4.RED);
                } else {
                    Graphics2D.fillRect(new Vec2(x, (double) noteToFrequency(best / 10.) * bufferSize / sampleRate).multiply(pixelSize), new Vec2(pixelSize), Color4.BLUE);
                }

                // Pitch detector 3
                for (int y : notes[x]) {
                    Graphics2D.fillRect(new Vec2(x, (double) noteToFrequency(y / 10.) * bufferSize / sampleRate).multiply(pixelSize), new Vec2(pixelSize), Color4.GREEN);
                }
            }
        });
    }

    private static int findMax(double[] a) {
        int max = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > a[max]) {
                max = i;
            }
        }
        return max;
    }

    public static void process() {

//        double[][] bins = new double[results.length][400];
//        for (int x = 0; x < results.length; x++) {
//            for (int y = 0; y < bins[x].length; y++) {
//                double sum = 0;
//                for (int y2 = (int) Math.pow(1.02, y); y2 < Math.pow(1.02, y + 1) && y2 < results[x].length; y2++) {
//                    sum += 1;
//                    bins[x][y] += results[x][y2];
//                }
//                bins[x][y] /= sum;
//                bins[x][y] *= 1;
//            }
//        }
//        results = bins;
        // Blur
        int blurSize = 5;
        double blurFactor = 10;
        double[][] r = new double[results.length][1000];
        for (int x = 0; x < r.length; x++) {
            for (int y = 0; y < r[x].length; y++) {
                double sum = 0;
                for (int i = -blurSize; i <= blurSize; i++) {
                    for (int j = -blurSize; j <= blurSize; j++) {
                        if (x + i >= 0 && x + i < r.length && y + j >= 0 && y + j < r[0].length) {
                            double filter = Math.exp(-Math.sqrt(i * i + j * j) * blurFactor);
                            sum += filter;
                            r[x][y] += results[x + i][y + j] * filter;
                        }
                    }
                }
                r[x][y] /= sum;
            }
        }
        results = r;

        // Scale
        double maxVal = 0;
        for (int x = 0; x < results.length; x++) {
            for (int y = 0; y < results[x].length; y++) {
                results[x][y] = Math.pow(results[x][y], 2);
                maxVal = Math.max(maxVal, results[x][y]);
            }
        }

        for (int x = 0; x < results.length; x++) {
            for (int y = 0; y < results[x].length; y++) {
                results[x][y] /= maxVal;
            }
        }

        // Confidence
        confidence = new double[results.length][1280];
        for (int x = 0; x < results.length; x++) {
            for (int y = 0; y < results[0].length; y++) {
                double baseFreq = noteToFrequency(y / 10.);
                for (int i = 1; i < 25; i++) {
                    int pos = (int) (baseFreq * i * bufferSize / sampleRate);
                    if (pos < results[x].length) {
                        confidence[x][y] += results[x][pos] / Math.pow(1.1, i);
                    }
                }
            }
        }

        // Detect pitches
        notes = new List[results.length];
        for (int x = 0; x < results.length; x++) {
            notes[x] = new ArrayList();
            while (true) {
                int best = findMax(confidence[x]);
                if (confidence[x][best] < .05) {
                    break;
                }
                notes[x].add(best);
                break;
//                for (int i = 1; i < 10; i++) {
//                    for (int j = -10; j <= 10; j++) {
//                        int pos = (int) (best + Math.log(i) * 120 / Math.log(2) + j);
//                        if (pos < confidence[x].length && pos >= 0) {
//                            confidence[x][pos] = 0;
//                        }
//                    }
//                }
//                for (int i = 1; i < 10; i++) {
//                    for (int j = -10; j <= 10; j++) {
//                        int pos = (int) (best - Math.log(i) * 120 / Math.log(2) + j);
//                        if (pos < confidence[x].length && pos >= 0) {
//                            confidence[x][pos] = 0;
//                        }
//                    }
//                }
            }
        }
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

    public static double noteToFrequency(double note) {
        return Math.pow(2, (note - 49) / 12.) * 440;
    }

    public static double fftToFrequency(double pos) {
        return pos * sampleRate / bufferSize;
    }
}
