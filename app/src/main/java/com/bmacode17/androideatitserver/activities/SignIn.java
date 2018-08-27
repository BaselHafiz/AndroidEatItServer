package com.bmacode17.androideatitserver.activities;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.commons.Common;
import com.bmacode17.androideatitserver.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignIn extends AppCompatActivity {

    TextView textView_phone , textView_password;
    Button button_signIn;
    ProgressBar myProgressBar;
    AlertDialog alertDialog;
    private static final String TAG = "Basel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        textView_password = (TextView) findViewById(R.id.textView_password);
        textView_phone = (TextView) findViewById(R.id.textView_phone);
        button_signIn = (Button) findViewById(R.id.button_signIn);

//      Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("user");

        button_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openAlertDialog();
                table_user.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // check if the user isn't exists in the database
                        if(dataSnapshot.child(textView_phone.getText().toString()).exists()){

                            // Get user information
                            alertDialog.dismiss();
                            User user = dataSnapshot.child(textView_phone.getText().toString()).getValue(User.class);
                            user.setPhone(textView_phone.getText().toString());
                            if(Boolean.parseBoolean(user.getIsStaff())){   // if isStaff = true

                                if(user.getPassword().equals(textView_password.getText().toString())){

                                    Intent homeIntent = new Intent(SignIn.this,Home.class);
                                    Common.currentUser = user;
                                    startActivity(homeIntent);
                                    finish();
                                }
                                else
                                    Toast.makeText(SignIn.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(SignIn.this, "Please loging with a STAFF account", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            alertDialog.dismiss();
                            Toast.makeText(SignIn.this, "User isn't exist in the database", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public void openAlertDialog() {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.myprogressdialog, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myProgressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
        alertDialog = myAlertDialog.create();
        alertDialog.show();

        try {
            Thread.sleep(2000);
        } catch (Exception e){}
    }
}
