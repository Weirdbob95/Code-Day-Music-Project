package soundconverter.wavfile;

import data.Note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Section!
 */
public class Section implements Frame {
    // invariant: all notes in the section have same the time duration
    public List<Note> notes;

    public Section(Note[] notesl) {
        this.notes = new ArrayList<>();

        for (int i = 0; i < notesl.length; i++)
            this.notes.add(notesl[i]);
    }

    public Section(Note note) {
        this(new Note[]{note});
    }

    public Section() {
        this(new Note[]{});
    }

    public float[] toData32() {
        if (notes.isEmpty()) {
            throw new IllegalStateException("empty section!");
        }

        float[] firstNote = notes.get(0).toData32();

        float[] buffer = new float[firstNote.length];

        for (int i = 0; i < buffer.length; i++)
            buffer[i] = firstNote[i];

        for (int i = 1; i < notes.size(); i++) {
            for (int j = 0; j < buffer.length; j++)
                buffer[j] += firstNote[j];
        }

        return buffer;
    }

    public int[] toData16() {
        throw new IllegalStateException("not implemented yet!");
    }

    public short[] toData8() {
        throw new IllegalStateException("not implemented yet!");
    }

    public String toString() {
        return notes.toString();
    }
}
