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

    public DataChunk32(List<Note> notes) {
        // initialize the chunk
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data = new ArrayList<>();

        for (Note note : notes) {
            addNote(note);
        }
    }

    public DataChunk32(Note note) {
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data  = new ArrayList<>();

        addNote(note);
    }

    public DataChunk32() {
        this.dwChunkSize = 0;
        this.fdata = new ArrayList<>();
        this.data  = new ArrayList<>();
    }

    // create a new byte array adding the new note
    // mutation! (for efficiency)
    public void addNote(Note note) {
        float[] nfdata = note.toData32();

        ByteBuffer bdata = ByteBuffer.allocate(data.size() + nfdata.length * 4);

        for (int i = 0; i < nfdata.length; i++) {
            bdata.put(toLE(nfdata[i]));
            fdata.add(nfdata[i]);
        }

        byte[] bytes = bdata.array();

        for (int i = 0; i < bytes.length; i++)
            data.add(bytes[i]);

        this.dwChunkSize = data.size();
    }

    public byte[] Write() {
        ByteBuffer buffer = ByteBuffer.allocate(8 + data.size());

        buffer.put(sGroupID.getBytes());
        buffer.put(toLE((int)dwChunkSize));

        for (int i = 0; i < data.size(); i++) {
            buffer.put(data.get(i));
        }

        return buffer.array();
    }

    /*
    public byte[] constructNote(Note note) {
        int fileSize = 12 + 24 + 8 + this.dwChunkSize * this.dataSize;

        System.out.println("file size = " + fileSize);
        ByteBuffer data = ByteBuffer.allocate(fileSize);

        Header hdr = new Header();
        hdr.dwFileLength = fileSize - 8;

        Format fmt = new Format();

        data.put(hdr.Write());
        data.put(fmt.Write());

        data.put(sGroupID.getBytes());
        data.putLong(Integer.toUnsignedLong(dwChunkSize));

        for (int i = 0; i < dwChunkSize; i++) {
            data.putFloat(data32[i]);
        }

    }
    */


    // test size
    public static void main(String[] args) {
        Note note = new Note(60, 1, 0.5, 60, 44100);
        List<Note> notes = new ArrayList<>();

        notes.add(note);
        DataChunk data = new DataChunk32(notes);

//        System.out.println(Arrays.toString(data.Write()));
    }

}
