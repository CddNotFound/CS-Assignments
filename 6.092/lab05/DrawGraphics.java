import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class DrawGraphics {
    ArrayList<BouncingBox> boxes;
    
    /** Initializes this class for drawing. */
    public DrawGraphics() {
        boxes = new ArrayList<BouncingBox>();

        boxes.add(new BouncingBox(200, 300, Color.RED, 1, 4));
        boxes.add(new BouncingBox(100, 50, Color.GREEN, 2, 3));
        boxes.add(new BouncingBox(50, 250, Color.YELLOW, 3, 0));

    }

    /** Draw the contents of the window on surface. Called 20 times per second. */
    public void draw(Graphics surface) {
//        surface.drawLine(50, 50, 250, 250);
        surface.drawArc(50, 50, 50, 50, 100, 210);
        surface.drawArc(200, 50, 50, 50, 100, 210);
        surface.draw3DRect(120, 120, 80, 80, false);
        surface.drawOval(135, 80, 30, 10);

        for (BouncingBox box : boxes) {
            box.draw(surface);
        }
    }
} 