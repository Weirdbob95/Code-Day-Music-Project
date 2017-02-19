package soundconverter.wavfile;

import data.Note;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The class for a single chunk of data
 */
public class DataChunk32 extends DataChunk {

    public ArrayList<Float> fdata;
    public ArrayList<Byte>  data;

    public static final float MAX_AMP = Float.MAX_VALUE;
    public static final float MIN_AMP = Float.MIN_VALUE;

    public DataChunk32(List<Frame> frames) {
        // initialize the chunk
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data = new ArrayList<>();

        for (Frame f : frames) {
            addFrame(f);
        }
    }

    public DataChunk32(Frame frame) {
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data  = new ArrayList<>();

        addFrame(frame);
    }

    public DataChunk32() {
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data  = new ArrayList<>();
    }

    // create a new byte array adding the new frame (note or section)
    // WARNING: mutation happens! (for efficiency)
    public void addFrame(Frame note) {
        float[] nfdata = note.toData32();

        for (int i = 0; i < nfdata.length; i++)
            fdata.add(nfdata[i]);

        this.dwChunkSize = fdata.size();
    }

    public byte[] Write() {
        ByteBuffer buffer = ByteBuffer.allocate(8 + fdata.size() * 4);

        buffer.putInt(sGroupID);
        buffer.put(toLE((int)dwChunkSize));

        for (int i = 0; i < fdata.size(); i++) {
            buffer.put(toLE(fdata.get(i)));
        }

        return buffer.array();
    }

    // test size
    public static void main(String[] args) {
        Note note = new Note(new Piano(), 60, 1, 0.5, 60, 44100);
        List<Frame> notes = new ArrayList<>();

        notes.add(note);
        DataChunk data = new DataChunk32(notes);

//        System.out.println(Arrays.toString(data.Write()));
    }

}
