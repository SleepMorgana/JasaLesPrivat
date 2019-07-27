package developer.aulia.jasalesprivat.interfaces;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

public abstract class DatabaseHelper<T extends Storable> {
    private String COLLECTION_NAME;// collection where the entity is situated
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String TAG;

    public DatabaseHelper(String collectionName, String tag){
        COLLECTION_NAME = collectionName;
        TAG = tag;
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    //if the entity is present in the database update the record, else create new record
    public void upsert(final T obj, OnSuccessListener<Void> successes, OnFailureListener failureListener){
        if (obj.getId()==null) {
            db.collection(COLLECTION_NAME)
                    .document()
                    .set(obj.marshal())
                    .addOnSuccessListener(successes)
                    .addOnFailureListener(failureListener);
            return;
        }
        db.collection(COLLECTION_NAME)
                .document(obj.getId())
                .set(obj.marshal())
                .addOnSuccessListener(successes)
                .addOnFailureListener(failureListener);
    }
    //get entity by document id
    public void getById(String id, OnCompleteListener<DocumentSnapshot> callback){
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
        docRef.get().addOnCompleteListener(callback);
    }

    //delete single item
    public void deleteById(String id, OnSuccessListener<Void> successes, OnFailureListener failureListener){
        db.collection(COLLECTION_NAME).document(id).delete()
                .addOnSuccessListener(successes)
                .addOnFailureListener(failureListener);
    }

    //get item list from the collection
    public void getAll(OnSuccessListener<QuerySnapshot> successListener, OnFailureListener failureListener){
       db.collection(COLLECTION_NAME).get()
               .addOnSuccessListener(successListener)
               .addOnFailureListener(failureListener);
    }
}
