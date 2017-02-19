package soundconverter.wavfile;

/*
 * Header Class
 */

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

class Header {
    public long dwFileLength;
    public static final String sGroupID = "RIFF";
    public static final String sRIFFType = "WAVE";

    public Header(long dwFileLength) {
        this.dwFileLength = dwFileLength;
    }

    public Header() {
        this(0);
    }

    private byte[] toLE(int num) {
        ByteBuffer temp = ByteBuffer.allocate(4);
        temp.order(ByteOrder.LITTLE_ENDIAN);
        temp.putInt(num);

        return temp.array();
    }

    public byte[] Write() {
        ByteBuffer buffer = ByteBuffer.allocate(12);

        buffer.put(sGroupID.getBytes());
        buffer.put(toLE((int)dwFileLength));
        buffer.put(sRIFFType.getBytes());

        return buffer.array();
    }

    // TODO
    public void Write(DataOutputStream out) {

    }

    private static void printBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            System.out.println(bytes[i]);
        }
    }

    private static void swap(byte[] arr, int i, int j) {
        byte temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // mutation!
    private static byte[] reverse(byte[] bytes) {
        for (int i = 0; i < bytes.length/ 2; i++) {
            swap(bytes, i, bytes.length - i - 1);
        }
        return bytes;
    }

    public void printLength() {
        byte[] bytes = toLE((int) (dwFileLength << 16));

        for (int i = 0; i < bytes.length; i++) {
            System.out.println(Byte.toString(bytes[i]));
        }
    }

    public static void main(String[] args) {
        printBytes(reverse("abcd".getBytes()));
        Header hdr = new Header(705636L);
        hdr.printLength();
//        hdr.dwFileLength = Long.MAX_VALUE;
//        System.out.println(Arrays.toString(hdr.Write()));
////        System.out.println(Long.toUnsignedString(hdr.dwFileLength));
////        System.out.println(Long.MAX_VALUE);
    }
}
