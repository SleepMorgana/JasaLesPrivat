package developer.aulia.jasalesprivat.sessions;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SessionManager {
    private static SessionDatabaseHelper db = null;

    public static void retrieveSessionById(String id, OnCompleteListener listener){
        if (id == null || id.equals(""))
            return;
        getDbInstance().getById(id,listener);
    }

    public static void addNewSession(final Session session, @NonNull final OnSuccessListener<Void> success, @NonNull final OnFailureListener error){
        if (session==null)
            error.onFailure(new UnsupportedOperationException("session is null"));

        getDbInstance().upsert(session, success, error);
    }

    public static void updateSession(final Session session, @NonNull final OnSuccessListener<Void> success, @NonNull final OnFailureListener error){
        if (session==null)
            error.onFailure(new UnsupportedOperationException("session is null"));
        getDbInstance().upsert(session, success, error);
    }

    private static SessionDatabaseHelper getDbInstance(){
        if (db != null){
            return db;
        }
        db = new SessionDatabaseHelper();
        return db;
    }
}
