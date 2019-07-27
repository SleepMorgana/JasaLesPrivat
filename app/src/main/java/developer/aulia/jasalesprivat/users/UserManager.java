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

//Use the UserManager to manage the state of the logged-in user
public class UserManager {
    private static UserController currentUser;
    private static final UserDatabaseHelper userDb = new UserDatabaseHelper();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);


    //acts as an initializer
    private static void initCurrentUser(final User user,@NonNull final OnSuccessListener listener,@NonNull final OnFailureListener failureListener) throws RuntimeException {
        if (user == null)
            throw new RuntimeException("initialization user is null");
        //retrieve info from users collection
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
                                        //subjects weren't updated but still goto next activity
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
                        //save the current user in the db, since it wasn't recorded yet
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
                    failureListener.onFailure(new InstantiationException("error during user initialization"));
                    Log.d("TUTOR_APP", "user query by Id failed: ", task.getException());
                }
            }
        });
    }

    //When sign up or signIn the initCurrentUser method will be called to create the currentUser.
    //Use UnsupportedOperationException for fatal errors,  Use InstantiationException for warnings
    public static void signupUser(FirebaseAuth firebaseAuth, String passwrd, String email, final String username, final Role role,
                                  Activity ctx,@NonNull final OnSuccessListener successListener,@NonNull final OnFailureListener failureListener) throws RuntimeException {
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
                                            if (task.isSuccessful()){
                                                //verification email was sent
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
                                                },failureListener);
                                            }else{
                                                //problems with sending verification email
                                                failureListener.onFailure(new UnsupportedOperationException("error sending the email verification"));
                                            }
                                        }
                                    });
                        }else{
                            if (failureListener != null)
                                failureListener.onFailure(new UnsupportedOperationException("error creating new database user"));
                        }
                    }
                });

    }

    //Use UnsupportedOperationException for fatal errors
    //Use InstantiationException for warnings
    public static void signinUser(FirebaseAuth firebaseAuth, String passwrd, String email,
                                  Activity ctx,@NonNull final OnSuccessListener successListener,@NonNull final OnFailureListener failureListener) {
        if (firebaseAuth == null)
            firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseAuth finalFirebaseAuth = firebaseAuth;
        firebaseAuth.signInWithEmailAndPassword(email, passwrd)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user was retrieved and logged in
                            User user = new User(finalFirebaseAuth.getCurrentUser());
                            //ATTENTION: initCurrentUser is an async operation, so immedietly after call
                            //currentUser might be still null
                            initCurrentUser(user, new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {

                                    //check if tutor has been accepted
                                    if (currentUser.getUser().getRole().equals(Role.TUTOR)
                                            && currentUser.getUser().getStatus().equals(Status.PENDING)){
                                        failureListener.onFailure(new InstantiationException("your tutor registration request is still pending"));
                                        return;
                                    }

                                    if (currentUser.getUser().getRole().equals(Role.TUTOR)
                                            && currentUser.getUser().getStatus().equals(Status.DECLINED)){
                                        failureListener.onFailure(new InstantiationException("your tutor registration request has been declined"));
                                        return;
                                    }

                                    successListener.onSuccess(currentUser.getUser());
                                }
                            },failureListener);

                        }else{
                            failureListener.onFailure(new UnsupportedOperationException("error during user sign in process"));
                        }
                    }
                });
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
        currentUser = null;
    }

    public static UserController getUserInstance(){
        if (currentUser!=null){
            return currentUser;
        }
        return currentUser;
    }

    public static void registerObserver(Observer observer) throws RuntimeException {
        if (observer == null) {
            throw new RuntimeException("Couldn't register observer for the current user");
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
                            //Email address associated with an existing account
                            if (task.isSuccessful()) {
                                Util.printToast(context, "Password reset email sent to " +
                                        email_input_field.getText().toString().toLowerCase().trim(), Toast.LENGTH_LONG);
                                email_input_field.setText(""); //Clear input field: visual feedback to user in addition to the toast msg
                                //Email address not associated with an existing account
                            } else {
                                Util.printToast(context, task.getException().getMessage(), Toast.LENGTH_LONG);
                            }
                        }
                    });

        /*An illedgal argument exception is thrown when the email address which firebases checks to eventually
          send a reset email is not filled ("Given String is empty or null")*/
        } catch (IllegalArgumentException e) {
            Util.printToast(context, "Please, enter your email address",Toast.LENGTH_LONG);
        }
    }

    //Adds a new session object to the session collection
    //Updates the sender and receiver session collections
    public static void createSession(final Session session, @NonNull final OnSuccessListener<Void> success, @NonNull final OnFailureListener error){
        if (session==null)
            error.onFailure(new UnsupportedOperationException("session is null"));

        SessionManager.addNewSession(session, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //sender
                UserManager.addSession(session, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        //target
                        UserManager.addSession(session.getTarget(),session, success,error);
                    }
                }, error);
            }
        }, error);
    }

    //fetches user fro db and updates its session collection
    private static void addSession(@NonNull String userId, final Session session,
                                   @NonNull final OnSuccessListener success, @NonNull final OnFailureListener listener){
        if (session == null || userId.equals("")){
            listener.onFailure(new UnsupportedOperationException("Invalid user id or session entity"));
            return;
        }

        getDbInstance().getById(userId, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult()!=null){
                    User user = new User(task.getResult());
                    addSession(user,session,success,listener);
                }else{
                    listener.onFailure(new IllegalStateException());
                }
            }
        });
    }

    //Updates user session collection
    private static void addSession(User user, Session session, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure){
        if (session==null || user==null){
            failure.onFailure(new UnsupportedOperationException("Invalid user or session entities"));
            return;
        }
        user.addSession(session);
        getDbInstance().upsert(user,success,failure);
    }

    //add session to current user
    private static void addSession(Session session, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure){
        addSession(currentUser.getUser(), session, success, failure);
    }

    //Accept session request: session must be part of the user session collection
    public static void acceptSession(Session session, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure){
        if (session==null || !currentUser.getUser().getSessions().contains(session)){
            failure.onFailure(new UnsupportedOperationException("Invalid session entity"));
            return;
        }
        currentUser.getUser().getSessions().get(currentUser.getUser().getSessions().indexOf(session)).updateStatus(Status.ACCEPTED);
        session.updateStatus(Status.ACCEPTED);
        SessionManager.updateSession(session,success,failure);
    }

    //Decline session request: session must be part of the user session collection
    public static void declineSession(Session session, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure){
        if (session==null || !currentUser.getUser().getSessions().contains(session)){
            failure.onFailure(new UnsupportedOperationException("Invalid session entity"));
            return;
        }
        currentUser.getUser().getSessions().get(currentUser.getUser().getSessions().indexOf(session)).updateStatus(Status.DECLINED);
        session.updateStatus(Status.DECLINED);
        SessionManager.updateSession(session,success,failure);
    }

    public static void addSessionDate(Session session, String timestamp, @NonNull OnSuccessListener success, @NonNull OnFailureListener failure){
        if (session==null || timestamp==null || !currentUser.getUser().getSessions().contains(session)){
            failure.onFailure(new UnsupportedOperationException("Invalid session entity"));
            return;
        }
        session.addDate(timestamp);
        SessionManager.updateSession(session,success,failure);
    }

    //Refresh user's sessions elements
    public static void refreshSessions(@NonNull final OnSuccessListener success, @NonNull final OnFailureListener error){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for(Object id : currentUser.getUser().getSessionIds()){
                    //query for each session
                    SessionManager.retrieveSessionById((String) id, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                currentUser.getUser().addSession(new Session(task.getResult()));
                            }else {
                                error.onFailure(new UnsupportedOperationException());
                            }
                        }
                    });
                }
                while (currentUser.getUser().getSessions().size() !=
                        currentUser.getUser().getSessionIds().size());

                success.onSuccess(null);
            }
        });
    }

    //Add already existing subject to current user
    public static void addSubject(Subject subject, OnSuccessListener success, OnFailureListener error){
        currentUser.getUser().addSubject(subject);
        getDbInstance().upsert(currentUser.getUser(),success,error);
    }

    /**
     * Add subjects to current user
     * @param subjects Subjects to be added to the current user
     * @param success Listener called when upsert task completed successfully
     * @param error Listener called when upsert task was not successfully completed
     */
    public static void addSubjects(Map<String,Subject> subjects, final OnSuccessListener success, final OnFailureListener error){
        currentUser.getUser().setSubjects(subjects);
        getDbInstance().upsert(currentUser.getUser(),success,error);
    }

    //Remove subject from current user
    public static void removeSubject(Subject subject, OnSuccessListener success, OnFailureListener error){
        currentUser.getUser().removeSubject(subject);
        getDbInstance().upsert(currentUser.getUser(),success,error);
    }

    //Refresh user's subjects elements
    public static void refreshSubjects(@NonNull final OnSuccessListener success, @NonNull final OnFailureListener error){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> deleteSubjects = new ArrayList<>();
                for(final Map.Entry<String, Subject> entry : currentUser.getUser().getSubjects().entrySet()){
                    SubjectManager.retrieveSubjectById((String) entry.getKey(), new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                //subject exists but could have been edited
                                Subject subject = currentUser.getUser().getSubjects().get(entry.getKey());
                                Subject tmp = new Subject(task.getResult());
                                if (!tmp.equals(subject)){
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
                            }else {
                                //subject was deleted, delete subject from user
                                deleteSubjects.add(entry.getKey());
                            }
                        }
                    });
                }

                //delete subjects
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

    //fetches user from db
    public static void retrieveUserById(@NonNull String userId,@NonNull final OnSuccessListener<User> success
            , @NonNull final OnFailureListener listener){
        if (userId.equals("")){
            listener.onFailure(new UnsupportedOperationException("Invalid user id"));
            return;
        }

        getDbInstance().getById(userId, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult()!=null){
                    User user = new User(task.getResult());
                    success.onSuccess(user);
                }else{
                    listener.onFailure(new IllegalStateException());
                }
            }
        });
    }
    //
    /*
        ADMIN COMMANDS
     */
    public static void acceptTutorRequest(User user, OnSuccessListener<Void> success, OnFailureListener error){
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)){
            //not admin, could throw an exception
            return;
        }
        user.setStatus(Status.ACCEPTED);
        getDbInstance().upsert(user, success, error);
    }

    public static void declineTutorRequest(User user, OnSuccessListener<Void> success, OnFailureListener error){
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)){
            //not admin, could throw an exception
            return;
        }
        user.setStatus(Status.DECLINED);
        getDbInstance().upsert(user, success, error);
    }

    public static void retrievePendingTutors(OnSuccessListener<QuerySnapshot> success, OnFailureListener error){
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)){
            //not admin, could throw an exception
            return;
        }
        getDbInstance().getPendingTutors(success,error);
    }

    public static void retrieveTutorsWithSubjects(OnSuccessListener<QuerySnapshot> success, OnFailureListener error) {
        getDbInstance().getTutorsWithSubjects(success, error);
    }

    //Add new subject to the subject collection
    public static void createSubject(Subject subject, OnSuccessListener<Void> success, OnFailureListener error){
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)){
            //not admin, could throw an exception
            return;
        }
        SubjectManager.addNewSubject(subject, success, error);
    }

    //Remove subject from the database
    public static void deleteSubject(Subject subject, OnSuccessListener<Void> success, OnFailureListener error){
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)){
            //not admin, could throw an exception
            return;
        }
        SubjectManager.removeSubject(subject, success, error);
    }

    //Update subject to the subject collection
    public static void updateSubject(Subject subject, OnSuccessListener<Void> success, OnFailureListener error){
        if (!currentUser.getUser().getRole().equals(Role.ADMIN)){
            //not admin, could throw an exception
            return;
        }
        SubjectManager.addNewSubject(subject, success, error);
    }

    private static UserDatabaseHelper getDbInstance(){
        return userDb;
    }

    //Just for testing, callback methods are difficult to unit test
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Future<User> signupUser(FirebaseAuth firebaseAuth, String passwrd, String email, final String username, final Role role,
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
                            //setCurrentUser(user,null,null);
                            completableFuture.complete(user);
                        }else{
                            completableFuture.complete(null);
                        }
                    }
                });

        return completableFuture;
    }
}