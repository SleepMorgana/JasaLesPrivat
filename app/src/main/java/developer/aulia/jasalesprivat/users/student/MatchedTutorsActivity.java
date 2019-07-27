package developer.aulia.jasalesprivat.users.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alphabetik.Alphabetik;
import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.utils.SearchListViewAdapter;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchedTutorsActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_tutors_activiyt);

        //Enable the Up button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // Get a support ActionBar corresponding to this toolbar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }



        //Get data sent from previous activity
        List<String> subjects_id_searched = getIntent().getExtras().getStringArrayList("subjects_id");

        //Retrieve tutors teaching at least one of subjects from subjects_id_searched
        getMatchedTutors(subjects_id_searched, new OnSuccessListener() {
            @Override
            public void onSuccess(final Object o) {
                TextView instructions_listview = (TextView) findViewById(R.id.instructions_id);
                final Map<User, List<String>> matched_tutors = (Map<User, List<String>>) o;
                final List<Pair<User, String>> user_info_list = new ArrayList<>();
                final List<String> tutors_with_matched_subjects = new ArrayList<>(); //Tutors usernames, for alphabet scroller
                Pair<User, String> temp_pair;

                //Display a custom instruction, depending on whether tutors matched the search criteria or not
                if (matched_tutors.size() == 0) { //No tutors matched the search criteria
                    instructions_listview.setText(R.string.instructions_match1);

                } else { //Tutors matched the search criteria
                    instructions_listview.setText(R.string.instructions_match2);
                }

                for (Map.Entry<User, List<String>> entry : matched_tutors.entrySet()) {
                    temp_pair = new Pair<>(entry.getKey(), entry.getValue().toString().substring(1,entry.getValue().toString().length()-1));
                    if(!user_info_list.contains(temp_pair)) {
                        user_info_list.add(temp_pair);
                        tutors_with_matched_subjects.add(entry.getKey().getUsername());
                    }
                }

                final ListView listView = findViewById(R.id.listView);
                //Data need to be ordered for the alphabetical scroller to work properly
                Collections.sort(user_info_list, new Comparator<Pair<User, String>>() {
                    @Override
                    public int compare(Pair<User, String> o1, Pair<User, String> o2) {
                        return o1.first.getUsername().compareTo(o2.first.getUsername());
                    }
                });
                listView.setAdapter(new SearchListViewAdapter(getBaseContext(), user_info_list));

                //Set alphabet relevant with the subjects' names
                Alphabetik alphabetik = findViewById(R.id.alphSectionIndex);
                String[] alphabet = Util.getCustomAlphabetList(tutors_with_matched_subjects);
                alphabetik.setAlphabet(alphabet);

                alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
                    @Override
                    public void onItemClick(View view, int position, String character) {
                        Collections.sort(tutors_with_matched_subjects);
                        //Toast.makeText(getBaseContext(), info, Toast.LENGTH_SHORT).show();
                        listView.smoothScrollToPosition(Util.getPositionFromData(character, tutors_with_matched_subjects));
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Get the selected item text from ListView
                        int pos = parent.getPositionForView(view);

                        // Start a new activity to diplay the user profile of the selected tutor
                        Intent intent = new Intent(MatchedTutorsActivity.this, ViewTutorProfileActivity.class);
                        intent.putExtra("selected_tutor", user_info_list.get(pos).first);
                        startActivity(intent);
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Util.printToast(MatchedTutorsActivity.this,"There were issues loading" +
                        " the list of tutors with subjects. Try again later or contact the administrator", Toast.LENGTH_SHORT);
            }
        });


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



    private Map<User, List<String>> getMatchedTutors(final List<String> subject_id, final OnSuccessListener successListener, OnFailureListener failureListener) {
        final Map<User, List<String>> res = new HashMap<>();

        UserManager.retrieveTutorsWithSubjects(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //Only retain tutors that match the user criteria (subjects in the user
                // learning needs pr subjects selected by the user)
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    User user = new User(doc);

                    List<String> matched_subjects = new ArrayList<>();

                    // Iterate over the list of subjects in search criteria
                    for (String subject_id_item:subject_id) {
                        if (user.getSubjects().containsKey(subject_id_item)) {
                            matched_subjects.add(user.getSubjects().get(subject_id_item).getName());
                        }
                    }

                    if (!res.containsKey(user)){
                        res.put(user, matched_subjects);
                    }
                }
                successListener.onSuccess(res);
            }
        },failureListener);

        return res;
    }
}

