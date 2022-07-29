package heap.handig;

/**
 * Created by Steven on 14/10/2017.
 */
public class VGL {
    public static boolean isKl(Comparable a, Comparable b) {
        return a.compareTo(b)<0;
    }

    public static boolean isKlGl(Comparable a, Comparable b){
        return a.compareTo(b)<=0;
    }
}
