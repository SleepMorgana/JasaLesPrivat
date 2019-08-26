package developer.aulia.jasalesprivat.users;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.utils.ListViewItem;


public class UserSearchItemView extends ListViewItem<Pair<User, String>> {

    public UserSearchItemView(Context context, Pair<User, String> info_tutor, int layout) {
        super(context,info_tutor,layout);
    }

    @Override
    public View getView() {
        View mView = super.getView();

        //cek layout search
        if (resourceLayout==R.layout.tutor_search_item_layout) {
            TextView username = (TextView) mView.findViewById(R.id.username_item_val_id);
            TextView subjects_list = (TextView) mView.findViewById(R.id.subject_list_id_val);

            //butuh menambahkan status ke user model
            username.setText(element.first.getUsername());
            subjects_list.setText(element.second);
        }

        return mView;
    }

}
