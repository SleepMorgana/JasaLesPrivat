package developer.aulia.jasalesprivat;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import developer.aulia.jasalesprivat.sessions.Session;
import developer.aulia.jasalesprivat.sessions.Status;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.utils.Util;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.timessquare.CalendarPickerView;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RequestSessionActivity extends AppCompatActivity {
    private User tutor;
    private Context mContext=this;
    public static final String mTutorFlag = "selectedItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session);//Gunakan konten dari layout activity_create_session.xml
        //Aktifkan tombol action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);//Mencari Toolbar pada activity_create_session.xml
        setSupportActionBar(toolbar);//Berikan Support Action Bar pada toolbar
        ActionBar ab = getSupportActionBar(); // Mendapatkan support ActionBar pada toolbar ini
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        //Digunakan untuk memilih pengguna Tutor melalui element intent
        Intent intent = getIntent(); //Mendapatkan Intent
        tutor = intent.getParcelableExtra(mTutorFlag); //Mendapatkan data tambahan Tutor

        //Berikan identitas Tutor (yaitu username di aktivitas ini)
        TextView text_view = findViewById(R.id.username_profile_id);
        text_view.setText(tutor.getUsername());
        String name = text_view.getText().toString();



        //Dates ListView, digunakan untuk membuat spinner dari subjek tutor yang tersedia
        ListView listView = (ListView) findViewById(R.id.session_date_listview);
        final ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new ArrayList<String>());
        listView.setAdapter(dateAdapter);

        //spinner elements, digunakan untuk konfigurasi pilihan subjek pelajaran di Spinner
        Spinner spinner = (Spinner) findViewById(R.id.subject_spinner);//ambil dari layout activity_create_session
        final List<String> availableSubjects = tutor.getSubjectNames();//dapatkan nama subjek Tutor
        //Session fields
        final String[] selectedSubject = {""};//Buat Variabel String untuk subjek Tutor
        final Map<String,String> selectedDates = new HashMap<>();//key = date; value = date+time

        //Menyambungkan Array Adapter ke Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,availableSubjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject[0] = availableSubjects.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //mengimplementasikan fitur date view
        final CalendarPickerView calendarView = (CalendarPickerView ) findViewById(R.id.calendar_view);
        //mendapatkan data dari tanggal yang sudah dipilih
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        Date today = new Date();

        calendarView.init(today,nextYear.getTime())
                .inMode(CalendarPickerView.SelectionMode.MULTIPLE);
        calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(final Date date) {
                //menambahkan dialog

                final EditText editText = new EditText(mContext);

                

                



                editText.setHint("HH:mm");
                Util.makeInputDialog("Time", "Select a time:", "Confirm", "Cancel", mContext,
                        editText,new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //OK
                                String timeString = (editText.getText()!=null)? editText.getText().toString().toLowerCase().trim() : "";
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");





                                Date time = null;
                                try {
                                    time = sdf.parse(timeString);//menambahkan seconds
                                } catch (ParseException e) {
                                    Util.printToast(getApplicationContext(),"Waktu tidak valid", Toast.LENGTH_SHORT);
                                    return;
                                }

                                //buat timestamp
                                Calendar dayCalendar = GregorianCalendar.getInstance();
                                dayCalendar.setTime(date); //menetapkan kalender pada tanggal yang ditentukan

                                Calendar timeCalendar = GregorianCalendar.getInstance();
                                timeCalendar.setTime(time);   // menetapkan kalender pada waktu yang ditentukan

                                Calendar finalDay = GregorianCalendar.getInstance(); // creates a new calendar instance
                                finalDay.set(dayCalendar.get(Calendar.YEAR),
                                        dayCalendar.get(Calendar.MONTH),dayCalendar.get(Calendar.DAY_OF_MONTH),
                                        timeCalendar.get(Calendar.HOUR_OF_DAY),timeCalendar.get(Calendar.MINUTE));

                                // menetapkan kalender untuk tanggal tertentu

                                selectedDates.put(dayCalendar.getTime().toString(),finalDay.getTimeInMillis()+"");
                                dateAdapter.add(finalDay.getTime().toString());
                                dialog.dismiss();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //CANCEL
                                dialog.dismiss();
                            }
                        }).show();
            }

            @Override
            public void onDateUnselected(Date date) {
                selectedDates.remove(date.toString());
                int flag = 0;
                for (int i=0; i<dateAdapter.getCount(); i++){
                    if (dateAdapter.getItem(i).startsWith(date.toString())){
                        flag=i;
                        break;
                    }
                }
                dateAdapter.remove(dateAdapter.getItem(flag));
            }
        });


        Button button = (Button) findViewById(R.id.request_session_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //membuat session (jadwal les) instance

                if (selectedSubject[0].equals("")){
                    Util.printToast(getApplicationContext(),"Harap pilih pelajaran untuk kegiatan les privat anda", Toast.LENGTH_SHORT);
                    return;
                }
                Session session = new Session(selectedSubject[0],UserManager.getUserInstance().getUser().getId(),
                        tutor.getId(), Status.PENDING);

                if (selectedDates.size()==0){
                    Util.printToast(getApplicationContext(),"Invalid day", Toast.LENGTH_SHORT);
                    return;
                }
                //Menambahkan timestamps ke session (jadwal les)
                for (Map.Entry<String,String> day : selectedDates.entrySet()){
                    session.addDate(day.getValue());
                }

                //Menyimpan Session
                UserManager.createSession(session, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Util.printToast(mContext,"Jadwal les berhasil ditambahkan, tunggu respon dari tutor", Toast.LENGTH_SHORT);
                        finish();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Util.printToast(mContext,"Adanya masalah ketika membuat jadwal les", Toast.LENGTH_SHORT);
                    }
                });
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Melakukan respon ke action bar
            case android.R.id.home:
                finish(); // menutup activity dan kembali ke activity sebelumnya (jika ada)
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*private void requestNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo_app)
                .setContentTitle("Pencari Jasa Les Privat")
                .setContentText("Sukses membuat jadwal les terbaru. Mohon menunggu respon dari pengajar");

        NotificationManager buatLes = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        buatLes.notify(0, builder.build());
    }*/
}
