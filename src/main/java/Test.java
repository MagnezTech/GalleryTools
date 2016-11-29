import java.util.TreeMap;

/**
 * Created by mgwozdek on 2016-11-29.
 */
public class Test {
    public static void main(String[] args) {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        map.put(4,4);
        map.put(1,1);
        map.put(2,2);
        map.put(6,6);
        map.put(3,3);
        System.out.println(map.values());
    }
}
