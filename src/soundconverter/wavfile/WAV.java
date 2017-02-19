package soundconverter.wavfile;

import data.Note;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WAV {
    public DataChunk dataChunk;
    public Header    header;
    public Format    format;

//    public static final int BUFFER_SIZE = 4096;
//    public byte[]    buffer = new byte[BUFFER_SIZE];
//    public byte[]    data;

    // given a list of notes, construct the data chunk, headers and format
    public WAV(List<Frame> frames, int wFormatTag, int wChannels, long
            dwSamplesPerSec, int wBitsPerSample) {

        if (wBitsPerSample == 32) {
            this.dataChunk = new DataChunk32(frames);
            this.header    = new Header(44 + ((DataChunk32)this.dataChunk).fdata.size() * 4 - 8);
        } else if (wBitsPerSample == 16) {
            this.dataChunk = new DataChunk16(frames);
            this.header    = new Header(44 + ((DataChunk16)this.dataChunk).fdata.size() * 2 - 8);
        } else if (wBitsPerSample == 8) {
            this.dataChunk = new DataChunk8(frames);
            this.header    = new Header(44 + ((DataChunk8)this.dataChunk).fdata.size() - 8);
        } else {
            throw new IllegalArgumentException("wrong wBitsPerSample");
        }

        this.format    = new Format(wFormatTag, wChannels,dwSamplesPerSec,wBitsPerSample);
    }

    // for debug
    public WAV(int wFormatTag, int wChannels, long dwSamplesPerSec, int wBitsPerSample) {
        this.dataChunk = new DataChunk32();
        this.format    = new Format(1, 2, 22100,16);
        this.header    = new Header(2084);
    }

    public void WriteAll(DataOutputStream out) {
//        if (data == null)  genData();

        try {
            out.write(this.header.Write());
            out.write(this.format.Write());
            out.write(this.dataChunk.Write());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printHeader() {
        System.out.println(Arrays.toString(this.header.Write()));
    }

    public void printFormat() {
        System.out.println(Arrays.toString(this.format.Write()));
    }

    public void write(String filename) {
        try {
            FileOutputStream ostream = new FileOutputStream(new File(filename));

            DataOutputStream out = new DataOutputStream(ostream);

            WriteAll(out);

            if (out != null) out.close();
            if (ostream != null) ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void genWAE(List<Frame> frames, String filename, int dwSamplePerSec) {
        WAV wav = new WAV(frames,1, 1, dwSamplePerSec,16);

        wav.printHeader();
        wav.printFormat();
        wav.write(filename);
    }


    public static void main(String[] args) {
        List<Frame> frames = new ArrayList<>();
        Note note;

        Instrument piano = new Piano();

        for (int i = 0; i < 10; i++) {
            note = new Note(piano, 60 + i, 4, 0.5, 60, 44100);
            System.out.println("\tadding..\n" + note);
            frames.add(note);
        }

//        for (int i = 0; i < 10; i++) {
//            note = new Note(piano, -1, 4, 0.5, 60, 44100);
//            System.out.println("\tadding..\n" + note);
//            frames.add(note);
//        }

        genWAE(frames, "mywave.wav", 44100);
    }
}
