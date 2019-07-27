package developer.aulia.jasalesprivat.interfaces;

import java.util.Map;

//interface must be implemented by database objects
public interface Storable {
    public String getId();
    public Map<String,Object> marshal();
}
