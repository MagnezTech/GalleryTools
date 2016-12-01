import java.awt.*;

/**
 * Created by mgwozdek on 2016-11-29.
 */
public class Test {
    public static void main(String[] args) {
        int distance = 10000;
        int angle = 10;
        System.out.println(Math.sqrt(2 * distance * distance * (1 - Math.cos(Math.toRadians(angle)))));
    }
}
