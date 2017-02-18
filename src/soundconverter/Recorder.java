package soundconverter;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 * A Recorder takes input from the microphone and saves the audio as a monaural
 * wave file.
 *
 * @author Kenneth J. Goldman, adapted in part from code by Ming Chow
 * @version 1.0
 */
class Recorder {

    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, true);
    private TargetDataLine microphone;
    private Thread session;

    /**
     * Starts recording to the given file.
     *
     * @param soundFile the new .wav file in which to save the sound clip
     * @throws IOException when the file cannot not be written
     * @throws LineUnavailableException when the microphone is not accessible
     */
    public synchronized void beginRecording(final File soundFile) throws IOException, LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new IOException("Line type not supported: " + info);
        }
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format, microphone.getBufferSize());
        session = new Thread(() -> {
            AudioInputStream sound = new AudioInputStream(microphone);
            microphone.start();
            try {
                AudioSystem.write(sound, fileType, soundFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        session.start();
    }

    /**
     * Terminates the current recording session.
     */
    public synchronized void endRecording() {
        microphone.stop();
        microphone.close();
        if (session != null) {
            try {
                session.join();
            } catch (InterruptedException e) {
            }
            session = null;
        }
        notify();
    }

    /**
     * Waits until the current recording session, if any, has terminated.
     */
    public synchronized void waitForEnd() {
        while (session != null) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
}
