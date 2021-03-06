package soundconverter.wavfile;

/*
 * Format class
 */
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

class Format {
    public static final int sGroupID = 0x666D7420;
    public long dwChunkSize = 16L;
    public int wFormatTag;
    public int wChannels;
    public long dwSamplesPerSec;
    public long dwAvgBytesPerSec;
    public int wBitsPerSample;
    public int wBlockAlign;

    // TODO: get constants on construction
    // 16, 1, 2, 22100,16
    public Format(int wFormatTag, int wChannels, long
            dwSamplesPerSec, int wBitsPerSample) {
        this.wFormatTag = wFormatTag;
        this.wChannels = wChannels;
        this.dwSamplesPerSec = dwSamplesPerSec;
        this.wBitsPerSample = wBitsPerSample;

        this.dwAvgBytesPerSec = this.dwSamplesPerSec * this.wBlockAlign;
        this.wBlockAlign = wChannels * wBitsPerSample / 8;
    }

    private byte[] toLE(int num) {
        ByteBuffer temp = ByteBuffer.allocate(4);
        temp.order(ByteOrder.LITTLE_ENDIAN);
        temp.putInt(num);

        return temp.array();
    }

    private byte[] toLE(short num) {
        ByteBuffer temp = ByteBuffer.allocate(2);
        temp.order(ByteOrder.LITTLE_ENDIAN);
        temp.putShort(num);
        return temp.array();
    }

    public byte[] Write() {
        ByteBuffer buffer = ByteBuffer.allocate(24);

        buffer.putInt(sGroupID);
        buffer.put(toLE((int)dwChunkSize));
        buffer.put(toLE((short)wFormatTag));
        buffer.put(toLE((short)wChannels));
        buffer.put(toLE((int)dwSamplesPerSec));
        buffer.put(toLE((int)dwAvgBytesPerSec));
        buffer.put(toLE((short)wBlockAlign));
        buffer.put(toLE((short)wBitsPerSample));

        return buffer.array();
    }

    // TODO
    public void Write(DataOutputStream out) {

    }

    public static void main(String[] args) {
        Format fmt = new Format(1, 2, 22100, 16);

//        System.out.println(Arrays.toString(fmt.sGroupID.getBytes()));
        System.out.println(fmt.dwChunkSize);
        System.out.println(Arrays.toString(fmt.Write()));
//        fmt.toLE(20);
    }
}
