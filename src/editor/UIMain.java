package editor;

import engine.AbstractEntity.LAE;
import engine.Core;
import engine.EventStream;
import engine.Input;
import engine.Signal;
import examples.Premade2D;
import graphics.Graphics2D;
import graphics.Window2D;
import static java.util.Comparator.comparingDouble;
import java.util.Optional;
import java.util.function.Supplier;
import org.lwjgl.opengl.Display;
import util.Color4;
import static util.Color4.*;
import util.RegisteredEntity;
import util.Vec2;
public class UIMain {
    private static final int SIZE = 100;
    private static final int TEXT_LENGTH = 8;
    private class Note {

        public int note; // 60 is middle C, 61 is C-sharp, 62 is D, 63 is D-sharp, 64 is E, 65 is F, etc
        public int time; // n means play for 1/nth of a measure, for example, normal quarter notes are time=4
        public double volume; // On a scale from 0 to 1, where 0 is the quietest and 1 is the loudest

        private Note(int note, int time, double volume) {
            this.note = note;
            this.time = time;
            this.volume = volume;
        }
    }
        public static void main(String[] args) {

            Core.init();

            Core.render.onEvent(() -> {
                
                for (int i = 0; i < TEXT_LENGTH; i++){
                Graphics2D.drawLine(new Vec2(-1 * SIZE, i * 80), new Vec2(SIZE, i * 80), Color4.BLACK, 60);
                // Drawing code can go here
                }
                });

            Core.run();
        }

        public void recognize(Note n) {

        }
    }
