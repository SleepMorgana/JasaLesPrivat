package developer.aulia.jasalesprivat.users.student;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.alphabetik.Alphabetik;
import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.subjects.Subject;
import developer.aulia.jasalesprivat.subjects.SubjectManager;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.utils.CheckboxArrayAdapter;
import developer.aulia.jasalesprivat.utils.Util;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchTutorsActivity extends AppCompatActivity {

    private List<String> checked_subjects;
    private Pair<Map<String, Subject>, Map<String, Boolean>> pairOfMapSubjects;
    private CheckboxArrayAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tutors);

        Button search1 = (Button) findViewById(R.id.search_tutors_option1); //Tombol pencarian untuk mencari tutor berdasarkan kebutuhan belajar siswa
        Button search2_custom = (Button) findViewById(R.id.search_tutors_option2); //Tombol Cari untuk mencari tutor pada subyek yang dipilih siswa

        //Aktifkan fungsi tombol up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // Dapatkan ActionBar dukungan yang sesuai dengan Toolbar ini
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        //User yang login saat ini
        final User user = UserManager.getUserInstance().getUser();




        // Query semua mata pelajaran yang tersedia dalam aplikasi
        SubjectManager.listSubjects(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                final ArrayList<Subject> all_app_subjects = new ArrayList<>(); //Daftar nama pelajaran yang tersedia pada database
                for (DocumentSnapshot snapshot : queryDocumentSnapshots){
                    Subject subject = new Subject(snapshot);
                    all_app_subjects.add(subject);

                }

                //Menangani kasus di mana tidak ada pelajaran yang tersedia pada aplikasi
                if (all_app_subjects.size() == 0) {
                    Util.printToast(SearchTutorsActivity.this,"There is no subject available yet. Try again later or contact the administrator", Toast.LENGTH_LONG);
                }

                checked_subjects = user.getOrderedSubjects().first;

                /* Mempopulasikan dua peta dalam sepasang:
                   -Pertama peta (pertama ELT berpasangan): memetakan nama subjek ke objek Subject yang sesuai.
                     Prakondisi: nama subjek dalam basis data unik
                   -Kedua diurutkan peta (kedua ELT dalam pasangan): pemetaan nama subjek dengan Boolean menunjukkan apakah
                     Nama yang terkait dengan pengguna saat ini atau tidak
                     Prakondisi: nama subjek dalam basis data unik
                      NB: diurutkan peta karena daftar semua subyek perlu diurutkan untuk alfabet scroller bekerja */
                pairOfMapSubjects = Util.populateMappingUserSubject(checked_subjects, all_app_subjects);


                // implementasi Alphabetik
                Alphabetik alphabetik = findViewById(R.id.alphSectionIndex);
                final ListView listView=(ListView)findViewById(R.id.listView);
                adapter = new CheckboxArrayAdapter(SearchTutorsActivity.this,
                        pairOfMapSubjects.second.keySet().toArray(new String[0]),
                        pairOfMapSubjects.second);
                listView.setAdapter(adapter);
                listView.setItemsCanFocus(false);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); //List allows multiple choices

                //Set alphabet yang relevan dengan nama pelajaran
                String[] alphabet = Util.getCustomAlphabetSet(pairOfMapSubjects.second.keySet());
                alphabetik.setAlphabet(alphabet);

                alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
                    @Override
                    public void onItemClick(View view, int position, String character) {
                        List<String> ordered_data = new ArrayList<>(pairOfMapSubjects.first.keySet());
                        Collections.sort(ordered_data);
                        listView.smoothScrollToPosition(Util.getPositionFromData(character, ordered_data));
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Util.printToast(SearchTutorsActivity.this,"Failing to get the list subjects available within the application. Try again later or contact the administrator",Toast.LENGTH_LONG);
                Log.i(Util.TAG, e.getMessage());
            }
        });

        //Pencarian tutor
        search1.setOnClickListener(new View.OnClickListener() { //Search option 1
            @Override
            public void onClick(View v) {
                //Kriteria pencarian pengguna = nama pelajaran yang ia Daftarkan sebagai kebutuhan belajar
                List<String> subjects_id_criteria = new ArrayList<>();
                subjects_id_criteria.addAll(user.getSubjects().keySet());

                // Pergi ke activity selanjutnya dimana
                Intent intent = new Intent(SearchTutorsActivity.this, MatchedTutorsActivity.class);
                intent.putStringArrayListExtra("subjects_id", (ArrayList<String>) subjects_id_criteria);
                startActivity(intent);
            }
        });

        search2_custom.setOnClickListener(new View.OnClickListener() { //Search opton 1
            @Override
            public void onClick(View v) {
                //Kriteria pencarian pengguna = nama pelajaran yang ia pilih di activity search
                Intent intent = new Intent(SearchTutorsActivity.this, MatchedTutorsActivity.class);
                intent.putStringArrayListExtra("subjects_id", (ArrayList<String>)
                        getSubjectsId(pairOfMapSubjects.first, adapter.getSubject_map()));
                startActivity(intent);
            }
        });
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
     * Retrieve subjects' id from a mapping between subjects' name and whether these are checked by the user or not
     * @param map_name_subject mapping between subjects' name and Subject object
     * @param subject_map above-mentioned mapping
     * @return List of subjects' id checked by the user
     */
    private List<String> getSubjectsId(Map<String, Subject> map_name_subject, Map<String, Boolean> subject_map) {
        List<String> res = new ArrayList<>();
        List<String> selected_subject_names = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : subject_map.entrySet()) {
            if (entry.getValue()) {
                selected_subject_names.add(entry.getKey());
            }
        }

        for (String subject_name:selected_subject_names) {
            for (Map.Entry<String, Subject> entry : map_name_subject.entrySet()) {
                if (entry.getKey().equals(subject_name)) {
                    res.add(entry.getValue().getId());
                    break;
                }
            }
        }
        return res;
    }
}

