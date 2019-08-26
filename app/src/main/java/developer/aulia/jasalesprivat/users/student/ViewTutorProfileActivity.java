package developer.aulia.jasalesprivat.users.student;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alphabetik.Alphabetik;
import com.bumptech.glide.Glide;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.RequestSessionActivity;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

public class ViewTutorProfileActivity extends AppCompatActivity {
    private Context mContext=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tutor_profile);

        //Aktifkan tombol up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // Dapatkan ActionBar dukungan yang sesuai dengan Toolbar ini
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        //Mengambil data yang dikirim dari activity sebelumnya (yaitu tutor pengguna yang dipilih dari activity sebelumnya)
        final User selected_tutor = Objects.requireNonNull(getIntent().getExtras()).getParcelable("selected_tutor");

        //Mengambil dan menampilkan foto profil tutor (jika ada) + menampilkan username
        updateUserIdentity(selected_tutor);

        //Merender daftar pelajaran pada tutor
        renderSubjects(selected_tutor);


        /*Button chatButton = findViewById(R.id.chat_button_id);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity
                Intent intent = new Intent(mContext, AddEventActivity.class);
                intent.putExtra(AddEventActivity.mTutorFlag,selected_tutor);
                startActivity(intent);
            }
        });*/

        //mulai activityy request session
        Button sessionButton = findViewById(R.id.session_request_button_id);
        sessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mulai activity request session
                Intent intent = new Intent(mContext, RequestSessionActivity.class);
                intent.putExtra(RequestSessionActivity.mTutorFlag,selected_tutor);
                startActivity(intent);
            }
        });

        /*Button checkscheduleButton = findViewById(R.id.session_check_activity);
        checkscheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity
                Intent intent = new Intent(mContext, ScheduleSessionActivity.class);
                intent.putExtra(RequestSessionActivity.mTutorFlag,selected_tutor);
                startActivity(intent);
            }
        });*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Merespon ke tombol Up/Home di action bar
            case android.R.id.home:
                finish(); // tutup activity and kembali ke activity sebelumnya (jika ada)
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Render identitas tutor (yaitu username, profil gambar)
     * NB: email pengguna tidak ditampilkan di sini untuk tujuan privasi. Seorang siswa dapat berkomunikasi dengan
     * tutor menggunakan sistem pesan
     * @param tutor_user
     */
    private void updateUserIdentity(User tutor_user) {
        /* Secara default, foto profil adalah avatar netral gender, kecuali jika ia telah mengunggah
        gambar profil sendiri yang kemudian harus ditampilkan bukan default avatar */
        //Referensi ke file gambar di Cloud Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/profile_picture_" +
                tutor_user.getId());

        // ImageView digunakan untuk foto tutor/pengajar
        final ImageView imageView = (ImageView) findViewById(R.id.profile_picture_view_id);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri.toString()).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(Util.TAG, "Not found");
            }
        });

        //Perbarui layout dengan info pengguna yang masuk pada
        //Username
        TextView text_view = findViewById(R.id.username_profile_id);
        text_view.setText(tutor_user.getUsername());
    }

    /**
     * Render daftar pengguna saat ini berdasarkan kebutuhuan pelajaran (daftar pelajaran yang dapat diajar oleh tutor)
     * @param populated_user pengguna tutor/pengajar
     */
    private void renderSubjects(User populated_user) {
        final ListView listView = findViewById(R.id.listView); //Listview implementation, with SORTED list of DATA
        Alphabetik alphabetik = findViewById(R.id.alphSectionIndex);
        //Daftar alfabetis urutan kebutuhan belajar (siswa) atau Les mata pelajaran (tutor)
        final Pair<List<String>, String[]> orderedSubjects = populated_user.getOrderedSubjects();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderedSubjects.first);
        listView.setAdapter(adapter);

        //Set alphabet yang relevan dengan nama pelajaran
        alphabetik.setAlphabet(orderedSubjects.second);

        alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
            @Override
            public void onItemClick(View view, int position, String character) {
                String info = " Position = " + position + " Char = " + character;
                Log.i("View: ", view + "," + info);

                listView.smoothScrollToPosition(Util.getPositionFromData(character, orderedSubjects.first));
            }
        });
    }
}

