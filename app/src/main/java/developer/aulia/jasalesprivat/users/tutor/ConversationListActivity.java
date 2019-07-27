package developer.aulia.jasalesprivat.users.tutor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import developer.aulia.jasalesprivat.R;

import developer.aulia.jasalesprivat.sessions.Session;
import developer.aulia.jasalesprivat.users.User;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConversationListActivity extends AppCompatActivity {
    private Context mContext = this;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        //Enable the Up button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // Get a support ActionBar corresponding to this toolbar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        //final List<String> subjects = getSubjects(UserManager.getUserInstance().getUser());
        final ListView listView = findViewById(R.id.listView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(adapter);

        final List<User> chatUsers = new ArrayList<>();
        getChatUsers(UserManager.getUserInstance().getUser(), new OnSuccessListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                List<String> chats = getAdapterLabel(users);
                chatUsers.addAll(users);
                Message message1 = new Message();
                message1.what = 1;
                message1.obj = chats;
                mHandler.sendMessage(message1);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Util.printToast(mContext,"There were issues retrieving your contacts, please try again", Toast.LENGTH_SHORT);
            }
        });

        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(ChatActivity.chatTarget, chatUsers.get(position));
                startActivity(intent);
            }
        });*/

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1)
                {// Update view component
                    adapter.addAll((Collection<? extends String>) msg.obj);
                    adapter.notifyDataSetChanged();
                    listView.setSelection(0);
                }
            }
        };

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish(); // close this activity and return to preview activity (if there is any)
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Generates list of elements fr=or the list view and array adapter
    private List<String> getAdapterLabel(List<User> users){
        List<String> labels = new ArrayList<>();
        for (User user : users){
            labels.add(user.getUsername());
        }
        return labels;
    }

    //Async call to db to retrieve usernames of contacts
    private void getChatUsers(final User user, final OnSuccessListener<List<User>> successListener,
                                    final OnFailureListener failureListener){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                final List<User> list = new ArrayList<>();
                for (final Session session : user.getAcceptedSessions()){
                    if (session.getSender() != user.getId())
                        UserManager.retrieveUserById(session.getSender(), new OnSuccessListener<User>() {
                            @Override
                            public void onSuccess(User user) {
                                list.add(user);
                            }
                        }, failureListener);
                    else
                        list.add(user);
                }
                while (list.size()!=user.getAcceptedSessions().size());
                successListener.onSuccess(list);
            }
        });
    }
}