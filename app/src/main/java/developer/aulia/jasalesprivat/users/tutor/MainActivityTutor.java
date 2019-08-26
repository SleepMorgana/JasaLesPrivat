package developer.aulia.jasalesprivat.users.tutor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.SignInSignUp;


import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.users.UserProfileActivity;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

//Activity ini digunakan untuk tampilan activity awal tutor/pengajar
public class MainActivityTutor extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private User user;
    private View headerView;
    private TextView info_sessions;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tutor);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        Button sign_out_button = findViewById(R.id.log_out_button_id); //Sign out button
        info_sessions = (TextView) findViewById(R.id.intro_future_sessions_id);
        listView = findViewById(R.id.listView);

        //Aktifkan tombol up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        user = UserManager.getUserInstance().getUser();

        /*Secara default gambar profil adalah avatar gender-netral. Jika pengguna yang masuk tidak memiliki
        gambar profil yang terkait dengan profilnya, ini harus ditampilkan, bukan avatar default */
        if (user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) headerView.findViewById(R.id.profile_pic_id);
            profile_pic_view.setImageBitmap(user.getProfile_picture());
        }

        //Tampilkan daftar les privat tutor yang ada saat ini
        Util.renderNUpcommingSessions(getBaseContext(), info_sessions, user, listView);

        //Perbarui menu navigasi dengan info pengguna yang masuk pada
        //Username
        TextView text_view = headerView.findViewById(R.id.username_nav_id);
        text_view.setText(user.getUsername());
        //Email
        text_view = headerView.findViewById(R.id.email_navigation_id);
        text_view.setText(user.getEmail());

        //Setiap kali pengguna mengklik header navbar, dia akan diarahkan ke halaman profilnya
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivityTutor.this, UserProfileActivity.class);
                //Data yang dikirim: saat ini login pengguna
                intent.putExtra("myCurrentUser", user);
                startActivity(intent);
            }
        });

        //User akan logout
        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserManager.signOut();

                //Kembali ke menu sign in/login
                Intent intent = new Intent(MainActivityTutor.this, SignInSignUp.class);
                startActivity(intent);
                finish();
            }
        });


    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //Ketika tombol kembali ditekan, aktivitas pada tumpukan dimulai ulang
        /* Secara default gambar profil adalah avatar gender-netral. Jika pengguna yang masuk tidak memiliki
        gambar profil yang terkait dengan profilnya, ini harus ditampilkan, bukan avatar default */
        if (user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) headerView.findViewById(R.id.profile_pic_id);
            profile_pic_view.setImageBitmap(user.getProfile_picture());
        }

        //Tampilkan sesi les privat
        Util.renderNUpcommingSessions(getBaseContext(), info_sessions, user, listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu; Hal ini menambahkan item ke action bar tindakan jika ada.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Menangani Action Bar item klik di sini. Action Bar akan
        //secara otomatis menangani klik pada tombol Home/up,
        //seperti yang ditentukan pada parent activity di AndroidManifest. XML.
        int id = item.getItemId();

        //noinspeksi SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Lihat navigasi menangani klik item di sini.
        int id = item.getItemId();

        if (id == R.id.nav_requests) {
           //pergi ke TutorSessionActivity
            Intent intent = new Intent(this,TutorSessionActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
