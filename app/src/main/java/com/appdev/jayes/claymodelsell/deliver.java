package com.appdev.jayes.claymodelsell;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class deliver extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    EditText receiptNo;
    EditText mobileno;
    EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver);


        receiptNo = (EditText) findViewById(R.id.receiptNo);
        mobileno = (EditText) findViewById(R.id.mobileno);
        name = (EditText) findViewById(R.id.name);


    }


    public void buttonFind(View view) {
        String findText;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/sales");
        Query q = null;

        if (receiptNo.hasFocus()) {
            q = mDatabase.child("2018").orderByChild("receiptNo").equalTo(receiptNo.getText().toString());
        } else if (mobileno.hasFocus()) {
            q = mDatabase.child("2018").orderByChild("mobile").startAt(mobileno.getText().toString());
        } else if (name.hasFocus()) {
            q = mDatabase.child("2018").orderByChild("name").startAt(name.getText().toString().toLowerCase());
        }
        if (q != null) {
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("Inside onDataChanged");
                    if (dataSnapshot.exists()) {
                        ;
                        // dataSnapshot is the "issue" node with all children with id 0
                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            // do something with the individual "issues"
                            System.out.println("Query : " + issue.getValue() + issue.getKey());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Database query error" + databaseError);


                }
            });
        }
    }
}
