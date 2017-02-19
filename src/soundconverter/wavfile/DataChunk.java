package soundconverter.wavfile;

import data.Note;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * common interface for DataChunks
 * extended by DataChunk[size]
 */
public abstract class DataChunk {
    protected static final int sGroupID = 0x64617461;
    protected long dwChunkSize;

    protected ArrayList<Float> fdata;
    protected ArrayList<Byte>  data;

    public abstract void addFrame(Frame frame);

    public abstract byte[] Write();

    protected byte[] toLE(int num) {
        ByteBuffer temp = ByteBuffer.allocate(4);
        temp.order(ByteOrder.LITTLE_ENDIAN);
        temp.putInt(num);

        return temp.array();
    }

    protected byte[] toLE(short num) {
        ByteBuffer temp = ByteBuffer.allocate(2);
        temp.order(ByteOrder.LITTLE_ENDIAN);
        temp.putShort(num);

        return temp.array();
    }

    protected byte[] toLE(float num) {
        ByteBuffer temp = ByteBuffer.allocate(4);
        temp.order(ByteOrder.LITTLE_ENDIAN);
        temp.putFloat(num);

        return temp.array();
    }

    // test size
//    public static void main(String[] args) {
//        Header hdr = new Header();
//        System.out.println("Header size = " + hdr.Write().length);
//    }
}
