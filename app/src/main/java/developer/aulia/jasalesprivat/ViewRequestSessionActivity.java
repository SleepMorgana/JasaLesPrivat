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

import developer.aulia.jasalesprivat.sessions.Session;
import developer.aulia.jasalesprivat.sessions.Status;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.timessquare.CalendarPickerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ViewRequestSessionActivity extends AppCompatActivity {
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
                Util.printToast(mContext,"Terjadi masalah ketika membuat jadwal les baru",Toast.LENGTH_LONG);
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

        //Set tanggal
        List<Date> selectedDates = session.getDates();

        final CalendarPickerView calendarView = (CalendarPickerView) findViewById(R.id.calendar_view);
        //mendapatkan waktu saat ini
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

        //Dapatkan tanggal
        for (Date date : selectedDates){
            calendarView.selectDate(date);
            dateAdapter.add(date.toString());
        }

        //Terima
        Button accept = (Button) findViewById(R.id.accept_request_button);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserManager.acceptSession(session, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Util.printToast(mContext,"Permintaan diterima",Toast.LENGTH_LONG);
                        finish();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Util.printToast(mContext,"Adanya masalah ketika menerima permintaan les, coba lagi",Toast.LENGTH_LONG);
                    }
                });
            }
        });

        //Tolak
        Button decline = (Button) findViewById(R.id.decline_request_button);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.makeDialog("Yakin menolak permintaan les?"
                        , "Calon murid anda mungkin akan mencari pengajar yang lain",
                        "Continue", "Cancel", mContext, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Lanjutkan
                                UserManager.declineSession(session, new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        Util.printToast(mContext,"Permintaan ditolak",Toast.LENGTH_LONG);
                                        finish();
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Util.printToast(mContext,"Adanya masalah ketika menolak permintaan les, mohon coba lagi",Toast.LENGTH_LONG);
                                    }
                                });
                                dialog.dismiss();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Batal
                                dialog.dismiss();
                                return;
                            }
                        }).show();
            }
        });

        //Sembunyikan tombol apabila permintaan les privat tidak PENDING
        if (!session.getStatus().equals(Status.PENDING)){
            accept.setVisibility(View.GONE);
            decline.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Merespon action bar pada tombol Up/Home
            case android.R.id.home:
                finish(); // menutup activity dan kembali ke activity sebelumnya (jika ada)
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Render identitas pengguna saat ini (yaitu nama pengguna, alamat email, gambar profil)
     * @param populated_user pengguna yang sudah login saat ini
     */
    private void updateUserIdentity(User populated_user) {
        /*Secara default, foto profil adalah avatar netral gender, kecuali jika ia telah mengunggah
        gambar profil sendiri yang kemudian harus ditampilkan, bukan avatar default */
        if (populated_user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) findViewById(R.id.profile_picture_view_id);
            profile_pic_view.setImageBitmap(populated_user.getProfile_picture());
        }

        //Update menu navigasi dengan info pengguna yang sudah login
        //Username
        TextView text_view = findViewById(R.id.username_profile_id);
        text_view.setText(populated_user.getUsername());
        //Email (tidak dipakai)
//        text_view = findViewById(R.id.email_profile_id);
//        text_view.setText(populated_user.getEmail());
    }
}