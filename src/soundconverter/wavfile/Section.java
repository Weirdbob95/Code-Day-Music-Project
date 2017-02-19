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

        float[] temp = notes.get(0).toData32();

        float[] buffer = new float[temp.length];

        for (int i = 0; i < buffer.length; i++)
            buffer[i] = temp[i];

        for (int i = 1; i < notes.size(); i++) {
            temp = notes.get(i).toData32();
            for (int j = 0; j < buffer.length; j++)
                buffer[j] += temp[j];
        }

        return buffer;
    }

    public short[] toData16() {
        if (notes.isEmpty()) {
            throw new IllegalStateException("empty section!");
        }

        short[] temp = notes.get(0).toData16();

        short[] buffer = new short[temp.length];

        for (int i = 0; i < buffer.length; i++)
            buffer[i] = temp[i];

        for (int i = 1; i < notes.size(); i++) {
            temp = notes.get(i).toData16();
            for (int j = 0; j < buffer.length; j++)
                buffer[j] += temp[j];
        }

        return buffer;
    }

    public byte[] toData8() {
        if (notes.isEmpty()) {
            throw new IllegalStateException("empty section!");
        }

        byte[] temp = notes.get(0).toData8();

        byte[] buffer = new byte[temp.length];

        for (int i = 0; i < buffer.length; i++)
            buffer[i] = temp[i];

        for (int i = 1; i < notes.size(); i++) {
            temp = notes.get(i).toData8();
            for (int j = 0; j < buffer.length; j++)
                buffer[j] += temp[j];
        }

        return buffer;
    }

    public String toString() {
        return notes.toString();
    }
}
