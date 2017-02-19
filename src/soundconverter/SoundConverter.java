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
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.lwjgl.input.Keyboard;
import soundconverter.fft.Complex;
import soundconverter.fft.FFT;
import soundconverter.wavfile.Frame;
import soundconverter.wavfile.WAV;
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
    public static List<Interval> result = new ArrayList();
    public static double[] verticalLength;
    public static List<Interval> result2 = new ArrayList<Interval>();

    public static void main(String[] args) {

        Core.init();

        // Draw the fft
        fft = new Framebuffer(new TextureAttachment());
        double pixelSize = 2;

        Mutable<Long> initialTime = new Mutable(System.currentTimeMillis());

        Window2D.viewPos = Window2D.viewSize.multiply(.5);
        Input.whileKeyDown(Keyboard.KEY_W).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, 100 * dt)));
        Input.whileKeyDown(Keyboard.KEY_A).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(100 * -dt, 0)));
        Input.whileKeyDown(Keyboard.KEY_S).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, 100 * -dt)));
        Input.whileKeyDown(Keyboard.KEY_D).forEach(dt -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(100 * dt, 0)));

        Recorder r = new Recorder();
        Mutable<Boolean> recording = new Mutable(false);
        Input.whenKey(Keyboard.KEY_SPACE, true).onEvent(() -> {
            if (!recording.o) {
                try {
                    r.beginRecording(new File("sounds/recording"));
                    recording.o = true;
                    System.out.println("Recording started");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                r.endRecording();
                loadFile("sounds/recording");
                List music = MusicCreator.toNoteList(result);
                WAV.genWAE(music, "sounds/music", 44100, 8);
                drawFFT(fft, pixelSize);
                playFile("sounds/music");
                //playFile("sounds/recording");
                initialTime.o = System.currentTimeMillis();
                recording.o = false;
                System.out.println("Recording finished");
            }
        });

        Input.whenKey(Keyboard.KEY_T, true).onEvent(() -> {
            loadFile("sounds/dream-battle.wav");
            List music = MusicCreator.toNoteList(result);
            WAV.genWAE(music, "sounds/music", 44100, 32);
            drawFFT(fft, pixelSize);
            playFile("sounds/dream-battle.wav");
            initialTime.o = System.currentTimeMillis();
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
            smooth();
            calculateVertically();
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

            for (int x = 0; x < results.length; x++) {

                // Draw the amplitudes
                for (int y = 0; y < results[x].length; y++) {
                    Graphics2D.fillRect(new Vec2(x, y).multiply(pixelSize), new Vec2(pixelSize), Color4.gray(results[x][y]));
                }

                // Pitch detector
//                int best = max(confidence[x]);
//                if (confidence[x][best] > .2) {
//                    Graphics2D.fillRect(new Vec2(x, (double) noteToFrequency(best / 10.) * bufferSize / sampleRate).multiply(pixelSize), new Vec2(pixelSize), Color4.RED);
//                } else {
//                    Graphics2D.fillRect(new Vec2(x, (double) noteToFrequency(best / 10.) * bufferSize / sampleRate).multiply(pixelSize), new Vec2(pixelSize), Color4.BLUE);
//                }
                for (int y : notes[x]) {
                    Graphics2D.fillRect(new Vec2(x, (double) noteToFrequency(y / 10.) * bufferSize / sampleRate).multiply(pixelSize), new Vec2(pixelSize), Color4.GREEN.withA(.5));
                    Graphics2D.fillRect(new Vec2(x, y / 10.).multiply(pixelSize), new Vec2(pixelSize), Color4.YELLOW.withA(.5));
                }
            }

            for (Interval i : result) {
                i.draw(pixelSize);
            }
        });
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
        int blurSize = 0;
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
                results[x][y] = Math.pow(results[x][y], 1);
                maxVal = Math.max(maxVal, results[x][y]);
            }
        }

        for (int x = 0; x < results.length; x++) {
            for (int y = 0; y < results[x].length; y++) {
                results[x][y] /= maxVal;
            }
        }

