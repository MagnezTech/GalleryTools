import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

class Player {
    static boolean symulation = false;

    static int team = 1;
    static int lastObliviate = 0;
    static int lastFlipendo = 0;
    static Point topBorder;
    static Point bottomBorder;
    static Point topBorderUpper;
    static Point bottomBorderUpper;
    static Point topBorderUnder;
    static Point bottomBorderUnder;


    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId;
        if (symulation) {
            myTeamId = 1;
        } else {
            myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left
        }
        team = myTeamId == 0 ? 1 : -1;
        if (team == 1) {
            topBorder = new Point(16000, 3750 - 1500);
            bottomBorder = new Point(16000, 3750 + 1500);
            topBorderUpper = new Point(16000, 3750 - 1500 - 7500 - 1000);
            bottomBorderUpper = new Point(16000, 3750 + 1500 - 7500 - 1000);
            topBorderUnder = new Point(16000, 3750 - 1500 + 7500 + 1000);
            bottomBorderUnder = new Point(16000, 3750 + 1500 + 7500 + 1000);
        } else {
            topBorder = new Point(0, 3750 - 1500);
            bottomBorder = new Point(0, 3750 + 1500);
            topBorderUpper = new Point(0, 3750 - 1500 - 7500);
            bottomBorderUpper = new Point(0, 3750 + 1500 - 7500);
            topBorderUnder = new Point(0, 3750 - 1500 + 7500);
            bottomBorderUnder = new Point(0, 3750 + 1500 + 7500);
        }
        Map<Integer, Entity> entitiesMap = new HashMap<>();
        Map<Integer, Wizard> opponents;
        Map<Integer, Ball> balls;
        Map<Integer, Ball> bludgers;

        Wizard shooter = null;
        Wizard defender = null;

