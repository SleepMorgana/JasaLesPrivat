package developer.aulia.jasalesprivat.users;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alphabetik.Alphabetik;
import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.utils.Util;

import java.util.ArrayList;
import java.util.List;


public class UserProfileActivity extends AppCompatActivity {

    private Pair<List<String>, String[]> orderedSubjects;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Enable the Up button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // Get a support ActionBar corresponding to this toolbar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        //Current user
        user = UserManager.getUserInstance().getUser();

        //Render the user's identity
        updateUserIdentity(user);

        //Render the user's subjects (learning needs for subjects vs tutoring subjects for tutors)
        renderSubjects(user);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //When BACK BUTTON is pressed, the activity on the stack is restarted
        /*By default the profile picture is a gender-neutral avatar, unless he/she has uploaded his/her
        own profile picture which must then be displayed instead of the default avatar */
        if (user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) findViewById(R.id.profile_picture_view_id);
            profile_pic_view.setImageBitmap(user.getProfile_picture());
        }

        //Render the user's subjects (learning needs for subjects vs tutoring subjects for tutors)
        renderSubjects(user);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_tutor_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Edit button clicked, go to the edit profile activity
            case R.id.editprofile_button_id: //cf. menu folder
                // Go to the edit user profile activity
                Intent intent = new Intent(UserProfileActivity.this, UserProfileEditActivity.class);
                intent.putStringArrayListExtra("user_ordered_subject_names", (ArrayList<String>) orderedSubjects.first);
                startActivity(intent);
                return true;
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish(); // close this activity and return to preview activity (if there is any)
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
            ImageView profile_pic_view = (ImageView) findViewById(R.id.profile_picture_view_id);
            profile_pic_view.setImageBitmap(populated_user.getProfile_picture());
        }

        //Update the layout with the logged-in user's info
        //Username
        TextView text_view = findViewById(R.id.username_profile_id);
        text_view.setText(populated_user.getUsername());
        //Email
        text_view = findViewById(R.id.email_profile_id);
        text_view.setText(populated_user.getEmail());
    }

    /**
     * Render the current user's list of subjects (learning needs for a student, tutoring subjects for a tutor)
     * @param populated_user current logged-in user
     */
    private void renderSubjects(User populated_user) {
        final ListView listView = findViewById(R.id.listView); //Listview implementation, with SORTED list of DATA
        TextView instructions = findViewById(R.id.subjects_instructions_id);
        Alphabetik alphabetik = findViewById(R.id.alphSectionIndex);
        //Alphabetically ordered list of learning needs (student) or tutoring subjects (tutors)
        orderedSubjects = populated_user.getOrderedSubjects();

        // Title of the list depends on the role of the user
        TextView subject_list_title = findViewById(R.id.subject_list_name);
        switch (populated_user.getRole()) {
            case STUDENT:
                subject_list_title.setText(R.string.my_learning_needs_txt);
                break;
            case TUTOR:
                subject_list_title.setText(R.string.subjects_taught_txt);
                break;
        }

        // Display instructions on how to add subjects if the user's subject list is empty
        if (orderedSubjects.first.size() == 0) {
            instructions.setText(R.string.no_subjects_specified);
            //Manage visibility
            //Show instructions for adding subjects when the user has no subjects associated with his profile
            instructions.setVisibility(View.VISIBLE);
            //Hide alphabet scroller on the right side + List of item (indeed, a user can uncheck all his subjects and go back to view his profile)
            alphabetik.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);

        // Alphabetik implementation & ListView population
        } else {
            //Handle visibility
            instructions.setVisibility(View.GONE); //Hide instructions for adding subjects when the user has no subjects associated with his profile
            alphabetik.setVisibility(View.VISIBLE); //Show alphabet scroller
            listView.setVisibility(View.VISIBLE); //Show list of subjects

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderedSubjects.first);
            listView.setAdapter(adapter);

            //Set alphabet relevant with the subjects' names
            alphabetik.setAlphabet(orderedSubjects.second);

            alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
                @Override
                public void onItemClick(View view, int position, String character) {
                    String info = " Position = " + position + " Char = " + character;
                    Log.i("View: ", view + "," + info);
                    //Toast.makeText(getBaseContext(), info, Toast.LENGTH_SHORT).show();
                    listView.smoothScrollToPosition(Util.getPositionFromData(character, orderedSubjects.first));
                }
            });
        }
    }
}
