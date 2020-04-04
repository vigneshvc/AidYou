package com.saveetha.aidyou;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ContactFetchActivity extends AppCompatActivity {
    Button enteredContactDetails;
    EditText editenum1,editenum2,editenum3;
    SharedPreferences sharedPreferences;
    String enum1,enum2,enum3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_fetch);
    }

    @Override
    protected void onStart() {
        super.onStart();

        enteredContactDetails = findViewById(R.id.enteredDetails);
        editenum1 = findViewById(R.id.editEnum1);
        editenum2 = findViewById(R.id.editEnum2);
        editenum3 = findViewById(R.id.editEnum3);

        sharedPreferences = getSharedPreferences("PreferenceAidYou",0);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        enum1 = sharedPreferences.getString("EmergencyNum1","");
        enum2 = sharedPreferences.getString("EmergencyNum2","");
        enum3 = sharedPreferences.getString("EmergencyNum3","");
        if(!enum1.equals("")){
            editenum1.setText(enum1);
        }

        if(!enum2.equals("")){
            editenum2.setText(enum2);
        }
        if (!enum3.equals("")) {
            editenum3.setText(enum3);
        }
        enteredContactDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editenum1.getText().toString().equals("")){
                    editenum1.setError("Empty");
                }else if(editenum2.getText().toString().equals("")){
                    editenum2.setError("Empty");
                }else if(editenum3.getText().toString().equals("")){
                    editenum3.setError("Empty");
                }else {
                    editor.putString("EmergencyNum1", editenum1.getText().toString());
                    editor.putString("EmergencyNum2", editenum2.getText().toString());
                    editor.putString("EmergencyNum3", editenum3.getText().toString());
                    editor.putBoolean("isFirstTime", false);
                    editor.apply();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });


    }
}
