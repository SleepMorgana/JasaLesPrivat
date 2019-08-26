package developer.aulia.jasalesprivat.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import developer.aulia.jasalesprivat.R;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Custom adapter untuk menampilkan nama subjek array (dapat diperiksa/dicentang)
 * Adapted from: Vogel L, MySimpleArrayAdapter.java from the android-examples GitHub repository,
 * https://github.com/vogellacompany/codeexamples-android/blob/master/com.vogella.android.test.traceview.list/src/de/vogella/android/listactivity/MySimpleArrayAdapter.java
 */
public class CheckboxArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values; //Sorted nama array pada daftar pelajaran
    private Map<String, Boolean> subject_map; /* Memetakan nama pelajaran dengan Boolean yang mengindikasikan apakah
    yang ditentukan oleh namanya terkait dengan pengguna saat ini atau tidak */

    public CheckboxArrayAdapter(Context context, String[] values, Map<String, Boolean> subject_map) {
        super(context, R.layout.checkboxrow, values);
        this.context = context;
        this.values = values;
        this.subject_map = subject_map;
    }

    @Nonnull
    @Override
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.checkboxrow, parent, false);
        final CheckBox checkbox=(CheckBox)rowView.findViewById(R.id.checkBox);
        checkbox.setText(values[position]);
        if (subject_map.get(values[position])) {
            checkbox.setChecked(true);
        }

        // Register listener
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject_map.put(checkbox.getText().toString(), checkbox.isChecked()); //Tidak dapat menggunakan map. replace karena panggilan tersebut memerlukan level API 24 (min saat ini adalah 19)
            }
        });

        return rowView;
    }

    public Map<String, Boolean> getSubject_map() {
        return subject_map;
    }
}