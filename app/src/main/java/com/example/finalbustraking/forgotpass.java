package com.example.finalbustraking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class forgotpass extends AppCompatActivity {
    private EditText editText1;
    private MaterialButton fogpass;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpass);
        mAuth = FirebaseAuth.getInstance();
        editText1=findViewById(R.id.forgot_email);
        fogpass=findViewById(R.id.forgot_btn);
        fogpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailforgot=editText1.getText().toString();
                if (TextUtils.isEmpty(emailforgot)) {
                    Toast.makeText(forgotpass.this, "Enter all the fields properly ", Toast.LENGTH_SHORT).show();
                } else {
                    forgot(emailforgot);
                }
            }
        });
    }

    private void forgot(String emailforgot){
        mAuth.sendPasswordResetEmail(emailforgot)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(forgotpass.this,"check You email",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(forgotpass.this,login.class));
                            finish();
                        }
                        else{
                            Toast.makeText(forgotpass.this,"filed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}