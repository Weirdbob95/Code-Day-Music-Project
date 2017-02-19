package soundconverter.wavfile;

/**
 * Frame!
 */
public interface Frame {

    float[] toData32();
    int[]   toData16();
    short[] toData8();
    String  toString();
}
