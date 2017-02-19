package data;


import soundconverter.wavfile.DataChunk;

// A class that represents a single note. Feel free to change this as needed.
public class Note {

    public int bpm;
    public int note; // 60 is middle C, 61 is C-sharp, 62 is D, 63 is D-sharp, 64 is E, 65 is F, etc
    // n means play for 1/nth of a measure, for example, normal quarter notes are time=4
    // time duration for time n = .5^(n - 3) * 60 / bpm
    public int time;
    public double volume; // On a scale from 0 to 1, where 0 is the quietest and 1 is the loudest

    private float maxAmp32 = Float.MAX_VALUE;
    private int dwSamplePerSec;

    public Note(int note, int time, double volume, int bpm, int dwSamplePerSec) {
        this.note = note;
        this.time = time;
        this.volume = volume;
        this.bpm = bpm;
        this.dwSamplePerSec = dwSamplePerSec;
    }

    public float[] toData32() {
        // TODO: note -> freq
        return toData32(440);
    }

    // returns data for that note (for 32-bit audio)
    public float[] toData32(double freq) {
        int sampleDataSize = dwSamplePerSec * (int)(Math.pow(2, (-this.time + 3)) * 60 / bpm);

//        System.out.println("sampleDataSize = " + sampleDataSize);
        float[] data = new float[sampleDataSize];

        double amplitude = this.volume * Float.MAX_VALUE;

        double t = (Math.PI * freq * 2) / dwSamplePerSec;

        for (int i = 0; i < sampleDataSize; i++) {
            data[i] = (float)(amplitude * Math.sin(t * i));
        }

        return data;
    }

    public void printData() {
        float[] data = this.toData32();
        for (int i = 0; i < data.length; i++) {
            System.out.println(" " + data[i]);
        }
    }

    public static void main(String[] args) {
        Note note = new Note(60, 1, 0.5, 60, 44100);
//        note.printData();
    }
}
