import java.util.HashMap;
import java.util.Map;

public enum Relationship {
    LESS_THAN,
    MORE_THAN,
    EQUAL_TO;

    private final static Map<String,Class> TYPE_MAP =new HashMap<String, Class>();

    static {
        TYPE_MAP.put("int",Integer.class);
    }
}
