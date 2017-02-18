/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editor;

import data.Note;
import graphics.data.Sprite;
import java.util.List;

/**
 *
 * @author liruiwang
 */
public class inputManager {
    private List<Note> list;
    private String pitch;
    private String name;
    private int topLine;
    public inputManager (List<Note> list, String pitch, String name) {
        this.list = list;
        this.pitch = pitch;
        this.name = name;  
        this.topLine = 0;
        
    }
        public Sprite recognize(Note n) {
            int pic = n.note - 60;
            //if (pic == 1)
            //else if (pic
            return new Sprite("note1");
    }
}
