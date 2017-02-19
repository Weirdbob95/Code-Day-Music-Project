package editor;

import data.Note;
import engine.Core;
import engine.Input;
import graphics.Graphics2D;
import graphics.Window2D;
import graphics.data.Sprite;
import static graphics.loading.FontContainer.add;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import util.Color4;
import util.Mutable;
import util.Vec2;

public class UIMain {

    private static final int SIZE = 500;
    private static final int TEXT_NUMBER = 50;
    public static final int VERTICALPOS = -600;
    public static List<Note> list;

    public static void main(String[] args) {

        Core.init();
        Window2D.viewSize = new Vec2(1200, 1200);
        add("Font", "Cambria", Font.PLAIN, 40);
        add("Font2", "Cambria", Font.BOLD, 60);
        add("Font3", "Cambria", Font.PLAIN, 20);
        //Test fixed note, no edit function, no volume and length

        Window2D.viewPos = new Vec2(0, VERTICALPOS);
        list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new Note(59, 1, 0.5));

        }
        inputManager user = new inputManager(list, "G", "Suite I");
        userInteraction(user);
        drawState(user);
        
        Core.run();
    }

    public static int curPos(int i, int j) {
        return -100 - i * 150 - 20 * j;
    }

    public static void drawState(inputManager user) {
        int length = user.list.size();
        Core.render.onEvent(() -> {
            
            Graphics2D.drawText(user.name, "Font", new Vec2(0, -5), Color.black);
            Graphics2D.drawText(user.pitch, "Font", new Vec2(-510, -30), Color.black);
            for (int i = 0; i < TEXT_NUMBER; i++) {

                for (int j = 0; j < 5; j++) {
                    Graphics2D.drawLine(new Vec2(-SIZE, curPos(i, j)),
                            new Vec2(SIZE, curPos(i, j)), Color4.BLACK, 2);

                }
                new Sprite("G").draw(new Vec2(-SIZE, -135 - i * 150), 0);
                int p = 0;

                for (p = 0; p < 10; p++) {//change the positions of each pic
                    if (10 * i + p < user.list.size()) {
                        int xPos = -300 + p * 80;

                        int yPos = -10 * (user.list.get(10 * i + p).note - 59) - i * 150 - 60;
                        if (user.list.get(10 * i + p).time == 1) {
                            yPos -= 32;
                        }
                        user.list.get(10 * i + p).pos = new Vec2(xPos, yPos);

                        user.recognize(user.list.get(10 * i + p)).draw(new Vec2(xPos, yPos), 0);

                    }
                }
            }
        });
    }

    public static void userInteraction(inputManager user) {
        Mutable<Integer> number = new Mutable(0);
        Input.mouseWheel.forEach(x -> {
            if (x < 0) {
                if ((-user.list.size() / 10 + 3) * 150 - 50 < Window2D.viewPos.y) {
                    Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, -50));
                }
            }
            if (x > 0) {
                if (Window2D.viewPos.y <= -540) {
                    Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, 50));
                }
            }
        });
        //inaccuracy
        Input.whenMouse(0, true).onEvent(() -> {
            Vec2 click = Input.getMouse();

                        for (int i = 0; i < list.size(); i++) {
                            if (click.x <= list.get(i).pos.x + 30 && click.x > list.get(i).pos.x - 30
                                    && click.y <= list.get(i).pos.y + 60 && click.y > list.get(i).pos.y - 60) {
                                System.out.println(list.get(i).pos + " " + i + " " + Input.getMouse());
                                number.o = i;
                            }
                        }
                        System.out.println(Input.getMouse());

                        System.out.print(number.o);
        });

        Input.whenKey(Keyboard.KEY_DOWN, true).onEvent(() -> {
            if (number.o < user.list.size()) {
                user.list.get(number.o).note += 1;
            }
        });
        Input.whenKey(Keyboard.KEY_UP, true).onEvent(() -> {
            if (number.o < user.list.size()) {
                user.list.get(number.o).note -= 1;
            }
        });

        Input.whenKey(Keyboard.KEY_BACK, true).onEvent(() -> {
            if (number.o < user.list.size()) {
                user.list.remove((int) number.o);
            }
        });
        Input.whenKey(Keyboard.KEY_1, true).onEvent(() -> {
            if (number.o < user.list.size()) {
                user.list.add((int) number.o, new Note(59, 1, 0));
            }
        });
        Input.whenKey(Keyboard.KEY_2, true).onEvent(() -> {
            if (number.o < user.list.size()) {
                user.list.add((int) number.o, new Note(59, 2, 0));
            }

        });
        Input.whenKey(Keyboard.KEY_4, true).onEvent(() -> {
            if (number.o < user.list.size()) {
                user.list.add((int) number.o, new Note(59, 4, 0));
            }
        });
        Input.whenKey(Keyboard.KEY_8, true).onEvent(() -> {
            if (number.o < user.list.size()) {
                user.list.add((int) number.o, new Note(59, 8, 0));
            }
        });
        Input.whenKey(Keyboard.KEY_0, true).onEvent(() -> {
            if (number.o < user.list.size()) {
                user.list.add((int) number.o, new Note(59, 16, 0));
            }
        });

    }
}
