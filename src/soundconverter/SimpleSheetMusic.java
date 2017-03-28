package soundconverter;

import data.Note;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.midi.*;
import javax.sound.sampled.LineUnavailableException;
import static soundconverter.SoundConverter.loadFile;
import static soundconverter.SoundConverter.result;

public class SimpleSheetMusic {

    private static Recorder r = new Recorder();

    public static void startRecording() {
        try {
            r.beginRecording(new File("sounds/recording"));
        } catch (IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    public static List<Note> finishRecording() {
        r.endRecording();
        loadFile("sounds/recording");
        return MusicCreator.toNoteList(result);
    }

    public static void playListOfNotes(List<Note> music, int instrument) {
        new Thread(() -> {
            try {
                /* Create a new Sythesizer and open it. Most of
         * the methods you will want to use to expand on this
         * example can be found in the Java documentation here:
         * https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Synthesizer.html
                 */
                Synthesizer midiSynth = MidiSystem.getSynthesizer();
                midiSynth.open();

                //get and load default instrument and channel lists
                Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
                for (Instrument i : instr) {
                    System.out.println(i);
                }
                MidiChannel[] mChannels = midiSynth.getChannels();

                midiSynth.loadInstrument(instr[instrument]);//load an instrument

                mChannels[0].programChange(instr[instrument].getPatch().getProgram());

                for (Note n : music) {
                    if (n.note != -1) {
                        mChannels[0].noteOn(n.note, 100);//On channel 0, play note number 60 with velocity 100
                    }
                    try {
                        Thread.sleep(3000 / n.time); // wait time in milliseconds to control duration
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (n.note != -1) {
                        mChannels[0].noteOff(n.note);//turn of the note
                    }
                }

                midiSynth.close();
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }).run();
    }

    public static void main(String[] args) {
        playListOfNotes(null, 0);
    }
}
