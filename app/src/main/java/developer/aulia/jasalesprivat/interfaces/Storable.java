package developer.aulia.jasalesprivat.interfaces;

import java.util.Map;

//interface harus diimplementasikan pada objek database
public interface Storable {
    public String getId();
    public Map<String,Object> marshal();
}
