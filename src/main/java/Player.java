import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

class Player {
    static int team = 1;
    static int initial = 2;
    static int lastObliviate = 0;
    static Point topBorder;
    static Point bottomBorder;


    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left
        team = myTeamId == 0 ? 1 : -1;
        if (team == 1) {
            topBorder = new Point(16000, 3750 - 2000);
            bottomBorder = new Point(16000, 3750 + 2000);
        } else {
            topBorder = new Point(16000, 3750 - 2000);
            bottomBorder = new Point(16000, 3750 + 2000);
        }
        Map<Integer, Entity> entitiesMap = new HashMap<>();
        Map<Integer, Wizard> opponents;
        Map<Integer, Ball> balls;
        Map<Integer, Ball> bludgers;

        Wizard shooter = null;
        Wizard defender = null;

        while (true) {
            entitiesMap.clear();
            int entities = in.nextInt(); // number of entities still in game
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // entity identifier
                String entityType = in.next(); // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first league)
                int x = in.nextInt(); // position
                int y = in.nextInt(); // position
                int vx = in.nextInt(); // velocity
                int vy = in.nextInt(); // velocity
                int state = in.nextInt(); // 1 if the wizard is holding a Snaffle, 0 otherwise
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

            balls = findBalls(entitiesMap);
            bludgers = findBludgers(entitiesMap);
            opponents = findOpponents(entitiesMap);
            if (initial == 2) {
                initial = 1;
                for (Entity e : entitiesMap.values()) {
                    if (e.isWizard()) {
                        if (shooter == null) {
                            shooter = (Wizard) e;
                        } else {
                            defender = (Wizard) e;
                        }
                    }
                }
                findShooterAndDefender(balls, shooter, defender);
            } else {
                shooter = (Wizard) entitiesMap.get(shooter.getEntityId());
                defender = (Wizard) entitiesMap.get(defender.getEntityId());
            }
            prepereDefenderMove(defender, balls, opponents, bludgers);
            prepereShooterMove(shooter, balls, opponents, bludgers);
            System.out.println(shooter.getMove());
            System.out.println(defender.getMove());
        }
    }

    private static void prepereDefenderMove(Wizard wizard, Map<Integer, Ball> balls, Map<Integer, Wizard> opponents, Map<Integer, Ball> bludgers) {
        if (wizard.hasBall()) {
            wizard.setMove(throwWithVector(wizard, opponents));
        } else {
            TreeMap<Double, Ball> ballsToFlipendo = new TreeMap<>();
            for (Ball ball : balls.values()) {
                long angleToBall = Math.round(toAngle(wizard.getPoint(), ball.getPoint()) + 180);
                long angleToTop = Math.round(toAngle(wizard.getPoint(), topBorder) + 180);
                long angleToBottom = Math.round(toAngle(wizard.getPoint(), bottomBorder) + 180);
                System.err.println("ball:" + ball.getEntityId() + " " + angleToBall + " " + angleToTop + " " + angleToBottom + " " + isBetween(angleToBall, angleToTop, angleToBottom));
                if (isBetween(angleToBall, angleToTop, angleToBottom)) {
                    //jak blisko i nie ma nic na drodze, strzelaj
                }
            }
            TreeMap<Double, Ball> distancesToBludgers = new TreeMap<>();
            for (Ball bludger : bludgers.values()) {
                if (bludger == null) continue;
                distancesToBludgers.put(Point.distance(bludger.getX(), bludger.getY(), wizard.getX(), wizard.getY()), bludger);
            }
            Ball closestBludger = distancesToBludgers.pollFirstEntry().getValue();
            if (wizard.isCloseTo(closestBludger.getPoint()) && lastObliviate > 20) {
                wizard.setMove("OBLIVIATE " + closestBludger.getEntityId());
                lastObliviate = 0;
                return;
            }
            lastObliviate++;
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
            Point opponentVector = toVector(closestOpponent.getPoint(), toAngle(closestOpponent.getPoint(), closestBallToOpponent.getPoint()));

            TreeMap<Double, Ball> distances = new TreeMap<>();
            distances.put(2000.0, new Ball(100, "NIC", team == 1 ? 300 : 15700, 3750, 0, 0, 0));
            distances.put(Point.distance(opponentVector.getX(), opponentVector.getY(), wizard.getX(), wizard.getY()), new Ball(100, "NIC", team == 1 ? 300 : 15700, 3750, 0, 0, 0));
            for (Ball ball : balls.values()) {
                if (ball == null || ball.taken()) continue;
                distances.put(Point.distance(ball.getX(), ball.getY(), wizard.getX(), wizard.getY()), ball);
            }
            Ball closest;
            do {
                closest = distances.pollFirstEntry().getValue();
            } while (closest.taken() || distances.isEmpty());
            closest.setTaken(true);
            wizard.setMove(createMove(wizard.getPoint(), closest.getPoint()));
        }
    }

    private static boolean isBetween(long angleToBall, long angleToTop, long angleToBottom) {
        if(team == 1){
            return (angleToBall > angleToTop) && (angleToBall < angleToBottom);
        } else {
            return (angleToBall > angleToTop) && (angleToBall > angleToBottom);
        }
    }

    private static void prepereShooterMove(Wizard wizard, Map<Integer, Ball> balls, Map<Integer, Wizard> opponents, Map<Integer, Ball> bludgers) {
        if (wizard.hasBall()) {
            wizard.setMove(throwWithVector(wizard, opponents));
        } else {
            TreeMap<Double, Ball> distancesToBludgers = new TreeMap<>();
            for (Ball bludger : bludgers.values()) {
                if (bludger == null) continue;
                distancesToBludgers.put(Point.distance(bludger.getX(), bludger.getY(), wizard.getX(), wizard.getY()), bludger);
            }
            Ball closestBludger = distancesToBludgers.pollFirstEntry().getValue();
            if (wizard.isCloseTo(closestBludger.getPoint()) && lastObliviate > 20) {
                wizard.setMove("OBLIVIATE " + closestBludger.getEntityId());
                lastObliviate = 0;
                return;
            }
            lastObliviate++;
            TreeMap<Double, Ball> distances = new TreeMap<>();
            for (Ball ball : balls.values()) {
                if (ball == null) continue;
                distances.put(Point.distance(ball.getX(), ball.getY(), wizard.getX(), wizard.getY()), ball);
            }
            Ball closest;
            do {
                closest = distances.pollFirstEntry().getValue();
            } while (closest.taken() && !distances.isEmpty());
            closest.setTaken(true);
            wizard.setMove(createMove(wizard.getPoint(), closest.getPoint()));
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

    private static void findShooterAndDefender(Map<Integer, Ball> balls, Wizard shooter, Wizard defender) {
        int countTop = 0;
        int countBottom = 0;
        for (Ball ball : balls.values()) {
            if (isOnOurHalfOnTop(ball)) {
                countTop++;
            } else if (isOnOurHalfOnBottom(ball)) {
                countBottom++;
            }
        }
        if (countTop > countBottom) {
            if (shooter.getY() < defender.getY()) {
                //ok
            } else {
                Wizard temp = shooter;
                shooter = defender;
                defender = temp;
            }
        } else {
            if (shooter.getY() > defender.getY()) {
                //ok
            } else {
                Wizard temp = shooter;
                shooter = defender;
                defender = temp;
            }
        }
    }

    private static boolean isOnOurHalfOnBottom(Ball ball) {
        if (team == 1) {
            return ball.getX() < 8000 && ball.getY() > 3750;
        } else {
            return ball.getX() > 8000 && ball.getY() < 3750;
        }
    }

    private static boolean isOnOurHalfOnTop(Ball ball) {
        if (team == 1) {
            return ball.getX() < 8000 && ball.getY() <= 3750;
        } else {
            return ball.getX() > 8000 && ball.getY() <= 3750;
        }
    }

    private static String throwWithVector(Wizard wizard, Map<Integer, Wizard> opponents) {
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
            if (wizard.isCloseTo(opponent.getPoint()) && opponent.further(wizard, team)) {
                if (wizard.under(opponent)) {
                    angle += 30;
                } else {
                    angle -= 30;
                }
                break;
            }
        }
        Point p = toVector(wizard.getPoint(), angle);
        return "THROW " + p.x + " " + p.y + " 500";
//        return "THROW " + target.x + " " + target.y + " 500";
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
                (int) (source.getX() + Math.round(Math.cos(Math.toRadians(angle)) * 10000)),
                (int) (source.getY() + Math.round(Math.sin(Math.toRadians(angle)) * 10000))
        );
    }

    public static double toAngle(Point source, Point target) {
        return Math.toDegrees(Math.atan2(target.y - source.y, target.x - source.x));
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

    public boolean isCloseTo(Point xy) {
        return Point.distance(x, y, xy.getX(), xy.getY()) < 3000;
    }

    public boolean under(Wizard opponent) {
        if (y > opponent.y) return true;
        return false;
    }

    public boolean further(Wizard opponent, int team) {
        if (team == 1) {
            if (x > opponent.x) return true;
            return false;
        } else {
            if (x < opponent.x) return true;
            return false;
        }
    }

}

class Ball extends Entity {
    private boolean taken = false;

    public Ball(int entityId, String entityType, int x, int y, int vx, int vy, int state) {
        super(entityId, entityType, x, y, vx, vy, state);
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

