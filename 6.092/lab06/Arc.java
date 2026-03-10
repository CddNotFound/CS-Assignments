import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Arc implements Sprite {
    private int height;
    private int width;
    private int startAngle;
    private int arcAngle;

    /**
     * Create an arc.
     */
    public Arc(int width, int height, int startAngle, int arcAngle) {
        this.height = height;
        this.width = width;
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
    }

    public void draw(Graphics surface, int x, int y) {
        // Draw the object
//        surface.fillArc(x, y, height, width, startAngle, arcAngle);
        ((Graphics2D) surface).setStroke(new BasicStroke(3.0f));
        surface.drawArc(x, y, height, width, startAngle, arcAngle);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
