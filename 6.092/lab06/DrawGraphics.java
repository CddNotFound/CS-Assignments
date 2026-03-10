import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class DrawGraphics {
    ArrayList<Mover> movingSprite;

    /** Initializes this class for drawing. */
    public DrawGraphics() {
        movingSprite = new ArrayList<Mover>();

        Rectangle box = new Rectangle(15, 20, Color.RED);
        Mover movingModel = new Bouncer(100, 170, box);
        movingModel.setMovementVector(3, 1);
        movingSprite.add(movingModel);

        Arc arc = new Arc(50, 50, 200, 360); // :(
        movingModel = new Bouncer(200, 200, arc);
        movingModel.setMovementVector(-1, 3);
        movingSprite.add(movingModel);

        movingModel = new StraightMover(50, 50, new Rectangle(15, 30, Color.YELLOW));
        movingModel.setMovementVector(-2, 2);
        movingSprite.add(movingModel);

        movingModel = new StraightMover(100, 100, arc);
        movingModel.setMovementVector(3, -1);
        movingSprite.add(movingModel);
    }

    /** Draw the contents of the window on surface. */
    public void draw(Graphics surface) {
        for (Mover box : movingSprite) {
            box.draw(surface);
        }
    }
}
