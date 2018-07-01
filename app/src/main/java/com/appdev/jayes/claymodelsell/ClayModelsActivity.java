package com.appdev.jayes.claymodelsell;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class ClayModelsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private EditText modelName, modelPrice;
    private Button saveButton;

    private ListView listView;
    private ThreeViewAdapter mAdapter;
    private ArrayList<ClayModel> modelList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_models);

        mAuth = FirebaseAuth.getInstance();
        //get firebase user
        FirebaseUser user = mAuth.getCurrentUser();

        listView = (ListView) findViewById(R.id.models_list);
        mAdapter = new ThreeViewAdapter(this, modelList);
        listView.setAdapter(mAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/claymodels");

        modelName = (EditText) findViewById(R.id.modelName);
        modelPrice = (EditText) findViewById(R.id.modelPrice);
        saveButton = (Button) findViewById(R.id.save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClayModel model = new ClayModel(modelName.getText().toString(), modelPrice.getText().toString());
                mDatabase.push().setValue(model);
                modelName.setText(null);
                modelPrice.setText(null);
            }
        });

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println(dataSnapshot.getKey());
                ClayModel temp = dataSnapshot.getValue(ClayModel.class);
                temp.setKey(dataSnapshot.getKey());
                modelList.add(temp);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                modifyData((ClayModel) parent.getItemAtPosition(position), position);
                return false;
            }
        });

    }

    private void modifyData(final ClayModel modelData, final int position) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.modifymodel, null);

        final EditText etmodelName = (EditText) v.findViewById(R.id.mdName);
        final EditText etmodelPrice = (EditText) v.findViewById(R.id.mdPrice);
        etmodelName.setText(modelData.getModelName());
        etmodelPrice.setText(modelData.getModelPrice());

        new AlertDialog.Builder(this)
                .setTitle("Update")
                .setView(v)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ClayModel model = new ClayModel(etmodelName.getText().toString(), etmodelPrice.getText().toString());
                                mDatabase.child(modelData.getKey()).setValue(model);
                                modelList.set(position, new ClayModel(modelData.getKey(), etmodelName.getText().toString(), etmodelPrice.getText().toString()));
                            }
                        }
                )
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mDatabase.child(modelData.getKey()).removeValue();
                        modelList.remove(position);
                    }
                }).show();
    }
}



