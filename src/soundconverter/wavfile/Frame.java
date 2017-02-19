package soundconverter.wavfile;

/**
 * Frame!
 */
public interface Frame {

    float[] toData32();
    short[]   toData16();
    byte[] toData8();
    String  toString();
}
