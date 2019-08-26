package developer.aulia.jasalesprivat.users;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alphabetik.Alphabetik;
import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.subjects.Subject;
import developer.aulia.jasalesprivat.subjects.SubjectManager;
import developer.aulia.jasalesprivat.utils.CheckboxArrayAdapter;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserProfileEditActivity extends AppCompatActivity {

    private User user;
    private ImageView profile_picture;
    private final int RESULT_LOAD_IMAGE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);
        ImageView upload_profile_picture = findViewById(R.id.upload_pic_button_id);
        final FrameLayout frame = findViewById(R.id.frame);

        //Mendapatkan data (nama subyek pengguna) yang dikirim dalam activity sebelumnya
        final Intent i = getIntent();
        final ArrayList<String> checked_subjects = i.getStringArrayListExtra("user_ordered_subject_names");

        //Aktifkan tombol up pada toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // dapat dukungan ActionBar pada toolbar ini
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        //User saat ini
        user = UserManager.getUserInstance().getUser();

        //User action: upload gambar profil baru
        upload_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_picture = findViewById(R.id.profile_picture_edit_id);
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
            }
        });

        //Render identitas pengguna
        updateUserIdentity(user);

        /* Mengelola daftar subyek yang terkait dengan pengguna (menambahkan/menghapus subyek dari daftar)
           dari daftar mata pelajaran yang tersedia dalam aplikasi (yaitu dalam database) */
        //Query semua mata pelajaran yang tersedia dalam aplikasi
        SubjectManager.listSubjects(new OnSuccessListener<QuerySnapshot>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                FloatingActionButton fab_save = (FloatingActionButton) findViewById (R.id.fab_save_id);
                final ArrayList<Subject> all_app_subjects = new ArrayList<>(); //List of all subjects available in the database
                for (DocumentSnapshot snapshot : queryDocumentSnapshots){
                    Subject subject = new Subject(snapshot);
                    all_app_subjects.add(subject);
                }

                //Lakukan penanganan apabila belum ada pelajaran yang tersedia
                if (all_app_subjects.size() == 0) {
                    Util.printToast(UserProfileEditActivity.this,"Belum ada subjek yang tersedia. Coba lagi nanti atau hubungi administrator",Toast.LENGTH_LONG);
                    //Hide save button
                    frame.setVisibility(View.GONE);
                }

                /* Mengisi dua peta dalam sepasang:
                   -Pertama peta (pertama ELT dalam pasangan): memetakan nama subjek dengan objek subjek yang sesuai.
                     Prakondisi: nama subjek dalam database unik
                   -Kedua diurutkan peta (kedua ELT dalam pasangan): pemetaan nama subjek dengan Boolean menunjukkan apakah
                     yang ditentukan oleh namanya terkait dengan pengguna saat ini atau tidak
                     Prakondisi: nama subjek dalam database unik
                      NB: diurutkan peta karena daftar semua subyek perlu diurutkan untuk alfabet scroller bekerja */
                final Pair<Map<String, Subject>, Map<String, Boolean>> pairOfMapSubjects =
                        Util.populateMappingUserSubject(checked_subjects, all_app_subjects);

                // implementasi Alphabetik
                Alphabetik alphabetik = findViewById(R.id.alphSectionIndex);
                final ListView listView=(ListView)findViewById(R.id.listView);
                final CheckboxArrayAdapter adapter = new CheckboxArrayAdapter(UserProfileEditActivity.this,
                        pairOfMapSubjects.second.keySet().toArray(new String[0]),
                        pairOfMapSubjects.second);
                listView.setAdapter(adapter);
                listView.setItemsCanFocus(false);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); //Daftar checkbox

                //Sortir alphabetik yang relevan dengan nama pelajaran
                String[] alphabet = Util.getCustomAlphabetSet(pairOfMapSubjects.second.keySet());
                alphabetik.setAlphabet(alphabet);

                alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
                    @Override
                    public void onItemClick(View view, int position, String character) {
                        List<String> ordered_data = new ArrayList<>(pairOfMapSubjects.first.keySet());
                        Collections.sort(ordered_data);
                        listView.smoothScrollToPosition(Util.getPositionFromData(character,ordered_data));
                    }
                });

                //User ingin menyimpan perubahan
                fab_save.setVisibility(View.VISIBLE);
                fab_save.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick (View view) {
                        final Map<String, Subject> checked_subjects = new HashMap<>();
                        Map<String, Boolean> updatedUserSubjectMap = adapter.getSubject_map();
                        for (Map.Entry<String, Boolean> entry : updatedUserSubjectMap.entrySet()) {
                            if (entry.getValue()) {
                                checked_subjects.put(Objects.requireNonNull(pairOfMapSubjects.first.get(entry.getKey())).getId(), Objects.requireNonNull(pairOfMapSubjects.first.get(entry.getKey())));
                            }
                        }

                        // Menambahkan subjek ke subjek saat ini dan menyimpannya dalam database
                        UserManager.addSubjects(checked_subjects, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //OK
                                Util.printToast(UserProfileEditActivity.this,"Pilihan Anda telah disimpan",Toast.LENGTH_SHORT);
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //error
                                Util.printToast(UserProfileEditActivity.this,"Gagal: pilihan Anda tidak dapat disimpan. Coba lagi nanti atau hubungi administrator",Toast.LENGTH_LONG);
                            }
                        });
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Util.printToast(UserProfileEditActivity.this,"Gagal untuk mendapatkan daftar mata pelajaran yang tersedia dalam aplikasi. Coba lagi nanti atau hubungi administrator",Toast.LENGTH_LONG);
                Log.i(Util.TAG, e.getMessage()); //For debugging
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                profile_picture.setImageBitmap(selectedImage);
                //Rescale gambar
                profile_picture.getLayoutParams().height = (int) getResources().getDimension(R.dimen.profile_pic_width);
                profile_picture.getLayoutParams().height = (int) getResources().getDimension(R.dimen.profile_pic_height);

                //Perbarui atribut gambar profil di objek pengguna
                user.setProfile_picture(selectedImage);

                //Simpan gambar profil pengguna baru saat ini ke Firestore
                uploadImage(imageUri);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(UserProfileEditActivity.this, "Ada kesalahan", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(UserProfileEditActivity.this, "Anda belum memilih gambar",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Merespons tombol up/Home di Action Bar
            case android.R.id.home:
                finish(); // menutup aktivitas ini dan kembali ke aktivitas pratinjau (jika ada)
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Mengunggah file (dalam proyek ini gambar profil yang sebelumnya dipilih dari aplikasi foto atau Galeri ponsel)
     * Referensi: https://code.tutsplus.com/tutorials/image-upload-to-firebase-in-android-application--cms-29934
     * @param filePath LOcal Url pada gambar profil
     */
    private void uploadImage(Uri filePath) {

        if(filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/profile_picture_"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfileEditActivity.this, "Sukses", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfileEditActivity.this, "Gagal "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
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
            ImageView profile_pic_view = (ImageView) findViewById(R.id.profile_picture_edit_id);
            profile_pic_view.setImageBitmap(populated_user.getProfile_picture());
        }

        //Update menu navigasi sesuai dengan user yang login saat ini
        //Username
        TextView text_view = findViewById(R.id.username_profile_id);
        text_view.setText(populated_user.getUsername());
        //Email
        text_view = findViewById(R.id.email_profile_id);
        text_view.setText(populated_user.getEmail());

        //Judul daftar tergantung pada peran pengguna
        TextView subject_list_title = findViewById(R.id.subject_list_name_edit);
        switch (populated_user.getRole()) {
            case STUDENT:
                subject_list_title.setText(R.string.learning_needs_txt);
                break;
            case TUTOR:
                subject_list_title.setText(R.string.tutoring_subject_choice_txt);
                break;
        }
    }
}
