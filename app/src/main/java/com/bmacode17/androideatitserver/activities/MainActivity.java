package com.bmacode17.androideatitserver.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bmacode17.androideatitserver.R;

public class MainActivity extends AppCompatActivity {

    Button button_signIn;
    TextView textView_eatIt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_signIn = (Button) findViewById(R.id.button_signIn);
        textView_eatIt = (TextView) findViewById(R.id.textView_eatIt);

        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/myFont.ttf");
        textView_eatIt.setTypeface(face);
        button_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signInIntent = new Intent(MainActivity.this , SignIn.class);
                startActivity(signInIntent);
            }
        });
    }
}
