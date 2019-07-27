package developer.aulia.jasalesprivat.subjects;

import android.support.annotation.NonNull;

import developer.aulia.jasalesprivat.interfaces.DatabaseHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class SubjectDatabaseHelper extends DatabaseHelper<Subject> {
    private static final String COLLECTION_NAME = "app_subjects";//Nama database untuk jenis mata pelajaran
    private static final String TAG = "SubjectDatabaseHelper";

    public SubjectDatabaseHelper() {
        super(COLLECTION_NAME, TAG);
    }

    public void getByName(String name, @NonNull final OnSuccessListener<QuerySnapshot> successListener, @NonNull final OnFailureListener failureListener){
        CollectionReference subjectsRef = db.collection(COLLECTION_NAME);
        Query query = subjectsRef.whereEqualTo("Name",name);
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
