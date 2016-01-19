import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap;

/**
 * a util class for handle params
 * @author hzx
 */

public class Params implements Iterable<Entry<String, Object>>{
    List<Entry<String, Object>> params = new ArrayList<>();
    
    /**
     * put param key-value pair
     * @param key
     * @param value 
     */
    public void put(String key, Object value) {
        params.add(new AbstractMap.SimpleEntry<>(key, value));
    }
    
    @Override
    public String toString(){ 
        String paramsInfo = "";
        for(Entry<String, Object> param : params) {
            paramsInfo += param.getKey() + ":" + param.getValue() + "\n";
        }
        return paramsInfo;
    }

    @Override
    public Iterator<Entry<String, Object>> iterator() {
        return params.iterator();
    }
}
