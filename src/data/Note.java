package data;
import java.util.*;
import util.Vec2;
// A class that represents a single note. Feel free to change this as needed.
public class Note {

    public int note; // 60 is middle C, 61 is C-sharp, 62 is D, 63 is D-sharp, 64 is E, 65 is F, etc
    public int time; // n means play for 1/nth of a measure, for example, normal quarter notes are time=4
    public double volume; // On a scale from 0 to 1, where 0 is the quietest and 1 is the loudest
    public Vec2 pos; // paper position
    
    public Note(int note, int time, double volume) {
        this.note = note;
        this.time = time;
        this.volume = volume;
        
    }
}
