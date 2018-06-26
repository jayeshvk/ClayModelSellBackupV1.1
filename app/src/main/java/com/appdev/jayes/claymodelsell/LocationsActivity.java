package com.appdev.jayes.claymodelsell;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class LocationsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private EditText locationText;
    private Button saveButton;

    private ListView listView;
    //private LocationAdapter mAdapter;
    LocationAdapter mAdapter;
    private ArrayList<Location> locationsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        mAuth = FirebaseAuth.getInstance();
        //get firebase user
        FirebaseUser user = mAuth.getCurrentUser();

        listView = (ListView) findViewById(R.id.movies_list);
        List<Map<String, String>> message = null;
        mAdapter = new LocationAdapter(this, locationsList);

        //mAdapter = new LocationAdapter(this, locationsList);
        listView.setAdapter(mAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/locations");
        mDatabase.keepSynced(true);

        locationText = (EditText) findViewById(R.id.location);
        locationText.setSelected(false);
        saveButton = (Button) findViewById(R.id.save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location loc = new Location(locationText.getText().toString());
                mDatabase.push().setValue(loc);
                locationText.setText(null);
            }
        });

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Location temp = dataSnapshot.getValue(Location.class);
                temp.setGuid(dataSnapshot.getKey());
                locationsList.add(temp);
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
                modifyData((Location) parent.getItemAtPosition(position), position);
                return false;
            }
        });

    }

    private void modifyData(final Location locationData, final int position) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.modifylocation, null);

        final EditText et = (EditText) v.findViewById(R.id.editTextLocation);
        et.setText(locationData.getLocationName());

        new AlertDialog.Builder(this)
                .setTitle("Update")
                .setView(v)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                mDatabase.child(locationData.getGuid()).setValue(et.getText().toString());
                                locationsList.set(position, new Location(locationData.getGuid(), et.getText().toString()));
                            }
                        }
                )
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mDatabase.child(locationData.getGuid()).removeValue();
                        locationsList.remove(position);
                    }
                }).show();
    }

}



