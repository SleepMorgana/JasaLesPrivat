package developer.aulia.jasalesprivat.users.student;

import android.content.Intent;
import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.List;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.SignInSignUp;

import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.users.UserProfileActivity;
import developer.aulia.jasalesprivat.utils.Util;

//Akan digunakan sebagai main activity untuk murid
public class MainActivityStudent extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private User user;
    private View headerView;
    private TextView info_sessions;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_student);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        Button sign_out_button = findViewById(R.id.log_out_button_id);
        info_sessions = (TextView) findViewById(R.id.intro_future_sessions_id);
        listView = findViewById(R.id.listView);

        //Aktifkan tombol up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        user = UserManager.getUserInstance().getUser();

        /*Secara default gambar profil adalah avatar gender-netral. Jika pengguna yang masuk tidak memiliki
        gambar profil yang terkait dengan profilnya, ini harus ditampilkan, bukan avatar default*/
        if (user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) headerView.findViewById(R.id.profile_pic_id);
            profile_pic_view.setImageBitmap(user.getProfile_picture());
        }

        //Update menu navigasi dengan pengguna yang sudah login
        //Username
        TextView text_view = headerView.findViewById(R.id.username_nav_id);
        text_view.setText(user.getUsername());
        //Email
        text_view = headerView.findViewById(R.id.email_navigation_id);
        text_view.setText(user.getEmail());

        //Setiap kali pengguna mengklik header navbar, pengguna akan diarahkan ke halaman profilnya
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivityStudent.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        //Proses Sign Out
        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserManager.signOut();

                //Kembali ke activity Login/Logout
                Intent intent = new Intent(MainActivityStudent.this, SignInSignUp.class);
                startActivity(intent);
                finish();
            }
        });

        //Dapatkan hingga N sesi masa berikutnya
        Util.renderNUpcommingSessions(getBaseContext(), info_sessions, user, listView);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //Ketika tombol KEMBALI ditekan, aktivitas pada tumpukan dimulai ulang
        //*Secara default gambar profil adalah avatar gender-netral. Jika pengguna yang masuk tidak memiliki
        //gambar profil yang terkait dengan profilnya, ini harus ditampilkan, bukan avatar default */
        if (user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) headerView.findViewById(R.id.profile_pic_id);
            profile_pic_view.setImageBitmap(user.getProfile_picture());
        }

        //Dapatkan hingga N sesi masa berikutnya
        Util.renderNUpcommingSessions(getBaseContext(), info_sessions, user, listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu; Hal ini menambahkan item ke action bar jika ada.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //   Menangani aksi Bar item klik di sini. Action Bar akan
        //   secara otomatis menangani klik pada tombol Home/up,
        //   seperti yang Anda tentukan pada parent activity di AndroidManifest. XML.
        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Lihat navigasi untuk menangani klik item di sini.
        int id = item.getItemId();

        if (id == R.id.nav_search_tutors) {
            // Menangani tindakan tutor pencarian dalam kegiatan activity baru


                    //Kriteria pencarian pengguna = nama pelajaran yang ia daftarkan sebagai kebutuhan belajar
                    List<String> subjects_id_criteria = new ArrayList<>();
                    subjects_id_criteria.addAll(user.getSubjects().keySet());

                    // Ke activity selanjutnya dimana
                    Intent intent = new Intent(this, MatchedTutorsActivity.class);
                    intent.putStringArrayListExtra("subjects_id", (ArrayList<String>) subjects_id_criteria);
                    startActivity(intent);



        } else if (id == R.id.nav_filter_search) {
            // Menangani tindakan pencarian filter dalam activity khusus baru
            Intent intent_filterSearch = new Intent(this, SearchTutorsActivity.class);
            startActivity(intent_filterSearch);

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}

