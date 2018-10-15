package nuc.jyg.knowing.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ji YongGuang.
 * @date  2017/11/01.
 */
public class ViewObject {
    private Map<String, Object> objs = new HashMap<String, Object>();
    public void set(String key, Object value) {
        objs.put(key, value);
    }

    public Object get(String key) {
        return objs.get(key);
    }
}
