package editor;

import data.Note;
import engine.Core;
import engine.Input;
import graphics.Graphics2D;
import graphics.Window2D;
import graphics.data.Sprite;
import static graphics.loading.FontContainer.add;
import graphics.loading.SpriteContainer;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import soundconverter.MusicCreator;
import soundconverter.Recorder;
import static soundconverter.SoundConverter.*;
import soundconverter.wavfile.Instrument;
import soundconverter.wavfile.Piano;
import soundconverter.wavfile.WAV;
import util.Color4;
import util.Mutable;
import util.Vec2;

public class UIMain {

    private static final int SIZE = 500;
    private static final int TEXT_NUMBER = 50;
    public static final int VERTICALPOS = -600;
    public static List<Note> notesList;
    static int dwSamplePerSec = 44100;
    static int bpm = 60;

    public static void main(String[] args) {

        Core.init();
        Window2D.viewSize = new Vec2(1200, 1200);
        add("Font", "Cambria", Font.PLAIN, 40);
        add("Font2", "Cambria", Font.BOLD, 60);
        add("Font3", "Cambria", Font.PLAIN, 20);
        //Test fixed note, no edit function, no volume and length

        Window2D.viewPos = new Vec2(0, VERTICALPOS);
        notesList = new ArrayList<>();
        Instrument piano = new Piano();

        for (int i = 0; i < 30; i++) {
            notesList.add(new Note(piano, 60, 4, 0.5, bpm, dwSamplePerSec));
        }

        inputManager user = new inputManager(notesList, "Suite I");
        userInteraction(user);
        userInteraction(user, piano);
        drawState(user);

        Recorder r = new Recorder();
        Mutable<Boolean> recording = new Mutable(false);
        Input.whenKey(Keyboard.KEY_SPACE, true).onEvent(() -> {
            if (!recording.o) {
                try {
                    r.beginRecording(new File("sounds/recording"));
                    recording.o = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                r.endRecording();
                loadFile("sounds/recording");
                notesList = MusicCreator.toNoteList(result);
                recording.o = false;
            }
        });

        Core.run();
    }

    public static void drawState(inputManager user) {
        Core.render.onEvent(() -> {

            // Draw header
            Graphics2D.drawText(user.name, "Font", new Vec2(0, -5), Color.black);
            new Sprite("piano").draw(new Vec2(-SIZE + 40, 5), 0);
            new Sprite("trumpet").draw(new Vec2(-SIZE + 160, 5), 0);
            new Sprite("pause").draw(new Vec2(SIZE - 20, 5), 0);
            new Sprite("start").draw(new Vec2(SIZE - 140, 5), 0);

            int noteToDraw = 0;

            for (int i = 0;; i++) {

                if (noteToDraw >= notesList.size()) {
                    break;
                }

                // Draw staff lines
                int staffTop = -100 - i * 250;
                for (int j = 0; j < 5; j++) {
                    int y = staffTop - 20 * j;
                    Graphics2D.drawLine(new Vec2(-SIZE, y), new Vec2(SIZE, y), Color4.BLACK, 2);
                }
                // Draw Treble clef symbol
                new Sprite("G").draw(new Vec2(-SIZE + 30, -35 + staffTop), 0);

                // Draw notes
                int x = -350;
                while (true) {
                    if (noteToDraw >= notesList.size()) {
                        break;
                    }
                    Note n = notesList.get(noteToDraw);
                    int widthRequired = 60;
//                    if (isSharp(n.note)) {
//                        widthRequired += 10;
//                    }
                    int y = 10 * noteHeight(n.note) - 72 + staffTop;
                    if (x + widthRequired >= SIZE) {
                        break;
                    }
                    n.pos = new Vec2(x, y);
                    if (isSharp(n.note)) {
                        user.recognize(n).draw(n.pos.add(new Vec2(10, 0)), 0);
                        Graphics2D.drawSprite(SpriteContainer.loadSprite("sharp"), n.pos.add(new Vec2(-10, -10)), new Vec2(1), 0, Color4.WHITE);
                    } else {
                        user.recognize(n).draw(n.pos, 0);
                    }
                    // Draw extra staff lines
                    if (noteHeight(n.note) > 11) {
                        int count = noteHeight(n.note) / 2 - 5;
                        for (int j = 0; j < count; j++) {
                            int lineY = staffTop + 20 + j * 20;
                            Graphics2D.drawLine(new Vec2(n.pos.x - 15, lineY), new Vec2(n.pos.x + widthRequired - 45, lineY), Color4.BLACK, 2);
                        }
                    } else if (noteHeight(n.note) <= 0) {
                        int count = -noteHeight(n.note) / 2 + 1;
                        for (int j = 0; j < count; j++) {
                            int lineY = staffTop - 100 - j * 20;
                            Graphics2D.drawLine(new Vec2(n.pos.x - 15, lineY), new Vec2(n.pos.x + widthRequired - 45, lineY), Color4.BLACK, 2);
                        }
                    }
                    x += widthRequired;
                    noteToDraw++;
                }
            }
        });
    }

    public static boolean isSharp(int note) {
        switch (note % 12) {
            case 1:
            case 3:
            case 6:
            case 8:
            case 10:
                return true;
            default:
                return false;
        }
    }

    public static int noteHeight(int note) {
        if (note == -1) {
            return 3;
        }
        int noteHeight = 0;
        if (note >= 60) {
            for (int i = 1; i <= note - 60; i++) {
                if (!isSharp(i)) {
                    noteHeight++;
                }
            }
        } else if (note < 60) {
            for (int i = note + 1; i <= 60; i++) {
                if (!isSharp(i)) {
                    noteHeight--;
                }
            }
        }
        return noteHeight;
    }

    public static boolean isValidSelection(int selected) {
        return selected >= 0 && selected < notesList.size();
    }

    public static void userInteraction(inputManager user) {
        Input.whenMouse(0, true).onEvent(() -> {
            Vec2 click = Input.getMouse();
//        System.out.print(click.x + 460 + " " + (click.y - 5));
            if (Math.abs(click.x + 420) <= 40 && (Math.abs(click.y + 5)) <= 40) {
                System.out.println(click.x + ",piano" + click.y);
//                userInteraction(user, new Piano());
                WAV.genWAE((List) notesList, "1234", 44100, 8);
                playFile("1234");
            } else if (Math.abs(click.x + 330) <= 40 && (Math.abs(click.y + 5)) <= 40) {
//                userInteraction(user, new Piano());
                System.out.println(click.x + ",trumpet" + click.y);
            } else if (Math.abs(click.x + 260) <= 20 && (Math.abs(click.y + 20)) <= 20) {
                user.verse++;
            } else {
//                userInteraction(user, new Piano());
            }
        });
    }

    public static void userInteraction(inputManager user, Instrument instru) {
        Mutable<Integer> selected = new Mutable(0);
        Input.mouseWheel.forEach(x -> {
            if (x < 0) {
                if ((-notesList.size() / 10 + 3) * 150 - 50 < Window2D.viewPos.y) {
                    Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, -50));
                }
            }
            if (x > 0) {
                if (Window2D.viewPos.y <= -540) {
                    Window2D.viewPos = Window2D.viewPos.add(new Vec2(0, 50));
                }
            }
        });
        // Draws a red box around the selected note
        Core.render.onEvent(() -> {
            if (isValidSelection(selected.o)) {
                Graphics2D.fillRect(notesList.get(selected.o).pos.subtract(new Vec2(30, 60)), new Vec2(60, 120), Color4.RED.withA(.1));
            }
        });
        //inaccuracy
        Input.whenMouse(0, true).onEvent(() -> {
            Vec2 click = Input.getMouse();

            for (int i = 0; i < notesList.size(); i++) {
                if (click.x <= notesList.get(i).pos.x + 30 && click.x >= notesList.get(i).pos.x - 30
                        && click.y <= notesList.get(i).pos.y + 60 && click.y >= notesList.get(i).pos.y - 60) {
                    selected.o = i;
                }
            }
        });

