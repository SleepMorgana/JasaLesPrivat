package developer.aulia.jasalesprivat.sessions;


import developer.aulia.jasalesprivat.interfaces.DatabaseHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.UUID;

public class SessionDatabaseHelper extends DatabaseHelper<Session> {
    private static final String COLLECTION_NAME = "app_sessions";//Nama database untuk membuat jadwal les
    private static final String TAG = "SessionDatabaseHelper";

    public SessionDatabaseHelper() {
        super(COLLECTION_NAME, TAG);
    }

    @Override
    public void upsert(Session obj, OnSuccessListener<Void> successes, OnFailureListener failureListener) {
        if (obj.getId() == null || obj.getId().equals("")){
            //buat id
            UUID uuid = UUID.randomUUID();
            obj.setId(uuid.toString());
        }
        super.upsert(obj, successes, failureListener);
    }

    //TODO add query methods: getPendingSessions by user
}
