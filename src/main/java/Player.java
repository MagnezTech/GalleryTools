import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

class Player {
    static int team = 1;
    static int initial = 2;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left
        team = myTeamId == 0 ? 1 : -1;
        Map<Integer, Entity> entitiesMap = new HashMap<>();
        Map<Integer, Ball> balls;

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
                    entity = new Entity(entityId, entityType, x, y, vx, vy, state);
                }
                entitiesMap.put(entityId, entity);
            }

            balls = findBalls(entitiesMap);
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
//                findWizards(entitiesMap, shooter, defender);
                findShooterAndDefender(balls, shooter, defender);
            } else {
                shooter = (Wizard) entitiesMap.get(shooter.getEntityId());
                defender = (Wizard) entitiesMap.get(defender.getEntityId());
            }
            prepereDefenderMove(defender, balls, entitiesMap);
            prepereShooterMove(shooter, balls, entitiesMap);
//            prepereShooterMove(defender, balls, entitiesMap);
            System.out.println(shooter.getMove());
            System.out.println(defender.getMove());
        }
    }

    private static void prepereDefenderMove(Wizard shooter, Map<Integer, Ball> balls, Map<Integer, Entity> entitiesMap) {
        if (shooter.hasBall()) {
            shooter.setMove(throwWithVector(shooter, entitiesMap));
        } else {
            TreeMap<Double, Ball> distances = new TreeMap<>();
            distances.put(2200.0, new Ball(100, "NIC", 300, 3750, 0, 0, 0));
            for (Ball ball : balls.values()) {
                if (ball == null || ball.taken()) continue;
                distances.put(Point.distance(ball.getX(), ball.getY(), shooter.getX(), shooter.getY()), ball);
            }
            Ball closest = distances.firstEntry().getValue();
            closest.setTaken(true);
            shooter.setMove("MOVE " + closest.getX() + " " + closest.getY() + " 150");
        }
    }

    private static void prepereShooterMove(Wizard shooter, Map<Integer, Ball> balls, Map<Integer, Entity> entitiesMap) {
        if (shooter.hasBall()) {
            shooter.setMove(throwWithVector(shooter, entitiesMap));
        } else {
            TreeMap<Double, Ball> distances = new TreeMap<>();
            for (Ball ball : balls.values()) {
                if (ball == null || ball.taken()) continue;
                distances.put(Point.distance(ball.getX(), ball.getY(), shooter.getX(), shooter.getY()), ball);
            }
            Ball closest = distances.firstEntry().getValue();
            closest.setTaken(true);
            shooter.setMove("MOVE " + closest.getX() + " " + closest.getY() + " 150");
        }
    }

//    private static void findWizards(Map<Integer, Entity> entitiesMap, Wizard shooter, Wizard defender) {
//        for (Entity e : entitiesMap.values()) {
//            if (e.isWizard()) {
//                if (shooter == null) {
//                    shooter = (Wizard) e;
//                } else {
//                    defender = (Wizard) e;
//                }
//            }
//        }
//    }

    private static Map<Integer, Ball> findBalls(Map<Integer, Entity> entitiesMap) {
        Map<Integer, Ball> newB = new HashMap<>();
        for (Entity e : entitiesMap.values()) {
            if (e.isBall()) newB.put(e.getEntityId(), (Ball) e);
        }
        return newB;
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

    private static String throwWithVector(Wizard wizard, Map<Integer, Entity> entitiesMap) {
        Point target;
        int vx = wizard.getVx();
        int vy = wizard.getVy();
        if (team == 1) {
            target = new Point((16000 + (-1 * vx)), (3750 + (-1 * vy)));
        } else {
            target = new Point((-1 * vx), (3750 + (-1 * vy)));
        }
        boolean changeAngle = false;
        for (Entity entity : entitiesMap.values()) {
            if (entity.isBludger() || entity.isOpponent()) {
                if (sameAngle(wizard.getPoint(), target, entity.getPoint()))
                    changeAngle = wizard.isCloseTo(entity.getPoint());
            }
        }
        if (changeAngle) {
            float angleTarget = (float) Math.toDegrees(Math.atan2(target.y - wizard.y, target.x - wizard.x));
            target = new Point(
                    (int) (wizard.getX() + Math.round(Math.cos(Math.toRadians(angleTarget - 0)) * 1000)),
                    (int) (wizard.getY() + Math.round(Math.sin(Math.toRadians(angleTarget - 0)) * 1000))
            );
        }
        return "THROW " + target.x + " " + target.y + " 500";
    }

    private static boolean sameAngle(Point source, Point target, Point opponent) {
        float angleTarget = (float) Math.toDegrees(Math.atan2(target.y - source.y, target.x - source.x));
        float angleOpponent = (float) Math.toDegrees(Math.atan2(opponent.y - source.y, opponent.x - source.x));
        if (angleTarget < 0) {
            angleTarget += 360;
        }
        if (angleOpponent < 0) {
            angleOpponent += 360;
        }
        return (angleTarget - angleOpponent) < 30;
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

    public Entity(int entityId) {
        this.entityId = entityId;
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

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    public boolean isCloseTo(Point xy) {
        return 4000 > Point.distance(x, y, xy.getX(), xy.getY());
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

    public double getSpeed() {
        return Point.distance(x, y, vx, vy);
    }
}

class Wizard extends Entity {
    private String move;

    public void setMove(String move) {
        this.move = move;
    }

    public String getMove() {
        return move;
    }

    public Wizard(int entityId, String entityType, int x, int y, int vx, int vy, int state) {
        super(entityId, entityType, x, y, vx, vy, state);
    }

    public boolean hasBall() {
        return state == 1;
    }
}