        while (true) {
            entitiesMap.clear();
            if (symulation) {
                try (BufferedReader br = new BufferedReader(new FileReader(new File("data")))) {
                    String sCurrentLine;
                    while ((sCurrentLine = br.readLine()) != null) {
                        Entity entity;
                        String type = sCurrentLine.split(" ")[1];
                        if ("WIZARD".equals(type)) {
                            entity = new Wizard(sCurrentLine);
                        } else if ("OPPONENT_WIZARD".equals(type)) {
                            entity = new Wizard(sCurrentLine);
                        } else if ("SNAFFLE".equals(type)) {
                            entity = new Ball(sCurrentLine);
                        } else {
                            entity = new Ball(sCurrentLine);
                        }
                        entitiesMap.put(entity.getEntityId(), entity);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                int entities = in.nextInt(); // number of entities still in game
                for (int i = 0; i < entities; i++) {
                    int entityId = in.nextInt(); // entity identifier
                    String entityType = in.next(); // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first league)
                    int x = in.nextInt(); // position
                    int y = in.nextInt(); // position
                    int vx = in.nextInt(); // velocity
                    int vy = in.nextInt(); // velocity
                    int state = in.nextInt(); // 1 if the wizard is holding a Snaffle, 0 otherwise
                    System.err.println(entityId + " " + entityType + " " + x + " " + y + " " + vx + " " + vy + " " + state + " " + lastObliviate + " " + lastFlipendo);
                    Entity entity;
                    if ("WIZARD".equals(entityType)) {
                        entity = new Wizard(entityId, entityType, x, y, vx, vy, state);
                    } else if ("OPPONENT_WIZARD".equals(entityType)) {
                        entity = new Wizard(entityId, entityType, x, y, vx, vy, state);
                    } else if ("SNAFFLE".equals(entityType)) {
                        entity = new Ball(entityId, entityType, x, y, vx, vy, state);
                    } else {
                        entity = new Ball(entityId, entityType, x, y, vx, vy, state);
                    }
                    entitiesMap.put(entityId, entity);
                }
            }

            balls = findBalls(entitiesMap);
            bludgers = findBludgers(entitiesMap);
            opponents = findOpponents(entitiesMap);
            shooter = null;
            for (Entity e : entitiesMap.values()) {
                if (e.isWizard()) {
                    if (shooter == null) {
                        shooter = (Wizard) e;
                    } else {
                        defender = (Wizard) e;
                    }
                }
            }
            //findShooterAndDefender(balls, shooter, defender);
            shooter = (Wizard) entitiesMap.get(shooter.getEntityId());
            defender = (Wizard) entitiesMap.get(defender.getEntityId());
            prepereMove(shooter, balls, opponents, bludgers, false);
            prepereMove(defender, balls, opponents, bludgers, true);
            System.out.println(shooter.getMove());
            System.out.println(defender.getMove());
        }
    }

    private static void prepereMove(Wizard wizard, Map<Integer, Ball> balls, Map<Integer, Wizard> opponents, Map<Integer, Ball> bludgers, boolean isDefender) {
        if (wizard.hasBall()) {
            wizard.setMove(throwWithVector(wizard, opponents, bludgers));
        } else {
            TreeMap<Double, Ball> ballsToFlipendo = new TreeMap<>();
            for (Ball ball : balls.values()) {
                boolean goodShot = false;
                long angleToBall = Math.round(toAngle(wizard.getPoint(), ball.getPoint()) + 180);
                long angleToTop = Math.round(toAngle(wizard.getPoint(), topBorder) + 180);
                long angleToBottom = Math.round(toAngle(wizard.getPoint(), bottomBorder) + 180);
                long angleToTopUpper = Math.round(toAngle(wizard.getPoint(), topBorderUpper) + 180);
                long angleToBottomUpper = Math.round(toAngle(wizard.getPoint(), bottomBorderUpper) + 180);
                long angleToTopUnder = Math.round(toAngle(wizard.getPoint(), topBorderUnder) + 180);
                long angleToBottomUnder = Math.round(toAngle(wizard.getPoint(), bottomBorderUnder) + 180);
                if ( isBetween(angleToBall, angleToTop, angleToBottom) ||
                        (isBetween(angleToBall, angleToTopUpper, angleToBottomUpper) && ball.getX() > 2000) ||
                        (isBetween(angleToBall, angleToTopUnder, angleToBottomUnder) && ball.getX() < 5500)) {
                    if (wizard.isCloseTo(ball.getPoint(), 5000)) {
                        goodShot = true;
                        for (Wizard opponent : opponents.values()) {
                            long angleToOpponent = Math.round(toAngle(wizard.getPoint(), opponent.getPoint()) + 180);
                            double angleDiff = Math.abs(angleToOpponent - angleToBall);
                            if (opponent.further(wizard.getPoint(), team) && isOnWay(wizard.getPoint(), opponent.getPoint(), angleDiff)) {
                                goodShot = false;
                            }
                        }
                        for (Ball bludger : bludgers.values()) {
                            long angleToBludger = Math.round(toAngle(wizard.getPoint(), bludger.getPoint()) + 180);
                            double angleDiff = Math.abs(angleToBludger - angleToBall);
                            if (bludger.further(wizard.getPoint(), team) && isOnWay(wizard.getPoint(), bludger.getPoint(), angleDiff)) {
                                goodShot = false;
                            }
                        }
                    }
                }
                if (goodShot) {
                    ballsToFlipendo.put(Point.distance(ball.getX(), ball.getY(), wizard.getX(), wizard.getY()), ball);
                }
            }
            if (!ballsToFlipendo.isEmpty() && lastFlipendo > 15) {
                lastFlipendo = 0;
                wizard.setMove("FLIPENDO " + ballsToFlipendo.pollFirstEntry().getValue().getEntityId());
                return;
            }
            // TreeMap<Double, Ball> distancesToBludgers = new TreeMap<>();
            // for (Ball bludger : bludgers.values()) {
            //     if (bludger == null) continue;
            //     distancesToBludgers.put(Point.distance(bludger.getX(), bludger.getY(), wizard.getX(), wizard.getY()), bludger);
            // }
            // Ball closestBludger = distancesToBludgers.pollFirstEntry().getValue();
            // if (wizard.isCloseTo(closestBludger.getPoint(), 2500) && lastObliviate > 30) {
            //     wizard.setMove("OBLIVIATE " + closestBludger.getEntityId());
            //     lastObliviate = 0;
            //     return;
            // }
            lastObliviate++;
            lastFlipendo++;
            TreeMap<Double, Wizard> distancesToOpponents = new TreeMap<>();
            for (Wizard opponent : opponents.values()) {
                if (opponent == null) continue;
                distancesToOpponents.put(Point.distance(opponent.getX(), opponent.getY(), wizard.getX(), wizard.getY()), opponent);
            }
            Wizard closestOpponent = distancesToOpponents.firstEntry().getValue();
            TreeMap<Double, Ball> distancesFromOpponentToBall = new TreeMap<>();
            for (Ball ball : balls.values()) {
                if (ball == null) continue;
                distancesFromOpponentToBall.put(Point.distance(ball.getX(), ball.getY(), closestOpponent.getX(), closestOpponent.getY()), ball);
            }
            Ball closestBallToOpponent = distancesFromOpponentToBall.pollFirstEntry().getValue();

            TreeMap<Double, Ball> distances = new TreeMap<>();
            if (isDefender) {
                int position = (int) Math.round((3750 - 2000) + 4000 * closestBallToOpponent.getY() * 1.0 / 7500.0);
                distances.put(3000.0, new Ball(100, "NIC", team == 1 ? 500 : 15500, 3750, 0, 0, 0));
                distances.put(Point.distance(closestBallToOpponent.getX(), closestBallToOpponent.getY(), wizard.getX(), wizard.getY()), new Ball(100, "NIC", team == 1 ? 500 : 15500, position, 0, 0, 0));
            }
            for (Ball ball : balls.values()) {
                if (ball == null || ball.taken()) continue;
                distances.put(Point.distance(ball.getX(), ball.getY(), wizard.getX(), wizard.getY()), ball);
            }
            Ball closest;
            do {
                closest = distances.pollFirstEntry().getValue();
            } while (!distances.isEmpty() && closest.taken());
            closest.setTaken(true);
            wizard.setMove(createMove(wizard.getPoint(), closest.getPoint()));
        }
    }

    private static boolean isOnWay(Point wizard, Point opponent, double angle) {
        int distance = (int) Math.round(Point.distance(opponent.getX(), opponent.getY(), wizard.getX(), wizard.getY()));
        return Math.sqrt(2 * distance * distance * (1 - Math.cos(Math.toRadians(angle)))) < 500;
    }

    private static boolean isBetween(long angleToBall, long angleToTop, long angleToBottom) {
        if (team == 1) {
            return (angleToBall > angleToTop) && (angleToBall < angleToBottom);
        } else {
            return (angleToBall < angleToTop) && (angleToBall > angleToBottom);
        }
    }

    private static Map<Integer, Ball> findBalls(Map<Integer, Entity> entitiesMap) {
        Map<Integer, Ball> map = new HashMap<>();
        for (Entity e : entitiesMap.values()) {
            if (e.isBall()) map.put(e.getEntityId(), (Ball) e);
        }
        return map;
    }

    private static Map<Integer, Ball> findBludgers(Map<Integer, Entity> entitiesMap) {
        Map<Integer, Ball> map = new HashMap<>();
        for (Entity e : entitiesMap.values()) {
            if (e.isBludger()) map.put(e.getEntityId(), (Ball) e);
        }
        return map;
    }

    private static Map<Integer, Wizard> findOpponents(Map<Integer, Entity> entitiesMap) {
        Map<Integer, Wizard> map = new HashMap<>();
        for (Entity e : entitiesMap.values()) {
            if (e.isOpponent()) map.put(e.getEntityId(), (Wizard) e);
        }
        return map;
    }

    private static String throwWithVector(Wizard wizard, Map<Integer, Wizard> opponents, Map<Integer, Ball> bludgers) {
        Point target;
        int vx = wizard.getVx();
        int vy = wizard.getVy();
        if (team == 1) {
            target = new Point((16000 + (-1 * vx)), (3750 + (-1 * vy)));
        } else {
            target = new Point((-1 * vx), (3750 + (-1 * vy)));
        }
        double angle = toAngle(wizard.getPoint(), target);
        for (Wizard opponent : opponents.values()) {
            if (wizard.isCloseTo(opponent.getPoint(), 2000) && opponent.further(wizard.getPoint(), team)) {
                if (wizard.under(opponent.getPoint())) {
                    angle += 30 * team;
                } else {
                    angle -= 30 * team;
                }
                break;
            }
        }
        for (Ball bludger : bludgers.values()) {
            if (wizard.isCloseTo(bludger.getPoint(), 3000) && bludger.further(wizard.getPoint(), team)) {
                if (wizard.under(bludger.getPoint())) {
                    angle += 15;
                } else {
                    angle -= 15;
                }
                break;
            }
        }
        Point p = toVector(wizard.getPoint(), angle);
        return "THROW " + p.x + " " + p.y + " 500";
    }

    private static String createMove(int x, int y) {
        return "MOVE " + x + " " + y + " 150";
    }

    private static String createMove(Point xy) {
        return createMove(xy.x, xy.y);
    }

    private static String createMove(Point source, double angle) {
        return createMove(toVector(source, angle));
    }

    private static String createMove(Point source, Point target) {
        return createMove(toVector(source, toAngle(source, target)));
    }

    public static Point toVector(Point source, double angle) {
        return new Point(
                (int) (source.getX() + Math.round(Math.cos(Math.toRadians(angle)) * 2000)),
                (int) (source.getY() + Math.round(Math.sin(Math.toRadians(angle)) * 2000))
        );
    }

    public static double toAngle(Point source, Point target) {
        return Math.toDegrees(Math.atan2(target.y - source.y, target.x - source.x));
    }

    private static Integer getEntityId(String sCurrentLine) {
        return Integer.valueOf(sCurrentLine.split(" ")[0]);
    }
}

class Entity {
    int entityId;
    String entityType;
    int x;
    int y;
    int vx;
    int vy;
    int state;

