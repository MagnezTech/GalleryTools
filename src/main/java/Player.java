import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Grab Snaffles and try to throw them through the opponent's goal!
 * Move towards a Snaffle and use your team id to determine where you need to throw it.
 **/
class Player {
    static int team = 1;
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left
        team = myTeamId == 0 ? 1 : -1 ;
        List<Wizard> wizards = new ArrayList<>();
        List<Wizard> opponent = new ArrayList<>();
        List<Ball> balls = new ArrayList<>();
        List<Ball> bludgers = new ArrayList<>();

        // game loop
        while (true) {
            wizards.clear();
            opponent.clear();
            balls.clear();
            bludgers.clear();
            int entities = in.nextInt(); // number of entities still in game
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // entity identifier
                String entityType = in.next(); // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first league)
                int x = in.nextInt(); // position
                int y = in.nextInt(); // position
                int vx = in.nextInt(); // velocity
                int vy = in.nextInt(); // velocity
                int state = in.nextInt(); // 1 if the wizard is holding a Snaffle, 0 otherwise
                if ("WIZARD".equals(entityType)) {
                    wizards.add(new Wizard(x, y, vx, vy, state == 1));
                } else if ("OPPONENT_WIZARD".equals(entityType)) {
                    opponent.add(new Wizard(x, y, vx, vy, state == 1));
                } else if ("SNAFFLE".equals(entityType)) {
                    balls.add(new Ball(x, y, vx, vy));
                } else if ("BLUDGER".equals(entityType)) {
                    bludgers.add(new Ball(x, y, vx, vy));
                }
            }
                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");
                // Edit this line to indicate the action for each wizard (0 <= thrust <= 150, 0 <= power <= 500)
                // i.e.: "MOVE x y thrust" or "THROW x y power"
            Wizard wizard1 = wizards.get(0);
            Wizard wizard2 = wizards.get(1);
            if (wizard1.isGotBall() && wizard2.isGotBall()) {
                throwBall(wizard1);
                throwBall(wizard2);
            } else if (wizard1.isGotBall() && !wizard2.isGotBall()) {
                throwBall(wizard1);
                moveToClosest(balls, wizard2);
            } else if (!wizard1.isGotBall() && wizard2.isGotBall()) {
                moveToClosest(balls, wizard1);
                throwBall(wizard2);
            } else {
                moveToClosest(balls, wizard1, wizard2);
            }
        }
    }

    private static void moveToClosest(List<Ball> balls, Wizard wizard) {
        TreeMap<Double, Ball> ballsDistance = new TreeMap<>();
        for (Ball ball : balls) {
            ballsDistance.put(Point.distance(wizard.getX(), wizard.getY(), ball.getX(), ball.getY()) + ball.getSpeed() * team, ball);
        }
        move(ballsDistance.get(0));
    }

    private static void moveToClosest(List<Ball> balls, Wizard first, Wizard another) {
        TreeMap<Double, Ball> firstDistance = new TreeMap<>();
        for (Ball ball : balls) {
            firstDistance.put(Point.distance(first.getX(), first.getY(), ball.getX(), ball.getY()) + ball.getSpeed() * team, ball);
        }
        TreeMap<Double, Ball> anotherDistance = new TreeMap<>();
        for (Ball ball : balls) {
            anotherDistance.put(Point.distance(another.getX(), another.getY(), ball.getX(), ball.getY()) + ball.getSpeed() * team, ball);
        }
        if (firstDistance.firstEntry() == anotherDistance.firstEntry()) {
            if (firstDistance.firstKey() < anotherDistance.firstKey()) {
                move(firstDistance.get(0));
                move(anotherDistance.get(1));
            } else {
                move(firstDistance.get(1));
                move(anotherDistance.get(0));
            }
        } else {
            move(firstDistance.get(0));
            move(anotherDistance.get(0));
        }
    }

    private static Ball findClosestSlowestBall(List<Ball> balls, Wizard wizard1, Wizard wizard2) {
        Ball closestSlowestBall = null;
        double distance = 100000;
        for (Ball ball : balls) {
            if (ball.isAlreadyTarget()) continue;
            double newDistance = Point.distance(wizard1.getX(), wizard1.getY(), ball.getX(), ball.getY()) + ball.getSpeed() * team;

            if (closestSlowestBall == null || newDistance < distance) {
                closestSlowestBall = ball;
                distance = newDistance;
            }
        }
        closestSlowestBall.setAlreadyTarget(true);
        return closestSlowestBall;
    }

    private static void move(Ball ball){
        System.out.println("MOVE " + ball.getX() + " " + ball.getY() + " 150");
    }

    private static void throwBall(Wizard wizard) {
        if (wizard.isGotBall()) {
            if (team == 1)  {
                System.out.println("THROW 16000 3750 500");
            } else {
                System.out.println("THROW 0 3750 500");
            }
        }
    }
}

class Ball {
    private int x;
    private int y;
    private int vx;
    private int vy;
    private boolean alreadyTarget;

    public Ball(int x, int y, int vx, int vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        alreadyTarget = false;
    }

    public void setAlreadyTarget(boolean alreadyTarget) {
        this.alreadyTarget = alreadyTarget;
    }

    public boolean isAlreadyTarget() {
        return alreadyTarget;
    }

    public double getSpeed(){
        return Point.distance(x,y, vx, vy);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getVx() {
        return vx;
    }

    public void setVx(int vx) {
        this.vx = vx;
    }

    public int getVy() {
        return vy;
    }

    public void setVy(int vy) {
        this.vy = vy;
    }
}

class Wizard {
    private int x;
    private int y;
    private int vx;
    private int vy;
    boolean gotBall;

    public Wizard(int x, int y, int vx, int vy, boolean gotBall) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.gotBall = gotBall;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getVx() {
        return vx;
    }

    public void setVx(int vx) {
        this.vx = vx;
    }

    public int getVy() {
        return vy;
    }

    public void setVy(int vy) {
        this.vy = vy;
    }

    public boolean isGotBall() {
        return gotBall;
    }

    public void setGotBall(boolean gotBall) {
        this.gotBall = gotBall;
    }
}
