import java.util.ArrayList;

class Solve {
    public static void main(String[] args) {
        Matrix luna = new Matrix();
        Matrix mercury = new Matrix();
        luna.read("/Users/kkk/CS/assignments/CS-Assignments/6.092/lab07/Luna.txt");
        mercury.read("/Users/kkk/CS/assignments/CS-Assignments/6.092/lab07/Mercury.txt");

        if (luna.check()) {
            System.out.println("Luna is a magic square.");
        }
        if (mercury.check()) {
            System.out.println("Mercury is a magic square.");
        }
    }
}