/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editor;

import data.Note;
import graphics.data.Sprite;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author liruiwang
 */
public class inputManager {

    public List<Note> list;
    public String pitch;
    public String name;
    public int topLine;

    public inputManager(List<Note> list, String name) {
        this.list = list;
        this.name = name;
        this.topLine = 0;

    }

    public Sprite recognize(Note n) {
        int pic = n.time;
        return new Sprite(pic + "");

    }
}
