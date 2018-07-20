package com.appdev.jayes.claymodelsell;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private EditText emailText;
    private EditText passText;

    ProgressDialog progressDialog;
    private static FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailText = (EditText) findViewById(R.id.editTextEmail);
        passText = (EditText) findViewById(R.id.editTextPass);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    progressDialog.cancel();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        };

    }

    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    public void loginButton(View view) {
        String email = emailText.getText().toString();
        String pass = passText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(Login.this, "Enter Values", Toast.LENGTH_LONG).show();

        } else {
            progressDialog.setTitle("Logging in");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.cancel();
                    if (!task.isSuccessful())
                        Toast.makeText(Login.this, "Incorrect email or passowrd", Toast.LENGTH_LONG).show();

                }
            });
        }
    }

    public void signUpButton(View view) {
        String email = emailText.getText().toString();
        String pass = passText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(Login.this, "Enter Values", Toast.LENGTH_LONG).show();

        } else {
            progressDialog.setTitle("Signing up user");
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.cancel();
                    if (!task.isSuccessful())
                        Toast.makeText(Login.this, "Error with signup", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void resetPassword(View view) {
        String email = emailText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Login.this, "Enter email address", Toast.LENGTH_LONG).show();

        } else {
            progressDialog.setTitle("Sending email to reset password");
            progressDialog.show();
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful())
                        Toast.makeText(Login.this, "Error resetting password", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(Login.this, "Reset instructions sent to your email addredd", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
