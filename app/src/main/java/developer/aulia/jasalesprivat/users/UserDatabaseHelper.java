package developer.aulia.jasalesprivat.users;


import developer.aulia.jasalesprivat.interfaces.DatabaseHelper;
import developer.aulia.jasalesprivat.sessions.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class UserDatabaseHelper extends DatabaseHelper<User> {
    private static final String COLLECTION_NAME = "app_users";
    private static final String TAG = "UserDatabaseHelper";

    public UserDatabaseHelper() {
        super(COLLECTION_NAME,TAG);
    }

    @Override
    public void getById(String id, OnCompleteListener<DocumentSnapshot> callback) {
        super.getById(id, callback);
    }

    @Override
    public void getAll(OnSuccessListener<QuerySnapshot> successListener, OnFailureListener failureListener) {
        //Probably not a good idea in the case of users, need a more limiting query methods
    }


    public void getPendingTutors(final OnSuccessListener<QuerySnapshot> successListener, final OnFailureListener failureListener) {
        CollectionReference subjectsRef = db.collection(COLLECTION_NAME);
        Query query = subjectsRef.whereEqualTo("Role", Role.TUTOR.toString())
                                 .whereEqualTo("Status", Status.PENDING.toString());
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    failureListener.onFailure(e);
                    return;
                }
                successListener.onSuccess(queryDocumentSnapshots);
            }
        });
    }

    public void getTutorsWithSubjects(final OnSuccessListener<QuerySnapshot> successListener, final OnFailureListener failureListener) {
        CollectionReference tutorsRef = db.collection(COLLECTION_NAME);
        Query query = tutorsRef.whereEqualTo("Role", Role.TUTOR.toString())
                               .whereEqualTo("Status", Status.ACCEPTED.toString());
                               //.whereNotEqualTo("Subjects", new HashMap<>()); //Firebase does not provide inequality checks
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    failureListener.onFailure(e);
                    return;
                }
                successListener.onSuccess(queryDocumentSnapshots);
            }
        });
    }
}
