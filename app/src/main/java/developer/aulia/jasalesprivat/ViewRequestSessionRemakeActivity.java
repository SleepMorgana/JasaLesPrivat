package developer.aulia.jasalesprivat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.timessquare.CalendarPickerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import developer.aulia.jasalesprivat.sessions.Session;
import developer.aulia.jasalesprivat.sessions.Status;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.utils.Util;

public class ViewRequestSessionRemakeActivity extends AppCompatActivity {
    private Session session;
    private Context mContext;
    public static final String mSessionFlag = "selectedItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request_session);
        //Enable the Up button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // Get a support ActionBar corresponding to this toolbar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        mContext =this;

        //Selected session, probably through intent element
        Intent intent = getIntent();//TODO flag name must be changed
        session = intent.getParcelableExtra(mSessionFlag);

        //if the user sent the request show Target as the image user
        String userView = (UserManager.getUserInstance().getUser().getId().equals(session.getTarget()))?
                session.getSender(): session.getTarget();

        UserManager.retrieveUserById(userView, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                //Render the user's identity
                updateUserIdentity(user);
                //
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //There was an error getting the sender quit activity
                Util.printToast(mContext,"There was an issue retrieving the session's sender",Toast.LENGTH_LONG);
                finish();
            }
        });


        //Dates ListView
        ListView listView = (ListView) findViewById(R.id.session_date_listview);
        final ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new ArrayList<String>());
        listView.setAdapter(dateAdapter);

        //Session subject
        TextView subject = (TextView) findViewById(R.id.selected_subject);
        subject.setText(session.getSubject());

        //Set time
        List<Date> selectedDates = session.getDates();

        final CalendarPickerView calendarView = (CalendarPickerView) findViewById(R.id.calendar_view);
        //getting current
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        Log.d(Util.TAG,"Date: "+selectedDates.size());
        Log.d(Util.TAG,"TS: "+session.getTimestamps().size());
        if (selectedDates.size()>0)
            calendarView.init(selectedDates.get(0),nextYear.getTime())
                    .inMode(CalendarPickerView.SelectionMode.MULTIPLE);
        else
            calendarView.init(new Date(),nextYear.getTime())
                    .inMode(CalendarPickerView.SelectionMode.MULTIPLE);

        //Set dates
        for (Date date : selectedDates){
            calendarView.selectDate(date);
            dateAdapter.add(date.toString());
        }

        //Accept
        Button accept = (Button) findViewById(R.id.accept_request_button);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserManager.acceptSession(session, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Util.printToast(mContext,"Request accepted",Toast.LENGTH_LONG);
                        finish();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Util.printToast(mContext,"There was an issue accepting the request, please try again",Toast.LENGTH_LONG);
                    }
                });
            }
        });

        //Decline
        Button decline = (Button) findViewById(R.id.decline_request_button);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.makeDialog("You are about to decline this request"
                        , "If you wish to suggest another time, please get in touch with the student to ask for a change of time before declining",
                        "Continue", "Cancel", mContext, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Continue
                                UserManager.declineSession(session, new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Util.printToast(mContext,"The session was declined",Toast.LENGTH_LONG);
                                        finish();
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Util.printToast(mContext,"There was an issue declining the request, please try again",Toast.LENGTH_LONG);
                                    }
                                });
                                dialog.dismiss();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel
                                dialog.dismiss();
                                return;
                            }
                        }).show();
            }
        });

        //hide control button if the request is not PENDING
        if (!session.getStatus().equals(Status.PENDING)){
            accept.setVisibility(View.GONE);
            decline.setVisibility(View.GONE);
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

        //Update navigation menu with the logged-in user's info
        //Username
        TextView text_view = findViewById(R.id.username_profile_id);
        text_view.setText(populated_user.getUsername());
        //Email
//        text_view = findViewById(R.id.email_profile_id);
//        text_view.setText(populated_user.getEmail());
    }
}