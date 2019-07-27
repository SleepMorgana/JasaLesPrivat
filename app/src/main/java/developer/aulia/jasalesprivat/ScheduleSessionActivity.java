package developer.aulia.jasalesprivat;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.support.v4.app.NotificationCompat;
import android.app.Notification;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.ByteArrayViaString;
import developer.aulia.jasalesprivat.CalendarDB;
import developer.aulia.jasalesprivat.CalendarObject;
import developer.aulia.jasalesprivat.CalendarObjectList;
import developer.aulia.jasalesprivat.EventListHandler;
import developer.aulia.jasalesprivat.Serializer;
import developer.aulia.jasalesprivat.users.User;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;


import com.bumptech.glide.Glide;

import static developer.aulia.jasalesprivat.RequestSessionActivity.mTutorFlag;

public class ScheduleSessionActivity extends AppCompatActivity {


    private User tutor;
    private Context mContext=this;
    public static final String mTutorFlag = "selectedItem";
        private HttpURLConnection connection = null;
        private static boolean hasCreatedNotification = false;
        private static String urlServer = "https://calendarplusproject.herokuapp.com/CalendarS";
        private static String mEmail = "";
        private static boolean isLoggedIn = false;
        NavigationView navigationView = null;
        Toolbar toolbar = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_session2);
            //Enable the Up button


            // update the email
            String email = getIntent().getStringExtra("email");
            if (email != null) {
                mEmail = email;
            }
            Log.e("restart main", "email is " + mEmail);

            /*if (!hasCreatedNotification) {
                hasCreatedNotification = true;
                notificationController = new NotificationController(this);
            }
            notificationController.scheduleNotification();*/

            /* Set the fragment initially */
            CalendarViewFragment fragment = new CalendarViewFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();

            /* Toolbar */
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            /* Floating action button */
            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

            //Retrieve data sent from previous activity (i.e. tutor user selected in previous activity in this case)
            final User selected_tutor = Objects.requireNonNull(getIntent().getExtras()).getParcelable("selected_tutor");





            /* Add Snackbar on click */
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//              Snackbar.make(view, "Add Event", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    Intent intent = new Intent(ScheduleSessionActivity.this, AddEventActivity.class);
                    intent.putExtra(RequestSessionActivity.mTutorFlag,selected_tutor);
                    Bundle extras = new Bundle();
                    extras.putBoolean("IS_EDIT_EVENT",false);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });

            //Selected tutor user, probably through intent element, Telah memilih pengguna Tutor melalui element intent
            Intent intent = getIntent(); //Mendapatkan Intent
            tutor = intent.getParcelableExtra(mTutorFlag); //Mendapatkan data tambahan Tutor

            //Render the tutor's identity (i.e. username in this activity), Berikan identitas Tutor (yaitu username di aktivitas ini)
            TextView text_view = findViewById(R.id.username_profile_id);
            text_view.setText(tutor.getUsername());

            final List<String> availableSubjects = tutor.getSubjectNames();//dapatkan nama subjek Tutor

            /* Navigation drawer */
            /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            navigationView = (NavigationView) findViewById(R.id.nav_view);

            /* Change email in the nav drawer programatically */
            //View headerView = navigationView.getHeaderView(0);
            //TextView emailText = (TextView) headerView.findViewById(R.id.email);
            //if (!mEmail.equals("")) {
                //emailText.setText("Logged in as: " + mEmail);
            //} else {
                //emailText.setText("You are not currently logged in...");
            //}

            //navigationView.setNavigationItemSelectedListener(this);
        }


        @Override
        public void onBackPressed() {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);

            // Change individual navigationView menu item color, Only change the first item "Today" to pink
//        MenuItem item = menu.findItem(R.id.nav_calender_view);
//        SpannableString spanString = new SpannableString(item.getTitle().toString());
//        spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0);
//        item.setTitle(spanString);

            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

            return super.onOptionsItemSelected(item);
        }

        public void refreshCalendar() {
            CalendarViewFragment fragment = new CalendarViewFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        }
        /*@Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_calender_view) {
                //Set the fragment initially
                refreshCalendar();

            } else if (id == R.id.nav_add_event) {
                Intent intent = new Intent(ScheduleSessionActivity.this, AddEventActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean("IS_EDIT_EVENT",false);
                intent.putExtras(extras);
                startActivity(intent);

            } else if (id == R.id.nav_start_new) {

                // restart everything
                EventListHandler.clearAllLists();
                try {
                    CalendarDB.initDBLocal(this);
                } catch (IOException e) {
                    Log.e("Error", e.getMessage());
                }
                notificationController.scheduleNotification();

                // Display the calendar view fragment again
                CalendarViewFragment fragment = new CalendarViewFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction =
                        getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
            } else if (id == R.id.nav_sync) {

                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }*/

        @Override
        public void onResume() {
            super.onResume();

            Log.d("onResume", "resumed!");
            // refresh calendar on resume, gotta refresh multiple times for bug fix
            refreshCalendar();

        }


        @Override
        public void onPause() {
            super.onPause();

            // refresh calendar on resume
            refreshCalendar();

            String s1 = getIntent().getStringExtra("isLoggedIn");
            if ((s1 != null) && (s1.equals("true"))) {
                isLoggedIn = true;
            }

            String s2 = getIntent().getStringExtra("email");
            if (s2 != null) {
                mEmail = s2;
            }

            try {
                Log.e("onPause", "yes");

                // Save lists from EventListHandler to database
                CalendarDB.updateListLocal(0, EventListHandler.getStaticList(), this);
                CalendarDB.updateListLocal(1, EventListHandler.getDynamicList(), this);
                CalendarDB.updateListLocal(2, EventListHandler.getDeadlineList(), this);
                CalendarDB.updateListLocal(3, EventListHandler.getFinishedDynamicList(), this);

            } catch (Exception e) {
                Log.e("Save", e.toString());
            }
        }

        public class UploadDataTask extends AsyncTask<Void, Void, Boolean> {

            private String email;

            UploadDataTask(String email) {
                this.email = email;
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                ArrayList<CalendarObjectList<? extends AbstractCollection<? extends CalendarObject>, ? extends CalendarObject>> list = new ArrayList<>();
                list.add(EventListHandler.getStaticList());
                list.add(EventListHandler.getDynamicList());
                list.add(EventListHandler.getDeadlineList());
                list.add(EventListHandler.getFinishedDynamicList());

                try {
                    byte[] bytes = Serializer.serialize(list);
                    Log.e("byte size: ", "" + bytes.length);
                    String uploadString = ByteArrayViaString.byteArrayToString(bytes);

                    URL url = new URL(urlServer);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");

                    Uri.Builder builder = new Uri.Builder().appendQueryParameter("email", email)
                            .appendQueryParameter("command", "uploadData")
                            .appendQueryParameter("backup", uploadString);
                    String query = builder.build().getEncodedQuery();

                    PrintWriter writer = new PrintWriter(connection.getOutputStream());
                    writer.write(query);
                    writer.flush();
                    writer.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String result = in.readLine();
                    connection.disconnect();

                    Log.e("upload result", result);

                } catch (Exception e) {
                    Log.e("bug here at upload data", e.toString());
                }

                return true;
            }
        }

    }
