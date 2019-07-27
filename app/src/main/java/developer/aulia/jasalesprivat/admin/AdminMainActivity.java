package developer.aulia.jasalesprivat.admin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alphabetik.Alphabetik;
import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.SignInSignUp;
import developer.aulia.jasalesprivat.subjects.Subject;
import developer.aulia.jasalesprivat.subjects.SubjectManager;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.utils.DoubleActionListViewAdapter;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

public class AdminMainActivity extends AppCompatActivity implements Observer {
    private FrameLayout mContent;
    private LayoutInflater mInflater ;
    private boolean mPageFlag = true;// false=tutorPage
    private AdminMainActivity mActivity = this;
    public static String mItemSelected = "subject";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolbar);

        mContent = (FrameLayout) findViewById(R.id.content);
        mInflater = (LayoutInflater) getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_tutor_page);
        setTutorPage();
    }

    private void setTutorPage(){
        if (!mPageFlag){
            return;
        }
        // clean previous subject views
        clearContent();

        mContent.addView(mInflater.inflate(R.layout.activity_admin_user_list,null));
        final ListView listView  = (ListView) mContent.findViewById(R.id.admin_tutor_listview);

        fetchTutorData(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<User> tutors = new ArrayList<>();
                if (queryDocumentSnapshots.getDocuments().size()==0){
                    TextView textView = new TextView(mActivity);
                    textView.setText("No Pending Requests");
                    mContent.removeAllViews();
                    mContent.addView(textView);
                    return;
                }
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                    User user = new User(doc);
                    user.addObserver(mActivity);
                    tutors.add(user);
                    listView.setAdapter(new DoubleActionListViewAdapter(getBaseContext(), tutors, true,
                            new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    //o = element selected
                                    final User user = (User) o;
                                    Util.makeDialog("Accept Tutor", "You are about to accept the selected tutor request.",
                                            "Accept", "Cancel", mActivity, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    UserManager.acceptTutorRequest(user, new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            //OK
                                                            Util.printToast(mActivity,"Tutor request was accepted",Toast.LENGTH_SHORT);
                                                        }
                                                    }, new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            //error
                                                            Util.printToast(mActivity,"There was an error accepting tutor request",Toast.LENGTH_SHORT);
                                                        }
                                                    });
                                                    dialog.dismiss();
                                                }
                                            }, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    return;
                                                }
                                            }).show();

                                }
                            }, new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            final User user = (User) o;
                            Util.makeDialog("Decline Tutor", "You are about to decline the selected tutor request.",
                                    "Decline", "Cancel", mActivity, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //decline
                                            UserManager.declineTutorRequest(user, new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //OK
                                                    Util.printToast(mActivity, "Tutor request was declined", Toast.LENGTH_SHORT);
                                                }
                                            }, new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Util.printToast(mActivity, "There was an error declining tutor request", Toast.LENGTH_SHORT);
                                                }
                                            });
                                            dialog.dismiss();
                                        }
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            return;
                                        }
                                    }).show();

                        }
                    }));
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Util.printToast(mActivity,"There were issues loading the tutors list", Toast.LENGTH_SHORT);
            }
        });


        mPageFlag=false;
    }

    //Pending tutors
    private void fetchTutorData(OnSuccessListener<QuerySnapshot> success, OnFailureListener error){
        UserManager.retrievePendingTutors(success, error);
    }

    private void setSubjectPage(){
        if (mPageFlag)
            return;
        //clean previous user views
        clearContent();

        mContent.addView(mInflater.inflate(R.layout.activity_admin_subject_list,null));

        // Alphabetik implementation
        final Alphabetik alphabetik = mContent.findViewById(R.id.admin_subject_sectionindex);
        final ListView listView=(ListView)mContent.findViewById(R.id.admin_subject_listview);

        SubjectManager.listSubjects(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //List of subject names, used as ids in the following map
                final ArrayList<String> subjectNameList = new ArrayList();
                //map of subjects
                final Map<String,Subject> subjectList = new HashMap<>();

                //instantiating subjects
                for (DocumentSnapshot snapshot : queryDocumentSnapshots){
                    Subject subject = new Subject(snapshot);
                    subjectNameList.add(subject.getName());
                    subjectList.put(subject.getName(), subject);
                }

                //sort subject list
                Collections.sort(subjectNameList);

                final ArrayAdapter adapter = new ArrayAdapter<String>(mActivity,android.R.layout.simple_list_item_1,subjectNameList);
                listView.setAdapter(adapter);

                //Set alphabet relevant with the subjects' names
                String[] alphabet = Util.getCustomAlphabetList(subjectNameList);
                alphabetik.setAlphabet(alphabet);

                alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
                    @Override
                    public void onItemClick(View view, int position, String character) {
                        listView.smoothScrollToPosition(Util.getPositionFromData(character, subjectNameList));
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Subject subject = subjectList.get(subjectNameList.get(position));
                        //start edit/delete activity
                        Intent intent = new Intent(mActivity,CreateSubjectActivity.class);
                        intent.putExtra(mItemSelected,subject);
                        Log.d(Util.TAG, "subject: "+ subject.getId());
                        startActivity(intent);

                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Util.printToast(mActivity,"There were issues loading the subjects list", Toast.LENGTH_SHORT);
            }
        });

        FloatingActionButton fab_save = (FloatingActionButton) findViewById (R.id.fab_create_subject);
        fab_save.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View view) {
               //goto create subject activity
                Intent intent = new Intent(mActivity,CreateSubjectActivity.class);
                startActivity(intent);
            }
        });

        mPageFlag=true;
    }

    private void clearContent(){
        mContent.removeAllViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.admin_action_signout) {
            //signout
            UserManager.signOut();
            //give time for signout to be done
            Util.printToast(this,"Signin out...",Toast.LENGTH_LONG);
            new Thread() {
                @Override
                public void run() {
                    try {
                        super.run();
                        sleep(2000);  //Delay of 2 seconds
                    } catch (Exception e) {

                    } finally {
                        final Intent intent = new Intent(mActivity, SignInSignUp.class);
                        startActivity(intent);
                        Objects.requireNonNull(mActivity).finish();
                    }
                }
            }.start();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_tutor_page:
                    setTutorPage();
                    return true;
                case R.id.navigation_subject_page:
                    setSubjectPage();
                    return true;
            }
            return false;
        }

    };

    @Override
    public void update(Observable o, Object arg) {
        //Update UI

        //Update tutor page
//        if (o instanceof User){
//            mPageFlag = true;
//            setTutorPage();
//            //instead of fetching online maybe just update the local list
//            return;
//        }

    }
}
