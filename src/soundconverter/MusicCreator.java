package soundconverter;

import data.Note;
import engine.Core;
import graphics.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import static soundconverter.SoundConverter.jump;
import static soundconverter.SoundConverter.sampleRate;
import util.Color4;
import util.Vec2;

public class MusicCreator {

    public static List<Note> toNoteList(List<Interval> input) {

        double bestTempo = 0;
        double bestOffset = 0;
        double bestScore = 0;
        for (double tempo = .5; tempo < 1; tempo += .01) {
            for (double offset = 0; offset < tempo; offset += .01) {
                double score = 0;
                double checkTempo = tempo;
                for (Interval i : input) {
                    double diff = Math.abs((posToTime(i.start) - offset + checkTempo / 2) % checkTempo - checkTempo / 2);
                    score += 1 / (1 + diff);
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestTempo = tempo;
                    bestOffset = offset;
                }
            }
        }
        double fbt = bestTempo;
        double fbo = bestOffset;

        System.out.println(bestTempo + "  " + bestOffset);
        Core.render.onEvent(() -> {
            for (int i = 1; i < 50; i++) {
                double x = (fbo + fbt * i) * sampleRate / jump;
                Graphics2D.drawLine(new Vec2(x, 0).multiply(2), new Vec2(x, 800).multiply(2), Color4.BLUE, 1);
            }
        });
        TreeMap<Integer, Integer> pitch = new TreeMap();
        for (Interval i : input) {
            int startTime8 = (int) Math.round((posToTime(i.start) - bestOffset) / bestTempo * 2);
            int endTime8 = (int) Math.round((posToTime(i.end) - bestOffset) / bestTempo * 2 + .2);
            if (startTime8 != endTime8) {
                if (!pitch.containsKey(endTime8)) {
                    Integer endPitch = pitch.lowerKey(endTime8);
                    if (endPitch == null) {
                        endPitch = -1;
                    } else {
                        endPitch = pitch.get(endPitch);
                    }
                    pitch.put(endTime8, endPitch);
                }
                pitch.put(startTime8, (int) Math.round(i.freq / 10));
                int middle;
                while ((middle = pitch.higherKey(startTime8)) != endTime8) {
                    pitch.remove(middle);
                }
            }
            Core.render.onEvent(() -> {
                double x1 = (startTime8 * fbt / 2 + fbo) * sampleRate / jump;
                double x2 = (endTime8 * fbt / 2 + fbo) * sampleRate / jump;
                Graphics2D.drawLine(new Vec2(x1, 10).multiply(2), new Vec2(x2, 10).multiply(2), Color4.RED, 1);
            });
        }

        List<Note> r = new ArrayList();
        for (int startTime : pitch.keySet()) {
            Integer endTime = pitch.higherKey(startTime);
            if (endTime != null) {
                int duration = endTime - startTime;
                for (int i = 0; i <= 3; i++) {
                    int pow = (int) Math.pow(2, 3 - i);
                    while (duration >= pow) {
                        duration -= pow;
                        int p = pitch.get(startTime);
                        r.add(new Note(p == -1 ? -1 : p + 20, 8 / pow));
                    }
                }
//                    if (endTime - startTime == 1) {
//                        r.add(new Note(pitch.get(startTime), 8));
//                    } else if (endTime - startTime == 2) {
//                        r.add(new Note(pitch.get(startTime), 4));
//                    } else if (endTime - startTime == 4) {
//                        r.add(new Note(pitch.get(startTime), 2));
//                    } else if (endTime - startTime == 8) {
//                        r.add(new Note(pitch.get(startTime), 1));
//                    } else {
//                        for (int i = 0; i < endTime - startTime; i++) {
//                            r.add(new Note(pitch.get(startTime), 8));
//                        }
//                    }
            }
        }

        for (Note n : r) {
            System.out.println("Note: " + n.note + " " + n.time);
        }

        return r;
    }

    public static double posToTime(int pos) {
        return (double) pos * jump / sampleRate;
    }
}
