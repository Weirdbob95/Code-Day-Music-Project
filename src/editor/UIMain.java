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
   	  int[] images = {1, 2, 4, 8, 16};
        Core.init();
        List<Texture> images;
        for(int i = 0; i < notesList.length;i++){
      	  images.add(SpriteContainer.loadImage(noteist[i]));
        }
        
        Core.render.onEvent(() -> {
      	  
      	  Graphics2D.drawSprite(noteImage, new Vec2(0, 5), new Vec2(1), 0, Color4.WHITE);

            for (int i = 0; i < TEXT_LENGTH; i++) {
                for (int j = 0; j < 5; j++) {

                    Graphics2D.drawLine(new Vec2(-1 * SIZE, i * 80), new Vec2(1), Color4.RED, 2);
                    // Drawing code can go here
                    
                }
                
            }
            Note[] result = .....();
            int notesEachLine = TEXT_LENGTH/(imageSize + 10);
            for(int i = 0; i < result.length; i += notesEachLine){
	            for(int j = 0; j < notesEachLine - 1; j++){
	            	int xPos = iP + j * (pL + 20);
	            	int yPos = iP + i * 150 - (result[i].note - 57) * 10);
	            	
            		int y = result[j].note;
            		while(y >= 60){
            			Graphics2D.drawLine(new Vec2(xPos, yPos), new Vec2(xPos + 30, yPos), Color4.WHITE, 2);
            			y --;
            		}
	           	 	
            		Graphics2D.drawSprite(images[result[i * notesEachLine+j].time], 
            				new Vec2(xPos, yPos, new Vec2(1,1), 0, Color4.WHITE);
	            }
            }
            
            for(int i = result.length - result.length % notesEachLine; i < result.length; i++){
            	int xPos = iP + i * (pL + 20);
            	int yPos = iP + result.length / notesEachLine * 150 - (result[i].note - 57) * 10);
            	int y = result[i].note;
         		while(y >= 60){
         			Graphics2D.drawLine(new Vec2(xPos, yPos), new Vec2(xPos + 30, yPos), Color4.WHITE, 2);
         			y --;
         		}
            	Graphics2D.drawSprite(images[result[i].time], 
         				new Vec2(iP +  i * (pL + 20), iP + result.length / notesEachLine * 150 - (result[i].note - 57)*10),
         				new Vec2(1,1), 0, Color4.WHITE);
            }
        });

        Core.run();
    }

    public void recognize(Note n) {

    }
    
    
    
}
