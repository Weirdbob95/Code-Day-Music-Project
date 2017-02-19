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
    public DataChunk32 dataChunk;
    public Header    header;
    public Format    format;

//    public static final int BUFFER_SIZE = 4096;
//    public byte[]    buffer = new byte[BUFFER_SIZE];
//    public byte[]    data;

    // given a list of notes, construct the data chunk, headers and format
    public WAV(List<Frame> frames, int wFormatTag, int wChannels, long
            dwSamplesPerSec, int wBitsPerSample) {
        this.dataChunk = new DataChunk32(frames);
        this.format    = new Format(wFormatTag, wChannels,dwSamplesPerSec,wBitsPerSample);
        this.header    = new Header(44 + this.dataChunk.fdata.size() * 4 - 8);
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
        WAV wav = new WAV(frames,1, 1, dwSamplePerSec,32);

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

        genWAE(frames, "mywave.wav", 44100);
    }
}
