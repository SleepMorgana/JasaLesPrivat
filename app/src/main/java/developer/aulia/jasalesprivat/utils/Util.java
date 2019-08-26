package developer.aulia.jasalesprivat.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import developer.aulia.jasalesprivat.R;

import developer.aulia.jasalesprivat.sessions.Session;
import developer.aulia.jasalesprivat.subjects.Subject;
import developer.aulia.jasalesprivat.users.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class Util {
    public static final String TAG = "JASALESPRIVAT";
    public static final int NB_UPCOMING_SESSION = 101; //Set kapasitas jadwal les hingga 100


    public static void printToast(Context context, String msg, int duration){
        Toast.makeText(context, msg,
                duration).show();
    }

    public static ProgressDialog makeProgressDialog(String title, String msg, Context ctx){
        ProgressDialog mDialog = new ProgressDialog(ctx);
        //Progress sign in bar
        mDialog.setTitle(title);
        mDialog.setMessage(msg);
        mDialog.setIndeterminate(false);
        mDialog.setCancelable(true);

        return mDialog;
    }

    public static Dialog makeDialog(String title, String msg, String positiveLabal, String negativeLabel, Context ctx,
                                    DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative){
        AlertDialog mDialog = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(positiveLabal, positive)
                .setNegativeButton(negativeLabel, negative)
                .create();

        return mDialog;
    }

    public static Dialog makeInputDialog(String title, String msg, String positiveLabal, String negativeLabel, Context ctx,
                                    EditText text, DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative) {
        AlertDialog mDialog = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(msg)
                .setView(text)
                .setPositiveButton(positiveLabal, positive)
                .setNegativeButton(negativeLabel, negative)
                .create();

        return mDialog;
    }

    /**
     * Transform daftar tanggal pada daftar 2-uple (tanggal di hari/bulan/tahun, waktu di HH: MM: SS)
     * @param dates_list daftar tanggal
     * @return Sesuaikan tanggal dengan daftar 2-uple (tanggal di hari/bulan/tahun, waktu di HH: MM: SS)
     */
    private static List<Pair<String, String>> transformListOfDates(List<Date> dates_list) {
        List<Pair<String, String>> res = new ArrayList<>();
        Pair<String, String> temp;

        for (Date d: dates_list) {
            temp = new Pair<>(new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH).format(d),
                    new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(d));

            res.add(temp);
        }

        return res;
    }

    /**
     * Render N sesi mendatang  "tanggal di layar (jika ada)
     * @param context konteks
     * @param info_sessions Info teks tentang N (jumlah sesi les privat) sesi mendatang
     * @param my_user Pengguna saat ini dengan tampilan sesi yang akan mendatang ditampilkan (jika ada)
     * @param customListView ListView di mana tanggal sesi mendatang ditampilkan (jika ada)
     */
    public static void renderNUpcommingSessions(Context context, TextView info_sessions, User my_user, ListView customListView) {
        ListView listView;

        //Dapatkan hingga N sesi masa berikutnya
        List<Date> upcoming_sessions = my_user.getNUpcomingSessionDates(Util.NB_UPCOMING_SESSION);

        if (upcoming_sessions.size() == 0) { //Pengguna tidak memiliki sesi les privat
            info_sessions.setText(R.string.no_upcoming_sessions_txt);
        } else {
            info_sessions.setText(R.string.upcoming_sessions_txt);

            //Transformasi List<Date> menjadi List<Pair<String(ie Date), String(ie Time)>> untuk menggunakan DateListViewAdapter
            List<Pair<String, String>> upcoming_sessions_dates_list = transformListOfDates(upcoming_sessions);

            customListView.setAdapter(new DateListViewAdapter(context, upcoming_sessions_dates_list));
        }
    }

    /**
     * Render N sesi mendatang  "tanggal di layar (jika ada)
     * @param context konteks
     * @param info_sessions Info teks tentang N (jumlah sesi les privat) sesi mendatang
     * @param tutor_user Pengguna tutor dengan tampilan sesi yang akan mendatang ditampilkan (jika ada)
     * @param customListView ListView di mana tanggal sesi mendatang ditampilkan (jika ada)
     */
    public static void renderNUpcommingTutorSessions(Context context, TextView info_sessions, User tutor_user, ListView customListView) {
        ListView listView;
        FirebaseFirestore.getInstance().collection("app_users").document("email");


        //Dapatkan hingga N sesi masa berikutnya
        List<Date> upcoming_sessions = tutor_user.getNUpcomingSessionDates(Util.NB_UPCOMING_SESSION);

        if (upcoming_sessions.size() == 0) { //Tutor tidak memiliki sesi les privat
            info_sessions.setText(R.string.no_upcoming_sessions_txt2);
        } else {
            info_sessions.setText(R.string.upcoming_sessions_txt2);

            //Transformasi List<Date> menjadi List<Pair<String(ie Date), String(ie Time)>> untuk menggunakan DateListViewAdapter
            List<Pair<String, String>> upcoming_sessions_dates_list = transformListOfDates(upcoming_sessions);

            customListView.setAdapter(new DateListViewAdapter(context, upcoming_sessions_dates_list));
        }
    }


    public static int getPositionFromData(String character, List<String> orderedData) {
        int position = 0;
        for (String s : orderedData) {
            String letter = "" + s.charAt(0);
            if (letter.equals("" + character)) {
                return position;
            }
            position++;
        }
        return 0;
    }

    /**
     * Menciptakan array memerintahkan huruf unik yang sesuai dengan huruf yang digunakan sebagai karakter pertama
     * dalam nama item
     * @param items Sortir nama pelajaran
     * @return memerintahkan array huruf unik yang sesuai dengan huruf yang digunakan sebagai karakter pertama
     * dalam nama item
     */
    public static String[] getCustomAlphabetSet(Set<String> items) {
        Set<String> first_letters = new HashSet<>();
        String[] res;

        for (String item:items) {
            first_letters.add(item.substring(0, 1).toUpperCase());
        }

        res = first_letters.toArray(new String[0]);
        //Arrays.sort(res);

        return(res);
    }

    public static String[] getCustomAlphabetList(List<String> items) {
        Set<String> first_letters = new HashSet<>();
        String[] res;

        for (String item:items) {
            first_letters.add(item.substring(0, 1).toUpperCase());
        }

        res = first_letters.toArray(new String[0]);
        Arrays.sort(res);

        return res;
    }

    /**
     * Mempopulasikan dua peta dalam sepasang:
     *-Pertama peta (pertama ELT dalam pasangan): pemetaan nama pelajaran dengan objek pelajaran yang sesuai.
     * Prakondisi: nama pelajaran dalam database unik
     *-Kedua diurutkan peta (kedua ELT dalam pasangan): pemetaan nama pelajaran dengan Boolean menunjukkan apakah
     * pelajaran yang ditunjuk oleh namanya terkait dengan pengguna saat ini atau tidak
     * Prakondisi: nama pelajaran dalam database unik
     * NB: diurutkan peta karena daftar semua mata pelajaran perlu diurutkan untuk alfabet scroller untuk bekerja
     * @param user_subjects Daftar nama pelajaran( nama pelajaran diasosiasikan dengan user
     * @param all_subjects Daftar semua nama pelajaran yang tersedia pada aplikasi
     * @return Pasangan yang disebutkan di atas
     */
    public static Pair<Map<String, Subject>, Map<String, Boolean>> populateMappingUserSubject(List<String> user_subjects, List<Subject> all_subjects) {
        Pair<Map<String, Subject>, Map<String, Boolean>> res;
        Map<String, Subject> subjectNameMap = new HashMap<>();
        Map<String, Boolean> subjectChecked = new TreeMap<>();

        //Nama pelajaran di res akan dipetakan dengan kondisi true (dicentang) apabila daftar pelajaran dengan
        //kondisi true sudah diasosiasikan dengan user
        for (Subject item : all_subjects) {
            if (user_subjects.contains(item.getName())) {
                subjectChecked.put(item.getName(), true);
            } else {
                subjectChecked.put(item.getName(), false);
            }
            subjectNameMap.put(item.getName(), item);
        }

        res = new Pair<>(subjectNameMap, subjectChecked);

        return res;
    }

    public static boolean isMyServiceRunning(Activity ctx, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /**
     *
     * @param pic_path
     * @return
     */
    public static Bitmap downloadProfilePic(String pic_path) {
        Bitmap res = null;
        FirebaseStorage.getInstance().getReference().child(pic_path).getBytes(Long.MAX_VALUE)
                .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        if (task.isSuccessful()) {
                            byte[] picture = task.getResult();
                            //Gambar akan dikonversi ke bitmap
                            Bitmap res = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                        }
                    }
                });

        return res;
    }


}


