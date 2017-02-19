package soundconverter.wavfile;

import data.Note;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The class for a single chunk of data
 */
public class DataChunk16 extends DataChunk {

    // fdata for frame data
    public ArrayList<Short> fdata;
    public ArrayList<Byte>  data;

    public static final float MAX_AMP = Short.MAX_VALUE;
    public static final float MIN_AMP = Short.MIN_VALUE;

    public DataChunk16(List<Frame> frames) {
        // initialize the chunk
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data = new ArrayList<>();

        for (Frame f : frames) {
            addFrame(f);
        }
    }

    public DataChunk16(Frame frame) {
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data  = new ArrayList<>();

        addFrame(frame);
    }

    public DataChunk16() {
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data  = new ArrayList<>();
    }

    // create a new byte array adding the new frame (note or section)
    // WARNING: mutation happens! (for efficiency)
    public void addFrame(Frame note) {
        short[] nfdata = note.toData16();

        for (int i = 0; i < nfdata.length; i++)
            fdata.add(nfdata[i]);

        this.dwChunkSize = fdata.size();
    }

    public byte[] Write() {
        ByteBuffer buffer = ByteBuffer.allocate(8 + fdata.size() * 2);

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
        DataChunk data = new DataChunk16(notes);

//        System.out.println(Arrays.toString(data.Write()));
    }

}
