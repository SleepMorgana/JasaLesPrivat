package developer.aulia.jasalesprivat.users;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import developer.aulia.jasalesprivat.sessions.Session;
import developer.aulia.jasalesprivat.sessions.SessionManager;
import developer.aulia.jasalesprivat.sessions.Status;
import developer.aulia.jasalesprivat.subjects.Subject;
import developer.aulia.jasalesprivat.subjects.SubjectManager;
import developer.aulia.jasalesprivat.utils.Util;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//Menggunakan UserManager untuk mengatur state pada user yang sudah login
public class UserManager {
    private static UserController currentUser;
    private static final UserDatabaseHelper userDb = new UserDatabaseHelper();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);


    //berperan sebagai initializer
    private static void initCurrentUser(final User user, @NonNull final OnSuccessListener listener, @NonNull final OnFailureListener failureListener) throws RuntimeException {
        if (user == null)
            throw new RuntimeException("initialization user is null");
        //mendapatkan info dari user collection
        getDbInstance().getById(user.getId(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TUTOR_APP", "DocumentSnapshot data: " + document.getData());
                        currentUser = new UserController(new User(document));
                        refreshSessions(new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                refreshSubjects(new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        listener.onSuccess(currentUser);
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //subjects tidak diupdate tapi masih dapat ke activity selanjutnya
                                        listener.onSuccess(currentUser);
                                    }
                                });
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                failureListener.onFailure(e);
                            }
                        });
                    } else {
                        currentUser = new UserController(user);
                        //menyimpan pengguna saat ini dalam DB, karena belum dicatat
                        getDbInstance().upsert(user, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                listener.onSuccess(currentUser);
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                failureListener.onFailure(e);
                            }
                        });
                    }
                } else {
                    failureListener.onFailure(new InstantiationException("error saat inisialisasi"));
                    Log.d("TUTOR_APP", "user query berdasarkan ID gagal : ", task.getException());
                }
            }
        });
    }

    //Ketika mendaftar atau signIn metode initCurrentUser akan dipanggil untuk membuat currentUser.
    //Menggunakan UnsupportedOperationException untuk galat fatal, gunakan InstantiationException untuk peringatan
    public static void signupUser(FirebaseAuth firebaseAuth, String passwrd, String email, final String username,
                                  final Role role,
                                  Activity ctx, @NonNull final OnSuccessListener successListener, @NonNull final OnFailureListener failureListener) throws RuntimeException {
        if (firebaseAuth == null)
            firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseAuth finalFirebaseAuth = firebaseAuth;
        firebaseAuth.createUserWithEmailAndPassword(email, passwrd)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            finalFirebaseAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                User user = new User(finalFirebaseAuth.getCurrentUser());
                                                user.setUsername(username);
                                                user.setRole(role);

                                                if (role.equals(Role.TUTOR))
                                                    user.setStatus(Status.PENDING);

                                                initCurrentUser(user, new OnSuccessListener() {
                                                    @Override
                                                    public void onSuccess(Object o) {
                                                        successListener.onSuccess(currentUser.getUser());
                                                    }
                                                }, failureListener);
                                            } else {
                                                failureListener.onFailure(new UnsupportedOperationException("error"));
                                            }
                                        }
                                    });
                        } else {
                            if (failureListener != null)
                                failureListener.onFailure(new UnsupportedOperationException("error membuat database baru"));
                        }
                    }
                });

    }

    //Menggunakan UnsupportedOperationException untuk error yang fatal
    //Menggunakan InstantiationException untuk peringatan
    public static void signinUser(FirebaseAuth firebaseAuth, String passwrd, String email,
                                  Activity ctx, @NonNull final OnSuccessListener successListener, @NonNull final OnFailureListener failureListener) {
        if (firebaseAuth == null)
            firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseAuth finalFirebaseAuth = firebaseAuth;
        firebaseAuth.signInWithEmailAndPassword(email, passwrd)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //data pengguna telah diambil dan masuk
                            User user = new User(finalFirebaseAuth.getCurrentUser());
                            //Perhatian: initCurrentUser adalah operasi async, begitu segera setelah panggilan
                            //currentUser mungkin masih kosong
                            initCurrentUser(user, new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {

                                    //cek apabila tutor sudah diterima
                                    if (currentUser.getUser().getRole().equals(Role.TUTOR)
                                            && currentUser.getUser().getStatus().equals(Status.PENDING)) {
                                        failureListener.onFailure(new InstantiationException("akun tutor anda masih dalam tahap pending"));
                                        return;
                                    }

                                    if (currentUser.getUser().getRole().equals(Role.TUTOR)
                                            && currentUser.getUser().getStatus().equals(Status.DECLINED)) {
                                        failureListener.onFailure(new InstantiationException("akun tutor anda tidak dapat digunakan"));
                                        return;
                                    }

                                    successListener.onSuccess(currentUser.getUser());
                                }
                            }, failureListener);

                        } else {
                            failureListener.onFailure(new UnsupportedOperationException("error ketika membuat akun"));
                        }
                    }
                });
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
        currentUser = null;
    }

    public static UserController getUserInstance() {
        if (currentUser != null) { //current user = not equal
            return currentUser;
        }
        return currentUser;
    }

    public static UserController getUserInstance2() {

        return currentUser;
    }

    public static void registerObserver(Observer observer) throws RuntimeException {
        if (observer == null) {
            throw new RuntimeException("Tidak bisa mendaftarkan pengamat untuk pengguna saat ini");
        }
        currentUser.registerUserObserver(observer);
    }

    public static void resetPassword(FirebaseAuth firebaseAuth, final EditText email_input_field, final Context context) {
        String email = email_input_field.getText().toString().toLowerCase().trim();

        try {
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Email address sesuai dengan akun yang ada
                            if (task.isSuccessful()) {
                                Util.printToast(context, "Password reset email telah dikirim ke " +
                                        email_input_field.getText().toString().toLowerCase().trim(), Toast.LENGTH_LONG);
                                email_input_field.setText(""); //Clear input field: visual feedback to user in addition to the toast msg
                                //Email address tidak sesuai dengan akun yang ada
                            } else {
                                Util.printToast(context, task.getException().getMessage(), Toast.LENGTH_LONG);
                            }
                        }
                    });

        /* Sebuah argumen pengecualian illegal dilemparkan ketika alamat email dari firebase dicek dan akhirnya
          pengiriman email reset tidak diisi ( "string yang diberikan kosong atau null ") */
        } catch (IllegalArgumentException e) {
            Util.printToast(context, "Mohon isi email address", Toast.LENGTH_LONG);
        }
    }

    //Menambahkan objek sesi baru ke koleksi sesi
    //Memperbarui koleksi sesi pengirim dan Penerima
    public static void createSession(final Session session, @NonNull final OnSuccessListener<Void> success, @NonNull final OnFailureListener error) {
        if (session == null)
            error.onFailure(new UnsupportedOperationException("session anda null"));

        SessionManager.addNewSession(session, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //sender
                UserManager.addSession(session, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        //target
                        UserManager.addSession(session.getTarget(), session, success, error);
                    }
                }, error);
            }
        }, error);
    }

    //mengambil pengguna fro DB dan update sesi koleksi
    private static void addSession(@NonNull String userId, final Session session,
                                   @NonNull final OnSuccessListener success, @NonNull final OnFailureListener listener) {
        if (session == null || userId.equals("")) {
            listener.onFailure(new UnsupportedOperationException("user id atau entitas pelajaran tidak valid"));
            return;
        }

        getDbInstance().getById(userId, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = new User(task.getResult());
                    addSession(user, session, success, listener);
                } else {
                    listener.onFailure(new IllegalStateException());
                }
            }
        });
    }

    //Pemutakhiran pengumpulan sesi pengguna
    private static void addSession(User user, Session session, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure) {
        if (session == null || user == null) {
            failure.onFailure(new UnsupportedOperationException("user id atau entitas pelajaran tidak valid"));
            return;
        }
        user.addSession(session);
        getDbInstance().upsert(user, success, failure);
    }

    //Tambah sesi ke pengguna saat ini
    private static void addSession(Session session, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure) {
        addSession(currentUser.getUser(), session, success, failure);
    }

    //Menerima permintaan sesi: sesi harus menjadi bagian dari pengumpulan sesi pengguna
    public static void acceptSession(Session session, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure) {
        if (session == null || !currentUser.getUser().getSessions().contains(session)) {
            failure.onFailure(new UnsupportedOperationException("entitas pelajaran tidak valid"));
            return;
        }
        currentUser.getUser().getSessions().get(currentUser.getUser().getSessions().indexOf(session)).updateStatus(Status.ACCEPTED);
        session.updateStatus(Status.ACCEPTED);
        SessionManager.updateSession(session, success, failure);
    }

    //Menolak permintaan sesi: sesi harus menjadi bagian dari koleksi sesi pengguna
    public static void declineSession(Session session, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure) {
        if (session == null || !currentUser.getUser().getSessions().contains(session)) {
            failure.onFailure(new UnsupportedOperationException("entitas pelajaran tidak valid"));
            return;
        }
        currentUser.getUser().getSessions().get(currentUser.getUser().getSessions().indexOf(session)).updateStatus(Status.DECLINED);
        session.updateStatus(Status.DECLINED);
        SessionManager.updateSession(session, success, failure);
    }

    public static void addSessionDate(Session session, String timestamp, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure) {
        if (session == null || timestamp == null || !currentUser.getUser().getSessions().contains(session)) {
            failure.onFailure(new UnsupportedOperationException("entitas pelajaran tidak valid"));
            return;
        }
        session.addDate(timestamp);
        SessionManager.updateSession(session, success, failure);
    }

    //Menyegarkan elemen sesi pengguna
    public static void refreshSessions(@NonNull final OnSuccessListener success, @NonNull final OnFailureListener error) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (Object id : currentUser.getUser().getSessionIds()) {
                    //query untuk setiap sesi
                    SessionManager.retrieveSessionById((String) id, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                currentUser.getUser().addSession(new Session(task.getResult()));
                            } else {
                                error.onFailure(new UnsupportedOperationException());
                            }
                        }
                    });
                }
                while (currentUser.getUser().getSessions().size() !=
                        currentUser.getUser().getSessionIds().size()) ;

                success.onSuccess(null);
            }
        });
    }

    //Tambahkan subjek yang sudah ada ke pengguna saat ini
    public static void addSubject(Subject subject, OnSuccessListener success, OnFailureListener error) {
        currentUser.getUser().addSubject(subject);
        getDbInstance().upsert(currentUser.getUser(), success, error);
    }

    /**
     * Tambahkan subyek untuk pengguna saat ini
     *
     * @param subjects subyek yang akan ditambahkan ke pengguna saat ini
     * @param success  Listener disebut ketika tugas upsert berhasil diselesaikan
     * @param error    Listener disebut ketika upsert tugas tidak berhasil diselesaikan
     */
    public static void addSubjects(Map<String, Subject> subjects, final OnSuccessListener success, final OnFailureListener error) {
        currentUser.getUser().setSubjects(subjects);
        getDbInstance().upsert(currentUser.getUser(), success, error);
    }

    //Hapus subjek dari pengguna saat ini
    public static void removeSubject(Subject subject, OnSuccessListener success, OnFailureListener error) {
        currentUser.getUser().removeSubject(subject);
        getDbInstance().upsert(currentUser.getUser(), success, error);
    }

    //Segarkan subjek dari pengguna saat ini
    public static void refreshSubjects(@NonNull final OnSuccessListener success, @NonNull final OnFailureListener error) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> deleteSubjects = new ArrayList<>();
                for (final Map.Entry<String, Subject> entry : currentUser.getUser().getSubjects().entrySet()) {
                    SubjectManager.retrieveSubjectById((String) entry.getKey(), new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                //subject masih ada tapi tidak bisa diedit
                                Subject subject = currentUser.getUser().getSubjects().get(entry.getKey());
                                Subject tmp = new Subject(task.getResult());
                                if (!tmp.equals(subject)) {
                                    currentUser.getUser().removeSubject(subject);
                                    currentUser.getUser().addSubject(tmp);
                                    getDbInstance().upsert(currentUser.getUser(), new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    }, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                        }
                                    });
                                }
                            } else {
                                //subject telah dihapus, hapus subjek dari pengguna
                                deleteSubjects.add(entry.getKey());
                            }
                        }
                    });
                }

                //Hapus nama pelajaran
                for (String id : deleteSubjects) {
                    Subject subject = currentUser.getUser().getSubjects().get(id);
                    removeSubject(subject, new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                }

                success.onSuccess(currentUser.getUser());
            }
        });
    }

    //mengambil pengguna dari DB
    public static void retrieveUserById(@NonNull String userId, @NonNull final OnSuccessListener<User> success
            , @NonNull final OnFailureListener listener) {
        if (userId.equals("")) {
            listener.onFailure(new UnsupportedOperationException("user id tidak valid"));
            return;
        }

        getDbInstance().getById(userId, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = new User(task.getResult());
                    success.onSuccess(user);
                } else {
                    listener.onFailure(new IllegalStateException());
                }
            }
        });
    }

    //
    /*
        ADMIN COMMANDS
     */
    public static void acceptTutorRequest(User user, OnSuccessListener<Void> success, OnFailureListener error) {
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)) {
            //bukan admin, lakukan pengecualian
            return;
        }
        user.setStatus(Status.ACCEPTED);
        getDbInstance().upsert(user, success, error);
    }

    public static void declineTutorRequest(User user, OnSuccessListener<Void> success, OnFailureListener error) {
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)) {
            //bukan admin, lakukan pengecualian
            return;
        }
        user.setStatus(Status.DECLINED);
        getDbInstance().upsert(user, success, error);
    }

    public static void retrievePendingTutors(OnSuccessListener<QuerySnapshot> success, OnFailureListener error) {
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)) {
            //bukan admin, lakukan pengecualian
            return;
        }
        getDbInstance().getPendingTutors(success, error);
    }

    public static void retrieveTutorsWithSubjects(OnSuccessListener<QuerySnapshot> success, OnFailureListener error) {
        getDbInstance().getTutorsWithSubjects(success, error);
    }

    //Tambah subjek baru ke subject collection
    public static void createSubject(Subject subject, OnSuccessListener<Void> success, OnFailureListener error) {
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)) {
            //bukan admin, lakukan pengecualian
            return;
        }
        SubjectManager.addNewSubject(subject, success, error);
    }

    //Hapus subjek baru ke subject collection
    public static void deleteSubject(Subject subject, OnSuccessListener<Void> success, OnFailureListener error) {
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)) {
            //bukan admin, lakukan pengecualian
            return;
        }
        SubjectManager.removeSubject(subject, success, error);
    }

    //Update subject to the subject collection
    public static void updateSubject(Subject subject, OnSuccessListener<Void> success, OnFailureListener error) {
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)) {
            //bukan admin, lakukan pengecualian
            return;
        }
        SubjectManager.addNewSubject(subject, success, error);
    }

    private static UserDatabaseHelper getDbInstance() {
        return userDb;
    }

    //Hanya untuk pengujian, metode callback sulit untuk unit test
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Future<User> signupUser(FirebaseAuth firebaseAuth, String passwrd, String email,
                                          final String username, final Role role,
                                          Executor ctx) {
        final CompletableFuture<User> completableFuture = new CompletableFuture<>();
        if (firebaseAuth == null)
            firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseAuth finalFirebaseAuth = firebaseAuth;
        firebaseAuth.createUserWithEmailAndPassword(email, passwrd)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(finalFirebaseAuth.getCurrentUser());
                            user.setUsername(username);
                            user.setRole(role);


                            completableFuture.complete(user);
                        } else {
                            completableFuture.complete(null);
                        }
                    }
                });

        return completableFuture;
    }
}