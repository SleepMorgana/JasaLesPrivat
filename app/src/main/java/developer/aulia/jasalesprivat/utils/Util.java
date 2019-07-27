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
import android.util.Pair;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import developer.aulia.jasalesprivat.R;

import developer.aulia.jasalesprivat.subjects.Subject;
import developer.aulia.jasalesprivat.users.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Util {
    public static final String TAG = "TUTOR-FINDER";
    public static final int NB_UPCOMING_SESSION = 5;

    public static void printToast(Context context, String msg, int duration){
        Toast.makeText(context, msg,
                duration).show();
    }

    public static ProgressDialog makeProgressDialog(String title, String msg, Context ctx){
        ProgressDialog mDialog = new ProgressDialog(ctx);
        //signin progress bar
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
     * Transform a list of dates on a list of 2-uple(Date in day/month/year, Time in HH:MM:SS)
     * @param dates_list list of dates
     * @return Corresponding list of 2-uple(Date in day/month/year, Time in HH:MM:SS)
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
     * Render the N upcoming sessions" dates on screen (if any)
     * @param context context
     * @param info_sessions text info about the N upcoming sessions
     * @param my_user current user whose upcomming sessions' dates are displayed (if any)
     * @param customListView listview in which upcoming sessions' dates are displayed (if any)
     */
    public static void renderNUpcommingSessions(Context context, TextView info_sessions, User my_user, ListView customListView) {
        ListView listView;

        //Get up to N next future sessions
        List<Date> upcoming_sessions = my_user.getNUpcomingSessionDates(Util.NB_UPCOMING_SESSION);

        if (upcoming_sessions.size() == 0) { //the user has no upcoming sessions
            info_sessions.setText(R.string.no_upcoming_sessions_txt);
        } else {
            info_sessions.setText(R.string.upcoming_sessions_txt);

            //Transform List<Date> into List<Pair<String(ie Date), String(ie Time)>> to use DateListViewAdapter
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
     * Creates an ordered array of  unique letters corresponding to the letters used as first characters
     * in the items name
     * @param items Sorted det of subjects' name
     * @return ordered array of  unique letters corresponding to the letters used as first characters
     * in the items name
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
     * Populating two maps in a pair:
     * - First map (first elt in pair): Mapping subject names with the corresponding subject object.
     *   Precondition: Subject names in the database are unique
     * - Second sorted map (second elt in pair): Mapping subject names with a boolean indicating whether the
     *   subject designated by its names is associated with the current user or not
     *   Precondition: Subject names in the database are unique
     *   NB: Sorted map because the list of all subjects needs to be sorted for the alphabet scroller to work
     * @param user_subjects List of subjects( names associated with a user
     * @param all_subjects List of all the subjects available in the app
     * @return The above-mentioned pair
     */
    public static Pair<Map<String, Subject>, Map<String, Boolean>> populateMappingUserSubject(List<String> user_subjects, List<Subject> all_subjects) {
        Pair<Map<String, Subject>, Map<String, Boolean>> res;
        Map<String, Subject> subjectNameMap = new HashMap<>();
        Map<String, Boolean> subjectChecked = new TreeMap<>();

        //Subject in res are mapped with true (i.e. checked) if it associated with the user
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

    /*public static void startQueueService(Activity ctx){
        if (Util.isMyServiceRunning(ctx, QueueService.class))
            return;
        //start queue service
        Intent tmpIntent = new Intent(ctx, QueueService.class);
        ctx.startService(tmpIntent);
    }

    public static void stopQueueService(Activity ctx){
        if (!Util.isMyServiceRunning(ctx,QueueService.class))
            return;
        //stop queue service
        Intent tmpIntent = new Intent(ctx, QueueService.class);
        ctx.stopService(tmpIntent);
    }*/

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
                            //Picture converted in Bitmap
                            Bitmap res = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                        }
                    }
                });

        return res;
    }

    public static int darker(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    /**
     * Lightens a color by a given factor.
     *
     * @param color
     *            The color to lighten
     * @param factor
     *            The factor to lighten the color. 0 will make the color unchanged. 1 will make the
     *            color white.
     * @return lighter version of the specified color.
     */
    public static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }
}