    public Entity(int entityId, String entityType, int x, int y, int vx, int vy, int state) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.state = state;
    }

    public Entity(String line) {
        String[] split = line.split(" ");
        this.entityId = Integer.valueOf(split[0]);
        this.entityType = split[1];
        this.x = Integer.valueOf(split[2]);
        this.y = Integer.valueOf(split[3]);
        ;
        this.vx = Integer.valueOf(split[4]);
        this.vy = Integer.valueOf(split[5]);
        this.state = Integer.valueOf(split[6]);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "entityId=" + entityId +
                ", entityType='" + entityType + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", vx=" + vx +
                ", vy=" + vy +
                ", state=" + state +
                '}';
    }

    public boolean isWizard() {
        return "WIZARD".equals(entityType);
    }

    public boolean isOpponent() {
        return "OPPONENT_WIZARD".equals(entityType);
    }

    public boolean isBall() {
        return "SNAFFLE".equals(entityType);
    }

    public boolean isBludger() {
        return "BLUDGER".equals(entityType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        return entityId == entity.entityId;

    }

    @Override
    public int hashCode() {
        return entityId;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getVx() {
        return vx;
    }

    public int getVy() {
        return vy;
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    public Point getVector() {
        return new Point(vx, vy);
    }

    public boolean isCloseTo(Point xy, int distance) {
        return Point.distance(x, y, xy.getX(), xy.getY()) < distance;
    }

    public boolean under(Point xy) {
        if (y > xy.y) return true;
        return false;
    }

    public boolean further(Point xy, int team) {
        if (team == 1) {
            if (x > xy.x) return true;
            return false;
        } else {
            if (x < xy.x) return true;
            return false;
        }
    }

}

class Ball extends Entity {
    private boolean taken = false;

    public Ball(int entityId, String entityType, int x, int y, int vx, int vy, int state) {
        super(entityId, entityType, x, y, vx, vy, state);
    }

    public Ball(String line) {
        super(line);
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public boolean taken() {
        return taken;
    }

    @Override
    public String toString() {
        return "Ball{" +
                "entityId=" + entityId +
                ", entityType='" + entityType + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", vx=" + vx +
                ", vy=" + vy +
                ", state=" + state +
                ", taken=" + taken +
                '}';
    }
}

class Wizard extends Entity {
    private String move;

    public Wizard(int entityId, String entityType, int x, int y, int vx, int vy, int state) {
        super(entityId, entityType, x, y, vx, vy, state);
    }

    public Wizard(String line) {
        super(line);
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public boolean hasBall() {
        return state == 1;
    }
}
