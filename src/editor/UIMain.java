package editor;

import engine.Core;

public class UIMain {

    public static void main(String[] args) {

        Core.init();

        Core.render.onEvent(() -> {

            // Drawing code can go here
        });

        Core.run();
    }
}
