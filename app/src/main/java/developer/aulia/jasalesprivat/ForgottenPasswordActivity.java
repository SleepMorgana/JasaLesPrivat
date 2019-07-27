package developer.aulia.jasalesprivat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import developer.aulia.jasalesprivat.users.UserManager;
import com.google.firebase.auth.FirebaseAuth;

public class ForgottenPasswordActivity extends AppCompatActivity {

    private static FirebaseAuth mAuth; // firebase authenticator
    Button reset_psswd_button;
    EditText input_email_edit_field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotten_password);

        //Enable the Up button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar(); // Get a support ActionBar corresponding to this toolbar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }


        reset_psswd_button = findViewById(R.id.reset_psswd_id);
        mAuth = FirebaseAuth.getInstance(); //Initialize Firebase Auth
        final Context context = this;
        reset_psswd_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_email_edit_field = (EditText) findViewById(R.id.input_email_id);

                UserManager.resetPassword(mAuth, input_email_edit_field, context);
            }
        });


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
}
