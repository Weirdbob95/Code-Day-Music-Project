package editor;

import engine.Core;
import engine.Input;
import graphics.Graphics2D;
import graphics.Window2D;
import static graphics.loading.FontContainer.add;
import java.awt.Font;
import org.newdawn.slick.Color;
import util.Color4;
import util.Vec2;

public class UIMain {

    private static final int SIZE = 500;
    private static final int TEXT_NUMBER = 1000;
      

    private class Note{
        
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
        String pitch = "G";
        String name = "Suite I";
        String initial = "G:#";
        int verticalPos = -600;
        Core.init();
        Window2D.viewSize = new Vec2(1200, 1200);
        add("Font", "Cambria", Font.PLAIN, 40);
        add("Font2", "Cambria", Font.BOLD, 60);
        Window2D.viewPos = new Vec2(0, verticalPos);
        Core.render.onEvent(() -> {
            Graphics2D.drawText(name,"Font", new Vec2(0, -5),Color.black);
            Graphics2D.drawText(pitch,"Font", new Vec2(-510, -30),Color.black);
            for (int i = 0; i < TEXT_NUMBER; i++) {
                for (int j = 0; j < 5; j++) {
                    Graphics2D.drawLine(new Vec2(-1 * SIZE, 
                           -100 - i * 150 - j * 20), new Vec2(SIZE, -100 - 
                                    i * 150 - j * 20), Color4.BLACK, 2);
                }
                Graphics2D.drawText(initial,"Font2", new Vec2(-1 * SIZE, -95 - i * 150),Color.black);
            }
        });
        Input.whenMouse(1, true).onEvent(() -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, -50)));
        if (Window2D.viewPos.y <= 0) {
            Input.whenMouse(0, true).onEvent(() -> Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, 50)));
        }
        Core.run();
    }
//   public void readInput(InputManager user) {

//}


}
