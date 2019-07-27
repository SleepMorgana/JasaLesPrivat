package developer.aulia.jasalesprivat.users;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alphabetik.Alphabetik;
import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.subjects.Subject;
import developer.aulia.jasalesprivat.subjects.SubjectManager;
import developer.aulia.jasalesprivat.utils.CheckboxArrayAdapter;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserProfileEditActivity extends AppCompatActivity {

    private User user;
    private ImageView profile_picture;
    private final int RESULT_LOAD_IMAGE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);
        ImageView upload_profile_picture = findViewById(R.id.upload_pic_button_id);
        final FrameLayout frame = findViewById(R.id.frame);

        //Received data (ordered list of the user's subjects names), sent in the previous activity
        final Intent i = getIntent();
        final ArrayList<String> checked_subjects = i.getStringArrayListExtra("user_ordered_subject_names");

        //Enable the Up button
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // Get a support ActionBar corresponding to this toolbar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        //Current user
        user = UserManager.getUserInstance().getUser();

        //User action: uploading a new profile picture
        upload_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_picture = findViewById(R.id.profile_picture_edit_id);
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
            }
        });

        //Render the user's identity
        updateUserIdentity(user);

        /* Manage the list of the subjects associated with the user (add/remove subjects from the list )
           from the list of subjects available within the app (i.e. in the database) */
        // Query all subjects available within the app
        SubjectManager.listSubjects(new OnSuccessListener<QuerySnapshot>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                FloatingActionButton fab_save = (FloatingActionButton) findViewById (R.id.fab_save_id);
                final ArrayList<Subject> all_app_subjects = new ArrayList<>(); //List of all subjects available in the database
                for (DocumentSnapshot snapshot : queryDocumentSnapshots){
                    Subject subject = new Subject(snapshot);
                    all_app_subjects.add(subject);
                }

                //Hanfle the case where there is no subjects available within the app yet
                if (all_app_subjects.size() == 0) {
                    Util.printToast(UserProfileEditActivity.this,"There is no subject available yet. Try again later or contact the administrator",Toast.LENGTH_LONG);
                    //Hide save button
                    frame.setVisibility(View.GONE);
                }

                /* Populating two maps in a pair:
                   - First map (first elt in pair): Mapping subject names with the corresponding subject object.
                     Precondition: Subject names in the database are unique
                   - Second sorted map (second elt in pair): Mapping subject names with a boolean indicating whether the
                     subject designated by its names is associated with the current user or not
                     Precondition: Subject names in the database are unique
                      NB: Sorted map because the list of all subjects needs to be sorted for the alphabet scroller to work*/
                final Pair<Map<String, Subject>, Map<String, Boolean>> pairOfMapSubjects =
                        Util.populateMappingUserSubject(checked_subjects, all_app_subjects);

                // Alphabetik implementation
                Alphabetik alphabetik = findViewById(R.id.alphSectionIndex);
                final ListView listView=(ListView)findViewById(R.id.listView);
                final CheckboxArrayAdapter adapter = new CheckboxArrayAdapter(UserProfileEditActivity.this,
                        pairOfMapSubjects.second.keySet().toArray(new String[0]),
                        pairOfMapSubjects.second);
                listView.setAdapter(adapter);
                listView.setItemsCanFocus(false);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); //List allows multiple choices

                //Set alphabet relevant with the subjects' names
                String[] alphabet = Util.getCustomAlphabetSet(pairOfMapSubjects.second.keySet());
                alphabetik.setAlphabet(alphabet);

                alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
                    @Override
                    public void onItemClick(View view, int position, String character) {
                        List<String> ordered_data = new ArrayList<>(pairOfMapSubjects.first.keySet());
                        Collections.sort(ordered_data);
                        listView.smoothScrollToPosition(Util.getPositionFromData(character,ordered_data));
                    }
                });

                //User wants to save his/her changes
                fab_save.setVisibility(View.VISIBLE);
                fab_save.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick (View view) {
                        final Map<String, Subject> checked_subjects = new HashMap<>();
                        Map<String, Boolean> updatedUserSubjectMap = adapter.getSubject_map();
                        for (Map.Entry<String, Boolean> entry : updatedUserSubjectMap.entrySet()) {
                            if (entry.getValue()) {
                                checked_subjects.put(Objects.requireNonNull(pairOfMapSubjects.first.get(entry.getKey())).getId(), Objects.requireNonNull(pairOfMapSubjects.first.get(entry.getKey())));
                            }
                        }

                        // Adding subjects to current subject and saving them in the database
                        UserManager.addSubjects(checked_subjects, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //OK
                                Util.printToast(UserProfileEditActivity.this,"Your selection has been saved",Toast.LENGTH_SHORT);
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //error
                                Util.printToast(UserProfileEditActivity.this,"Failure: your selection could not be saved. Try again later or contact the administrator",Toast.LENGTH_LONG);
                            }
                        });
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Util.printToast(UserProfileEditActivity.this,"Failing to get the list subjects available within the application. Try again later or contact the administrator",Toast.LENGTH_LONG);
                Log.i(Util.TAG, e.getMessage()); //For debugging
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                profile_picture.setImageBitmap(selectedImage);
                //Rescale image
                profile_picture.getLayoutParams().height = (int) getResources().getDimension(R.dimen.profile_pic_width);
                profile_picture.getLayoutParams().height = (int) getResources().getDimension(R.dimen.profile_pic_height);

                //Update profile picture attribute in the user object
                user.setProfile_picture(selectedImage);

                //Save the new current user's profile picture to Firestore
                uploadImage(imageUri);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(UserProfileEditActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(UserProfileEditActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish(); // close this activity and return to preview activity (if there is any)
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Uploading a file (in this project a profile picture previously picked from the phone's Photos or Gallery app)
     * Reference: https://code.tutsplus.com/tutorials/image-upload-to-firebase-in-android-application--cms-29934
     * @param filePath LOcal Uri of the profile picture
     */
    private void uploadImage(Uri filePath) {

        if(filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/profile_picture_"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfileEditActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfileEditActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    /**
     * Render the current user's identity (i.e. username, email address, profile picture)
     * @param populated_user current logged-in user
     */
    private void updateUserIdentity(User populated_user) {
        /*By default the profile picture is a gender-neutral avatar, unless he/she has uploaded his/her
        own profile picture which must then be displayed instead of the default avatar */
        if (populated_user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) findViewById(R.id.profile_picture_edit_id);
            profile_pic_view.setImageBitmap(populated_user.getProfile_picture());
        }

        //Update navigation menu with the logged-in user's info
        //Username
        TextView text_view = findViewById(R.id.username_profile_id);
        text_view.setText(populated_user.getUsername());
        //Email
        text_view = findViewById(R.id.email_profile_id);
        text_view.setText(populated_user.getEmail());

        // Title of the list depends on the role of the user
        TextView subject_list_title = findViewById(R.id.subject_list_name_edit);
        switch (populated_user.getRole()) {
            case STUDENT:
                subject_list_title.setText(R.string.learning_needs_txt);
                break;
            case TUTOR:
                subject_list_title.setText(R.string.tutoring_subject_choice_txt);
                break;
        }
    }
}
