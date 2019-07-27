package developer.aulia.jasalesprivat.utils;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.sessions.SessionItemView;

import java.util.List;

public class DateListViewAdapter extends ListableViewAdapter {

    public DateListViewAdapter(Context a, List d) {
        super(a, d);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (data.size()>0) {
            return new SessionItemView(context, (Pair<String, String>) data.get(i), R.layout.session_item_layout).getView();
        }
        return null;
    }
}
