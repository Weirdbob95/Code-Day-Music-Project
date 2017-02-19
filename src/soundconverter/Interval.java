package soundconverter;

import graphics.Graphics2D;
import static soundconverter.SoundConverter.*;
import util.Color4;
import util.Vec2;

/**
 * Created by JWZ on 2/18/17.
 */
public class Interval {

    public int start, end;
    public double freq;

    public Interval(int start, int end, double freq) {
        this.start = start;
        this.end = end;
        this.freq = freq;
    }

    public void draw(double pixelSize) {
        Graphics2D.drawLine(new Vec2(start * pixelSize, noteToFrequency(freq / 10.) * bufferSize / sampleRate * pixelSize),
                new Vec2(end * pixelSize, noteToFrequency(freq / 10.) * bufferSize / sampleRate * pixelSize), Color4.ORANGE, 2);
        Graphics2D.drawLine(new Vec2(start, freq / 10.).multiply(pixelSize), new Vec2(end, freq / 10.).multiply(pixelSize), Color4.PURPLE, 2);

    }

    @Override
    public String toString() {
        return "Interval{" + "start=" + start + ", end=" + end + ", freq=" + freq + '}';
    }
}
