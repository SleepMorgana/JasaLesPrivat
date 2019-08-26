package developer.aulia.jasalesprivat.users;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.utils.ListViewItem;
import com.google.android.gms.tasks.OnSuccessListener;


public class UserItemView extends ListViewItem<User> {
    private View mView;
    private OnSuccessListener button1Action;// tombol action sukses
    private OnSuccessListener button2Action;


    public UserItemView(Context context, User user, int layout, OnSuccessListener action1, OnSuccessListener action2) {
        super(context,user,layout);
        this.button1Action = action1;
        this.button2Action = action2;
    }

    @Override
    public View getView() {
        mView=super.getView();

        //cek tipe layout: admin_user_list or normal user_list
        if (resourceLayout==R.layout.user_admin_item_layout){
            //kontrol admin akan dihadirkan
            TextView username = (TextView) mView.findViewById(R.id.username_item_id);
            TextView status = (TextView) mView.findViewById(R.id.status_item_id);
            Button acceptButton = (Button) mView.findViewById(R.id.item_accept_button);
            Button declineButton = (Button) mView.findViewById(R.id.item_decline_button);

            //butuh menambahkan status ke user model
            username.setText(element.getUsername());
            status.setText("Pending");
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button1Action.onSuccess(element);
                }
            });
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button2Action.onSuccess(element);
                }
            });
        }


        return mView;
    }

}