//        // Fake a pitch
//        for (int x = 0; x < results.length; x++) {
//            results[x][50] = .75;
//        }
        // Find notes
        confidence = new double[results.length][1280];
        notes = new List[results.length];
        for (int x = 0; x < results.length; x++) {

            double[] resultsCopy = results[x].clone();
            notes[x] = new ArrayList();

            while (true) {

                confidence[x] = new double[confidence[x].length];
                for (int y = 0; y < results[0].length; y++) {
                    double baseFreq = noteToFrequency(y / 10.);
                    for (int i = 1; i < 10; i++) {
                        int pos = (int) (baseFreq * i * bufferSize / sampleRate);
                        if (pos < results[x].length) {
                            confidence[x][y] += resultsCopy[pos] / Math.pow(1.1, i);
                        }
                    }
                }

                int best = max(confidence[x]);
                if (confidence[x][best] < .2) {
                    break;
                }
                notes[x].add(best);
                for (int i = 0; i < 10; i++) {
                    for (int j = -3; j <= 3; j++) {
                        int pos = (int) (noteToFrequency(best / 10.) * i * bufferSize / sampleRate) + j;
                        if (pos >= 0 && pos < results[x].length) {
                            resultsCopy[pos] *= i * i / 100;
                        }
                    }
                }
            }
        }
    }

    private static void smooth() {

        result = new ArrayList();

        Interval active = null;
        for (int x = 0; x < results.length + 5; x++) {
            if (active != null && x > active.end + 2) {
                if (active.end > active.start + 5) {
                    result.add(active);
                }
                active = null;
            }
            if (x < results.length && !notes[x].isEmpty()) {
                int note = notes[x].get(0);
                if (active == null) {
                    active = new Interval(x, x, note);
                } else {
                    if (Math.abs(note - active.freq) < 20) {
                        active.end = x;
                        active.freq = ((active.end - active.start + 1) * active.freq + note) / (active.end - active.start + 2);
                    }
                }
            }
        }
    }

    public static void calculateVertically() {
        verticalLength = new double[results.length];
        for (int i = 0; i < results.length; i++) {
            verticalLength[i] = 0;
            for (double j : results[i]) {
                verticalLength[i] += j * j;
            }
            verticalLength[i] = Math.pow(verticalLength[i], .5);
        }
        smooth2();
    }

    private static void smooth2() {
        result2 = new ArrayList();

        Interval active = null;
        for (int x = 0; x < results.length + 5; x++) {
            if (active != null && x > active.end + 2) {
                if (active.end > active.start + 5) {
                    result2.add(active);
                }
                active = null;
            }
            if (x < results.length) {
                double note = verticalLength[x];
                if (active == null) {
                    active = new Interval(x, x, note);
                } else {
                    if (Math.abs((note - active.freq) / active.freq) < 0.5) {
                        active.end = x;
                        active.freq = ((active.end - active.start + 1) * active.freq + note) / (active.end - active.start + 2);
                    }
                }
            }
        }

        for (int i = 0; i < result2.size(); i++) {
            List<Integer> pitches = new ArrayList();
            for (int x = result2.get(i).start; x <= result2.get(i).end; x++) {
                pitches.addAll(notes[x]);
            }
            if (!pitches.isEmpty()) {
                Collections.sort(pitches);
                result2.get(i).freq = pitches.get(pitches.size() / 2);
            }
        }
//        result = result2;
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

    public static int frequencyToNote(double freq) {
        return (int) Math.round(Math.log(freq / 440) / Math.log(2) * 12 + 49);
    }

    private static int max(double[] a) {
        int max = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > a[max]) {
                max = i;
            }
        }
        return max;
    }

    public static double mean(List<Integer> a) {
        double sum = 0;
        for (int i : a) {
            sum += i;
        }
        return sum / a.size();
    }
}
