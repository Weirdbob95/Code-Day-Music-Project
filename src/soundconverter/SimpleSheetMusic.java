package soundconverter;

import data.Note;
import java.io.File;
import java.io.IOException;
import java.util.List;
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
}
