package com.appdev.jayes.claymodelsell;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Deliver extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private DatabaseReference refLocation;
    private DatabaseReference refModelName;

    EditText receiptNo;
    EditText mobileno;
    EditText name;

    private ThreeViewAdapter mAdapter;
    ListView lv;
    ArrayList<SellModel> salesArray = new ArrayList<>();
    ArrayList<ClayModel> tempArray = new ArrayList<>();
    ArrayList<ClayModel> mtempArray = new ArrayList<>();

    ArrayList<ClayModel> modelList = new ArrayList<>();
    List<String> locationList = new ArrayList<>();
    List<String> modelNameList = new ArrayList<>();

    int transactionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/sales");

        refModelName = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/claymodels");
        refLocation = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/locations");

        receiptNo = (EditText) findViewById(R.id.receiptNo);
        mobileno = (EditText) findViewById(R.id.mobileno);
        name = (EditText) findViewById(R.id.name);

        lv = (ListView) findViewById(R.id.lv_search);
        mAdapter = new ThreeViewAdapter(this, tempArray);
        lv.setAdapter(mAdapter);


        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(salesArray.get(position).getName());
                transactionID = position;
                showInputDialog(salesArray.get(position));
                return false;
            }
        });

        { // load the datat into array list
            locationList.add("Select location");
            modelNameList.add("Select Clay model");
            refLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        locationList.add(child.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            refModelName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot dts : dataSnapshot.getChildren()) {
                        ClayModel mdl = dts.getValue(ClayModel.class);
                        mdl.setKey(dts.getKey());
                        modelNameList.add(mdl.getModelName());
                        modelList.add(mdl);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });


        }
    }


    public void buttonFind(View view) {
        mtempArray.clear();
        salesArray.clear();
        Query q = null;
        if (receiptNo.hasFocus()) {
            if (receiptNo.getText().toString().length() != 0) {
                String no = String.format(Locale.US, "%04d", UHelper.parseInt(receiptNo.getText().toString()));
                q = mDatabase.child("2018").orderByChild("receiptNo").equalTo(no);
            }
        } else if (mobileno.hasFocus()) {
            if (mobileno.getText().toString().length() != 0)
                q = mDatabase.child("2018").orderByChild("mobile").startAt(mobileno.getText().toString()).endAt(mobileno.getText().toString() + "\uf8ff");
        } else if (name.hasFocus()) {
            if (name.getText().toString().length() != 0)
                q = mDatabase.child("2018").orderByChild("name").startAt(name.getText().toString().toLowerCase()).endAt(name.getText().toString().toLowerCase() + "\uf8ff");
        }
        final Query finalQ = q;
        new Thread(new Runnable() { //this is to avoid error which uptes list view in background instead of main thread
            @Override
            public void run() {
                if (finalQ != null) {
                    finalQ.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            System.out.println("Inside onDataChanged");
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    System.out.println("Query : " + data.getValue() + data.getKey());
                                    SellModel temp = data.getValue(SellModel.class);
                                    temp.setKey(data.getKey());
                                    salesArray.add(temp);
                                    ClayModel mTemp = new ClayModel(temp.getReceiptNo(), temp.getName(), temp.getMobile());
                                    mtempArray.add(mTemp);
                                }
                            }
                            System.out.println(mtempArray.size());
                            refreshList();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("Database query error" + databaseError);
                        }
                    });
                }
                // Make updates the "data" list.
                // Update your adapter.
            }
        }).start();
    }

    void refreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                mAdapter.addAll(mtempArray);
                mAdapter.notifyDataSetChanged();
                lv.invalidateViews();
            }
        });
    }

    void showInputDialog(SellModel temp) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View promptView = layoutInflater.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final TextView receiptNo = (TextView) promptView.findViewById(R.id.receiptNo);
        final EditText name = (EditText) promptView.findViewById(R.id.name);
        final EditText mobile = (EditText) promptView.findViewById(R.id.mobile);
        final EditText city = (EditText) promptView.findViewById(R.id.city);
        final EditText price = (EditText) promptView.findViewById(R.id.price);
        final EditText comments = (EditText) promptView.findViewById(R.id.comments);
        final EditText advance = (EditText) promptView.findViewById(R.id.advance);
        final TextView balance = (TextView) promptView.findViewById(R.id.balance);

        receiptNo.setText(temp.getReceiptNo());
        name.setText(temp.getName());
        mobile.setText(temp.getMobile());
        city.setText(temp.getCity());
        price.setText(temp.getPrice());
        comments.setText(temp.getComments());
        advance.setText(temp.getAdvance());
        balance.setText(temp.getBalance());

        final Spinner modelNameSpinner = (Spinner) promptView.findViewById(R.id.modelNameSpinner);
        final Spinner locationSpinner = (Spinner) promptView.findViewById(R.id.locationSpinner);

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, locationList);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, modelNameList);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelNameSpinner.setAdapter(modelAdapter);

        for (int i = 0; i < modelList.size(); i++) {
            if (temp.getModelName().equals(modelList.get(i).getKey())) {
                modelNameSpinner.setSelection(i + 1);
                break;
            }
        }
        for (int i = 0; i < locationList.size(); i++) {
            if (temp.getLocation().equals(locationList.get(i))) {
                locationSpinner.setSelection(i + 1);
                break;
            }
        }

        modelNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String temp = parent.getItemAtPosition(position).toString();
                if (position == 0) {
                    price.setText(null);
                    balance.setText(null);

                } else {
                    price.setText(modelList.get(position - 1).getModelPrice());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double bal = UHelper.parseDouble(s.toString()) - UHelper.parseDouble(advance.getText().toString());
                balance.setText(bal.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        advance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                Double bal = UHelper.parseDouble(price.getText().toString()) - UHelper.parseDouble(s.toString());
                balance.setText(bal.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double bal = UHelper.parseDouble(s.toString()) - UHelper.parseDouble(advance.getText().toString());
                balance.setText(bal.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        advance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                Double bal = UHelper.parseDouble(price.getText().toString()) - UHelper.parseDouble(s.toString());
                balance.setText(bal.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        alertDialogBuilder
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //deleteRow(temp.getSlno());
                        DatabaseReference refSale = FirebaseDatabase.getInstance().getReference(
                                "users/" +
                                        user.getUid() +
                                        "/sales/" +
                                        UHelper.getTime("y") + "/");

                        refSale.child(salesArray.get(transactionID).getKey()).setValue(null, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(Deliver.this, "Transaction deleted !", Toast.LENGTH_LONG).show();
                                mAdapter.clear();
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if ((name.getText().toString().length() == 0 || mobile.getText().toString().length() == 0 || price.getText().toString().length() == 0)) {
                            Toast.makeText(Deliver.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                        } else {
                            //save to database
                            DatabaseReference refSale = FirebaseDatabase.getInstance().getReference(
                                    "users/" +
                                            user.getUid() +
                                            "/sales/" +
                                            UHelper.getTime("y") + "/");

                            refSale.child(salesArray.get(transactionID).getKey()).setValue(getSalesTransaction(), new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Toast.makeText(Deliver.this, "Update successfull", Toast.LENGTH_SHORT).show();
                                    mAdapter.clear();
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }


                    }

                    private SellModel getSalesTransaction() {
                        String settled = "false";
                        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        if (UHelper.parseDouble(balance.getText().toString()) == 0)
                            settled = "true";

                        return new SellModel(salesArray.get(transactionID).getReceiptNo(),
                                datetime.format(new Date().getTime()),
                                name.getText().toString().toLowerCase(),
                                mobile.getText().toString(),
                                city.getText().toString(),
                                comments.getText().toString(),
                                price.getText().toString(),
                                advance.getText().toString(),
                                balance.getText().toString(),
                                modelList.get(modelNameSpinner.getSelectedItemPosition() - 1).getKey(),
                                locationSpinner.getSelectedItem().toString(),
                                settled);
                    }
                })
                .setNegativeButton("Print",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


}