        Input.whenKey(Keyboard.KEY_DOWN, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                if (notesList.get(selected.o).note != -1) {
                    notesList.get(selected.o).note -= 1;
                }
            }
        });
        Input.whenKey(Keyboard.KEY_UP, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                if (notesList.get(selected.o).note != -1) {
                    notesList.get(selected.o).note += 1;
                }
            }
        });
        Input.whenKey(Keyboard.KEY_LEFT, true).onEvent(() -> {
            selected.o--;
        });
        Input.whenKey(Keyboard.KEY_RIGHT, true).onEvent(() -> {
            selected.o++;
        });
        Input.whenKey(Keyboard.KEY_R, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                if (notesList.get(selected.o).note != -1) {
                    notesList.get(selected.o).note = -1;
                } else {
                    notesList.get(selected.o).note = 60;
                }
            }
        });
        Input.whenKey(Keyboard.KEY_BACK, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                notesList.remove((int) selected.o);
            }
        });
        Input.whenKey(Keyboard.KEY_1, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                notesList.add((int) selected.o, new Note(instru, 60, 1, 0, bpm, dwSamplePerSec));
            }
        });
        Input.whenKey(Keyboard.KEY_2, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                notesList.add((int) selected.o, new Note(instru, 60, 2, 0, bpm, dwSamplePerSec));
            }

        });
        Input.whenKey(Keyboard.KEY_4, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                notesList.add((int) selected.o, new Note(instru, 60, 4, 0, bpm, dwSamplePerSec));
            }
        });
        Input.whenKey(Keyboard.KEY_8, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                notesList.add((int) selected.o, new Note(instru, 60, 8, 0, bpm, dwSamplePerSec));
            }
        });
        Input.whenKey(Keyboard.KEY_0, true).onEvent(() -> {
            if (isValidSelection(selected.o)) {
                notesList.add((int) selected.o, new Note(instru, 60, 16, 0, bpm, dwSamplePerSec));
            }
        });

    }

}
