import java.awt.*;

/**
 * Created by mgwozdek on 2016-11-29.
 */
public class Test {
    public static void main(String[] args) {
//        double a1 = toAngle(new Point(0, 0), new Point(100, 100));
//        double a2 = toAngle(new Point(0, 0), new Point(100, 90));
//        System.out.println(a1-a2);
//        toVector(new Point(3600, 3600), -135);
        toAngle(new Point(0, 0), new Point(100, 0));
        toAngle(new Point(0, 0), new Point(90, 10));
        toAngle(new Point(0, 0), new Point(80, 20));
        toAngle(new Point(0, 0), new Point(70, 30));
        toAngle(new Point(0, 0), new Point(60, 40));
        toAngle(new Point(0, 0), new Point(50, 50));
        toAngle(new Point(0, 0), new Point(40, 60));
        toAngle(new Point(0, 0), new Point(30, 70));
        toAngle(new Point(0, 0), new Point(20, 80));
        toAngle(new Point(0, 0), new Point(10, 90));
        toAngle(new Point(0, 0), new Point(0, 100));
        toAngle(new Point(0, 0), new Point(-10, 90));
        toAngle(new Point(0, 0), new Point(-20, 80));
        toAngle(new Point(0, 0), new Point(-30, 70));
        toAngle(new Point(0, 0), new Point(-40, 60));
        toAngle(new Point(0, 0), new Point(-50, 50));
        toAngle(new Point(0, 0), new Point(-60, 40));
        toAngle(new Point(0, 0), new Point(-70, 30));
        toAngle(new Point(0, 0), new Point(-80, 20));
        toAngle(new Point(0, 0), new Point(-90, 10));
        toAngle(new Point(0, 0), new Point(-100, 0));
        toAngle(new Point(0, 0), new Point(-90, -10));
        toAngle(new Point(0, 0), new Point(-80, -20));
        toAngle(new Point(0, 0), new Point(-70, -30));
        toAngle(new Point(0, 0), new Point(-60, -40));
        toAngle(new Point(0, 0), new Point(-50, -50));
        toAngle(new Point(0, 0), new Point(-40, -60));
        toAngle(new Point(0, 0), new Point(-30, -70));
        toAngle(new Point(0, 0), new Point(-20, -80));
        toAngle(new Point(0, 0), new Point(-10, -90));
        toAngle(new Point(0, 0), new Point(0, -100));
        toAngle(new Point(0, 0), new Point(10, -90));
        toAngle(new Point(0, 0), new Point(20, -80));
        toAngle(new Point(0, 0), new Point(30, -70));
        toAngle(new Point(0, 0), new Point(40, -60));
        toAngle(new Point(0, 0), new Point(50, -50));
        toAngle(new Point(0, 0), new Point(60, -40));
        toAngle(new Point(0, 0), new Point(70, -30));
        toAngle(new Point(0, 0), new Point(80, -20));
        toAngle(new Point(0, 0), new Point(90, -10));
        toVector(new Point(0, 0), 185+360);
        toAngle(new Point(0, 0), new Point(-100, -9));
    }

    public static double toAngle(Point source, Point target) {
        double angle = Math.toDegrees(Math.atan2(target.y - source.y, target.x - source.x));
        System.out.println(target.x + ","+target.y + " angle: " + angle);
        return angle;
    }

    public static Point toVector(Point source, double angle) {
        Point target = new Point(
                (int) (source.getX() + Math.round(Math.cos(Math.toRadians(angle)) * 100)),
                (int) (source.getY() + Math.round(Math.sin(Math.toRadians(angle)) * 100))
        );
        System.out.println("Target: " + target);
        return target;
    }
}
