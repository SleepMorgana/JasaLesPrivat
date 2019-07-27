package developer.aulia.jasalesprivat.admin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import developer.aulia.jasalesprivat.R;
import developer.aulia.jasalesprivat.subjects.Subject;
import developer.aulia.jasalesprivat.users.UserManager;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class CreateSubjectActivity extends AppCompatActivity {
    private Context mContext;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        Intent intent = getIntent();
        final Subject subject = intent.getParcelableExtra(AdminMainActivity.mItemSelected);
        //check if subject was passed from previous activity
        //true: edit activity
        //false: create subject activity
        if (subject != null){
            //edit
            initEdit();
            final EditText ed1 = (EditText) findViewById(R.id.admin_edit_subject_edit);
            FloatingActionButton save = (FloatingActionButton) findViewById(R.id.admin_edit_subject_button);
            FloatingActionButton delete = (FloatingActionButton) findViewById(R.id.admin_delete_subject_button);
            ed1.setText(subject.getName());

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String name = (ed1.getText()!=null)? ed1.getText().toString().trim() : "";
                    Log.d(Util.TAG,"Edit button");
                    if (name.equals("")) {
                        Util.printToast(mContext, "Missing subject name", Toast.LENGTH_LONG);
                        return;
                    }
                    //save the edited subject
                    subject.setName(name);
                    UserManager.updateSubject(subject, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //OK
                            Util.printToast(mContext, "Edit was successful!", Toast.LENGTH_SHORT);
                            finish();
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error
                            Util.printToast(mContext, String.format("There were issues with the editing: %s",e.getMessage()), Toast.LENGTH_SHORT);
                        }
                    });
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String name = (ed1.getText()!=null)? ed1.getText().toString().trim() : "";
                    if (name.equals("")) {
                        Util.printToast(mContext, "Missing subject name", Toast.LENGTH_LONG);
                        return;
                    }
                    Util.makeDialog("Delete item", "You are about to delete the selected item.",
                            "Delete", "Cancel", mContext, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //delete subject
                                    UserManager.deleteSubject(subject, new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //OK
                                            Util.printToast(mContext, "Deletion was successful!", Toast.LENGTH_SHORT);
                                            finish();
                                        }
                                    }, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error
                                            Util.printToast(mContext, "There were issues with deleting the subject", Toast.LENGTH_SHORT);
                                        }
                                    });
                                    dialog.dismiss();
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //cancel deletion
                                    dialog.dismiss();
                                    return;
                                }
                            }).show();

                }
            });

        }else {
            //create
            initCreate();
            final EditText ed1 = (EditText) findViewById(R.id.admin_create_subject_edit);
            FloatingActionButton save = (FloatingActionButton) findViewById(R.id.admin_create_subject_button);

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String name = (ed1.getText()!=null)? ed1.getText().toString().trim() : "";
                    if (name.equals("")) {
                        Util.printToast(mContext, "Missing subject name", Toast.LENGTH_LONG);
                        return;
                    }
                    //save the new subject
                    Subject subject1 = new Subject(name);
                    Log.d(Util.TAG,"Save button");
                    UserManager.createSubject(subject1, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //OK
                            Util.printToast(mContext, "New subject was added successfully!", Toast.LENGTH_SHORT);
                            finish();
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error
                            Util.printToast(mContext, String.format("There were issues with the subject creation: %s", e.getMessage()), Toast.LENGTH_SHORT);
                        }
                    });
                }
            });
        }
    }

    private void initCreate(){
        setContentView(R.layout.activity_admin_add_subject);
        setToolbar();
    }

    private void initEdit(){
        setContentView(R.layout.activity_admin_edit_subject);
        setToolbar();
    }

    private void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }
}
