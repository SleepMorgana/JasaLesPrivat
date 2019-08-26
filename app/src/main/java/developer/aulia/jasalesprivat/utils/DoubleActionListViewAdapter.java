package developer.aulia.jasalesprivat.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserItemView;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class DoubleActionListViewAdapter extends ListableViewAdapter {
    private OnSuccessListener action1;// Tombol action sukses merespon
    private OnSuccessListener action2;
    private boolean isAdmin;

    public DoubleActionListViewAdapter(Context a, List d, boolean isAdmin, OnSuccessListener action1, OnSuccessListener action2) {
        super(a, d);
        this.isAdmin = isAdmin;
        this.action1 = action1;
        this.action2 = action2;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (data.size()>0) {
            if (data.get(0) instanceof User && isAdmin)
                return new UserItemView(context, (User) data.get(i), R.layout.user_admin_item_layout, action1, action2).getView();
            //Menambahkan cases lain
            return null;
        }
        return null;
    }
}
