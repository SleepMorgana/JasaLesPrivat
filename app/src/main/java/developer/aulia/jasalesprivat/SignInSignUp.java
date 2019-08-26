package developer.aulia.jasalesprivat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import developer.aulia.jasalesprivat.admin.AdminMainActivity;
import developer.aulia.jasalesprivat.sessions.Status;
import developer.aulia.jasalesprivat.users.Role;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.users.student.MainActivityStudent;
import developer.aulia.jasalesprivat.users.tutor.MainActivityTutor;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignInSignUp extends AppCompatActivity {
    private static FirebaseAuth mAuth; // firebase authenticator
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} akan digunakan untuk membuat halaman fragment.
     */
    private ViewPager mViewPager;
    private TabLayout mTabLayout;




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_sign_up);
        //Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();



        mTabLayout = findViewById(R.id.tabs);

        // Membuat adapter yang dapat kembali ke fragment untuk setiap aktivitas
        // utama dari activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Mengatur ViewPager dengan section adapter.
        mViewPager = findViewById(R.id.container);
        populateViewPager();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }



    public void startMainActivity(){
        // ke activity home
        Intent intent = new Intent(this, MainActivityStudent.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Cek apabila pengguna sudah terdaftar (non-null) dan Update UI secara berkala.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //user has already logged in recently
            //TODO go to next activity
            //startMainActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Membuat fragments dan set ke ViewPager.
     */
    private void populateViewPager() {
        TabDetails tab;
        tab = new TabDetails("Masuk", PlaceholderFragment.newInstance(R.layout.sign_in_fragment));
        mSectionsPagerAdapter.addFragment(tab);
        tab = new TabDetails("Daftar", PlaceholderFragment.newInstance(R.layout.sign_up_fragment));
        mSectionsPagerAdapter.addFragment(tab);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

    }


    /**
     * PlaceholderFragment mengandung simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * Argumen fragment yang mewakili section number untuk
         * fragment.
         * TODO we may want to declare vars for the different error messages
         */
        private static final String ARG_LAYOUT = "layout";
        private final String TAG = "TUTOR_APP";
        static ProgressDialog mDialog;
        private OnSuccessListener signinSuccess = new OnSuccessListener() {
            @Override
            public void onSuccess(final Object o) {
                // Akun berhasil login/sign in, TODO go to next activity
                Log.d(TAG, "signInUserWithEmail:success");
                // Berhasil login sebagai admin, pergi ke halaman admin
                if (((User) o).getRole().equals(Role.ADMIN)) {
                    mDialog.dismiss();
                    startAdminMainActivity();
                    return;
                } else {
                    //Download gambar profil jika ada konversi bitmap dan perbarui atribut gambar profil yang sesuai di pengguna saat ini
                    FirebaseStorage.getInstance().getReference().
                            child("images/profile_picture_" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                                    .getUid()).getBytes(Long.MAX_VALUE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                        @Override
                        public void onComplete(@NonNull Task<byte[]> task) {
                            if (task.isSuccessful()) {
                                byte[] picture = task.getResult();
                                //Gambar profil dikonversi ke Bitmap
                                Bitmap bmp = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                                ((User) o).setProfile_picture(bmp);
                            }
                            mDialog.dismiss();
                            /* Apakah gambar profil download berhasil atau tidak. Di akhir
                              mungkin alasannya bahwa pengguna belum mengunggah profilnya sendiri.
                              Ketika tugas download tidak berhasil, pengguna akan mengakses
                              aplikasi dengan avatar netral gender (yang dapat diedit dalam aplikasi) */
                            startStudentorTutorMainActivity((User) o);
                        }
                    });
                }
            }
        };
        private OnFailureListener signinFailure = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Apabila sign in gagal, tampilkan pesan ke pengguna.
                Log.w(TAG, "signInUserWithEmail:failure", e);

                if (e instanceof UnsupportedOperationException)
                    Util.printToast(getActivity(), "Login gagal, mohon coba lagi.",Toast.LENGTH_SHORT);
                if (e instanceof java.lang.InstantiationException) {
                    Util.printToast(getActivity(), String.format("Login gagal: %s", e.getMessage()), Toast.LENGTH_SHORT);
                }
                mDialog.dismiss();
            }
        };


        private OnSuccessListener signupSuccess = new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                // Sign up sukses, ke activity selanjutnya, TODO add subject selection
                Log.d(TAG, "createUserWithEmail:success");
                Util.printToast(getActivity(), "Akun berhasil dibuat. Selamat menikmati", Toast.LENGTH_SHORT);
            }
        };
        private OnFailureListener signupFailure = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Apabila sign in gagal, tampilkan pesan ke pengguna.
                Log.w(TAG, "createUserWithEmail:failure", e);
                Util.printToast(getActivity(), "Sign up failed. Please try again.",Toast.LENGTH_SHORT);
            }
        };


        public PlaceholderFragment() {
        }

        /**
         * Mengembalikan sebuah instance baru dari fragmen ini untuk layout yang diberikan.
         */
        public static PlaceholderFragment newInstance(int layout) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT, layout);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            assert getArguments() != null;
            final View layout = inflater.inflate(getArguments().getInt(ARG_LAYOUT), container, false);
            int currentLayout = getArguments().getInt(ARG_LAYOUT);

            //Fragment sign in/login
            if (currentLayout == R.layout.sign_in_fragment) {
                Button signIn = layout.findViewById(R.id.sign_in_button_id);
                signIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText ed1 = layout.findViewById(R.id.input_sign_in_email);
                        String email = (ed1.getText()!=null)? ed1.getText().toString().toLowerCase().trim() : "";
                        EditText ed2 = layout.findViewById(R.id.passwd_input_sign_in_id);
                        String password = (ed2.getText()!=null)? ed2.getText().toString().toLowerCase().trim() : "";
                        //Input verifikasi
                        if (email.equals("") || password.equals("")) {
                            Util.printToast(getActivity(), "Sign in failed: empty fields", Toast.LENGTH_SHORT);
                            return;
                        }
                        Log.d(TAG, "SIGN_IN Clicked");
                        mDialog = Util.makeProgressDialog("","Loading..",getActivity());
                        mDialog.show();
                        UserManager.signinUser(mAuth,password,email,getActivity(),signinSuccess,signinFailure);
                    }
                });

                Button passButton = layout.findViewById(R.id.forgotten_passwd_id);
                passButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "FORGOT_PSSWD Clicked");
                        Intent intent = new Intent(getActivity(), ForgottenPasswordActivity.class);
                        startActivity(intent);
                    }
                });
            }

            //fragment untuk mendaftar akun
            if (currentLayout == R.layout.sign_up_fragment) {
                Button signUp = layout.findViewById(R.id.sign_up_button_id);
                RadioButton studentRadioButton = layout.findViewById(R.id.radioButton_student_id);
                RadioButton tutorRadioButton = layout.findViewById(R.id.radioButton_tutor_id);
                final Role[] role = {null};


                signUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        EditText ed1 = layout.findViewById(R.id.input_username_sign_up_id);
                        String username = (ed1.getText()!=null)? ed1.getText().toString().toLowerCase().trim() : "";
                        EditText ed2 = layout.findViewById(R.id.input_email_sign_up_id);
                        String email = (ed2.getText()!=null)? ed2.getText().toString().toLowerCase().trim() : "";
                        EditText ed3 = layout.findViewById(R.id.input_passwd_sign_up_id);
                        String password = (ed3.getText()!=null)? ed3.getText().toString().toLowerCase().trim() : "";
                        EditText ed4 = layout.findViewById(R.id.input_confirm_passwd_sign_up_id);
                        String confirmPassword = (ed4.getText()!=null)? ed4.getText().toString().toLowerCase().trim() : "";


                        Log.d(TAG, "SIGN_UP Clicked");

                        //check fields
                        if (username.equals("") || email.equals("") || password.equals("")  ) {
                            Util.printToast(getActivity(), "Periksa kembali field yang kosong", Toast.LENGTH_SHORT);
                            return;
                        }

                        if (!password.equals(confirmPassword)) {
                            Util.printToast(getActivity(), "Password dan Konfirmasi Password tidak cocok", Toast.LENGTH_SHORT);
                            return;
                        }

                        if (role[0]==null){
                            Util.printToast(getActivity(), "Mohon pilih salah satu peran",Toast.LENGTH_SHORT);
                            return;
                        }

                        UserManager.signupUser(mAuth,password,email,username, role[0],getActivity(),signupSuccess,signupFailure);
                    }
                });

                studentRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            role[0] = Role.STUDENT;
                            Log.d(TAG, "Peran murid telah dipilih");
                        }
                    }
                });

                tutorRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            role[0] = Role.TUTOR;
                            Log.d(TAG, "Peran tutor telah dipilih");
                        }
                    }
                });
            }
            return layout;
        }



        private void startAdminMainActivity(){
            // Pergi ke halaman AdminMainActivity
            Intent intent = new Intent(getActivity(), AdminMainActivity.class);
            startActivity(intent);
            Objects.requireNonNull(getActivity()).finish();
        }

        /**
         * Pergi ke halaman MainActivityStudent
         */
        private void startStudentMainActivity() {
            Intent intent = new Intent(getActivity(), MainActivityStudent.class);
            startActivity(intent);
        }

        /**
         * Pergi ke halaman MainActivityTutor
         */
        private void startTutorMainActivity() {
            Intent intent = new Intent(getActivity(), MainActivityTutor.class);
            startActivity(intent);
        }

        private void startStudentorTutorMainActivity(User my_user) {
            // Berhasil login sebagai murid, dapatkan informasi akun kemudian ke halaman murid
            if (my_user.getRole().equals(Role.STUDENT)) {
                startStudentMainActivity();
                Objects.requireNonNull(getActivity()).finish();

             // Berhasil login sebagai pengajar, dapatkan informasi akun kemudian ke halaman tutor
            } else if (my_user.getRole().equals(Role.TUTOR)) {
                startTutorMainActivity();
                Objects.requireNonNull(getActivity()).finish();
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} yang mengembalikan fragment yang sesuai untuk
     * salah satu bagian/tab/halaman.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<TabDetails> tabs = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return tabs.get(position).getFragment();
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

        private void addFragment(TabDetails tab) {
            tabs.add(tab);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs.get(position).getTabName();
        }
    }


}

