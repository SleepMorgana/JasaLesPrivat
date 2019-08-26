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

        //Aktifkan tombol up pada toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // dapat dukungan ActionBar pada toolbar ini
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        //User saat ini
        user = UserManager.getUserInstance().getUser();

        //Render identitas pengguna
        updateUserIdentity(user);

        //Render subyek pengguna (kebutuhan belajar untuk murid dan Les subyek untuk tutor)
        renderSubjects(user);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //Ketika tombol kembali ditekan, aktivitas pada tumpukan dimulai ulang
        /* Secara default, foto profil adalah avatar netral gender, kecuali jika ia telah mengunggah
        gambar profil sendiri yang kemudian harus ditampilkan bukan default avatar */
        if (user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) findViewById(R.id.profile_picture_view_id);
            profile_pic_view.setImageBitmap(user.getProfile_picture());
        }

        //Render subyek pengguna (kebutuhan belajar untuk murid dan Les subyek untuk tutor)
        renderSubjects(user);
    }

    // buat button actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_tutor_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.editprofile_button_id:
                //Buka edit user profile activity
                Intent intent = new Intent(UserProfileActivity.this, UserProfileEditActivity.class);
                intent.putStringArrayListExtra("user_ordered_subject_names", (ArrayList<String>) orderedSubjects.first);
                startActivity(intent);
                return true;
            //Merespons tombol up/Home di Bar tindakan
            case android.R.id.home:
                finish(); // menutup aktivitas ini dan kembali ke aktivitas pratinjau (jika ada)
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Render identitas pengguna saat ini (yaitu username, alamat email, gambar profil)
     * @param populated_user saat ini login pengguna
     */
    private void updateUserIdentity(User populated_user) {
        /* Secara default, foto profil adalah avatar netral gender, kecuali jika ia telah mengunggah
        gambar profil sendiri yang kemudian harus ditampilkan bukan default avatar */
        if (populated_user.getProfile_picture() != null) {
            ImageView profile_pic_view = (ImageView) findViewById(R.id.profile_picture_view_id);
            profile_pic_view.setImageBitmap(populated_user.getProfile_picture());
        }

        //Update the layout dengan pengguna yang masuk
        //Username
        TextView text_view = findViewById(R.id.username_profile_id);
        text_view.setText(populated_user.getUsername());
        //Email
        text_view = findViewById(R.id.email_profile_id);
        text_view.setText(populated_user.getEmail());
    }

    /**
     * Render daftar pengguna saat ini subjek (kebutuhan belajar untuk mahasiswa, Les mata pelajaran untuk tutor)
     * @param populated_user saat ini login pengguna
     */
    private void renderSubjects(User populated_user) {
        final ListView listView = findViewById(R.id.listView); //Implementasi ListView, dengan daftar DATA yang diurutkan
        TextView instructions = findViewById(R.id.subjects_instructions_id);
        Alphabetik alphabetik = findViewById(R.id.alphSectionIndex);
        //Daftar alfabetis urutan kebutuhan belajar (siswa) atau Les mata pelajaran (tutor)
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

        // Menampilkan petunjuk tentang cara menambahkan subjek jika daftar subjek pengguna kosong
        if (orderedSubjects.first.size() == 0) {
            instructions.setText(R.string.no_subjects_specified);
            //Atur visibilitas
            //Tampilkan petunjuk untuk menambahkan subjek saat pengguna tidak memiliki subjek yang terkait dengan profilnya
            instructions.setVisibility(View.VISIBLE);
            //Sembunyikan alfabet scroller di sisi kanan + daftar item (memang, pengguna dapat Hapus centang semua subjeknya dan kembali untuk melihat profilnya)
            alphabetik.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);

        // Alphabetik implementation & ListView population
        } else {
            //Handle visibility
            instructions.setVisibility(View.GONE); //Sembunyikan petunjuk untuk menambahkan subjek saat pengguna tidak memiliki subjek yang terkait dengan profilnya
            alphabetik.setVisibility(View.VISIBLE); //Tunjukkan alphabet scroller
            listView.setVisibility(View.VISIBLE); //Tunjukkan nama pelajaran

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderedSubjects.first);
            listView.setAdapter(adapter);

            //Set alphabet yang relevan dengan nama pelajaran
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
