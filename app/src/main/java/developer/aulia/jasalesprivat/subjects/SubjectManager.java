package developer.aulia.jasalesprivat.subjects;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;

public class SubjectManager {
    private static final SubjectDatabaseHelper db = new SubjectDatabaseHelper();
    //Buat nama pelajaran baru
    public static void addNewSubject(final Subject subject, @NonNull final OnSuccessListener<Void> successListener, @NonNull final OnFailureListener failureListener){
        if (subject==null){
            failureListener.onFailure(new NullPointerException("subject instance is null"));
            return;
        }

        retrieveSubjectsByName(subject.getName().trim(), new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //cek apabila dokumen ada

                if (queryDocumentSnapshots.size() > 0) {
                    //dokumen ada
                    failureListener.onFailure(new UnsupportedOperationException("subject with the same name already exists"));
                } else {
                    getDbInstance().upsert(subject, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            successListener.onSuccess(aVoid);
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            failureListener.onFailure(e);
                        }
                    });
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public static void removeSubject(Subject subject, @NonNull final OnSuccessListener<Void> successListener, @NonNull final OnFailureListener failureListener){
        if (subject==null){
            failureListener.onFailure(new UnsupportedOperationException("subject instance is null"));
            return;
        }
        //TODO A subject must also be deleted from user records
        getDbInstance().deleteById(subject.getId(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //menambahkan operasi batch pengguna
                successListener.onSuccess(aVoid);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                failureListener.onFailure(e);
            }
        });
    }

    public static void listSubjects(@NonNull final OnSuccessListener<QuerySnapshot> successListener, @NonNull final OnFailureListener failureListener){
        getDbInstance().getAll(successListener,failureListener);
    }

    public static void retrieveSubjectById(String id,@NonNull OnCompleteListener listener){
        if (id==null || id =="")
            return;
        getDbInstance().getById(id,listener);
    }

    public static void retrieveSubjectsByName(String name, @NonNull final OnSuccessListener<QuerySnapshot> successListener, @NonNull final OnFailureListener failureListener){
        if (name==null || name=="") {
            failureListener.onFailure(new UnsupportedOperationException("no name input was passed to the query"));
            return;
        }
        getDbInstance().getByName(name,successListener,failureListener);
    }

    private static SubjectDatabaseHelper getDbInstance(){
        return db;
    }
}
