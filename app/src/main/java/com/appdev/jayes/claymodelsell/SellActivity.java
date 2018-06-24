package com.appdev.jayes.claymodelsell;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SellActivity extends AppCompatActivity {

    private DatabaseReference refLocation;
    private DatabaseReference refModelName;
    private DatabaseReference refYear;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private boolean increse;

    TextView receiptNo;
    EditText name;
    EditText mobile;
    EditText city;
    EditText price;
    EditText comments;
    EditText advance;
    TextView balance;
    Spinner locationSpinner;
    Spinner modelNameSpinner;

    private ProgressDialog pDialog;

    ArrayList<ClayModel> modelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        receiptNo = (TextView) findViewById(R.id.receiptNo);
        name = (EditText) findViewById(R.id.name);
        mobile = (EditText) findViewById(R.id.mobile);
        city = (EditText) findViewById(R.id.city);
        price = (EditText) findViewById(R.id.price);
        comments = (EditText) findViewById(R.id.comments);
        advance = (EditText) findViewById(R.id.advance);
        balance = (TextView) findViewById(R.id.balance);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        modelList = new ArrayList<>();

        refModelName = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/claymodels");
        refLocation = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/locations");
        refYear = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/sales/2018/receiptno");


        locationSpinner = (Spinner) findViewById(R.id.locationSpinner);
        modelNameSpinner = (Spinner) findViewById(R.id.modelNameSpinner);

        final List<String> locationList = new ArrayList<>();
        locationList.add("Select location");
        final List<String> modelNameList = new ArrayList<>();
        modelNameList.add("Select Clay model");

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, locationList);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, modelNameList);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelNameSpinner.setAdapter(modelAdapter);

        receiptno();
        showProgressBar(true);
        refLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showProgressBar(false);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    locationList.add(child.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        showProgressBar(true);
        refModelName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showProgressBar(false);
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

    }

    public void buttonSave(View view) {

        //Validate and collect all data
        if (name.getText().toString().length() == 0 || mobile.getText().toString().length() == 0 || price.getText().toString().length() == 0)
            Toast.makeText(SellActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
        else {

            //save to database
            DatabaseReference refSale = FirebaseDatabase.getInstance().getReference(
                    "users/" +
                            user.getUid() +
                            "/sales/" +
                            UHelper.getTime("y") + "/");
            refSale.push().setValue(getSalesTransaction(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Toast.makeText(SellActivity.this, "Error saving transaction, please try later", Toast.LENGTH_LONG).show();
                    }
                    if (databaseReference != null) {
                        Toast.makeText(SellActivity.this, "Save successfull", Toast.LENGTH_LONG).show();
                        name.setText(null);
                        name.requestFocus();
                        price.setText(null);
                        mobile.setText(null);
                        city.setText(null);
                        comments.setText(null);
                        balance.setText(null);
                        advance.setText(null);
                        modelNameSpinner.setSelection(0);
                        locationSpinner.setSelection(0);

                    }
                }
            });
            //on successfull save print 2 copies
            increse = true;
            receiptno();
        }
    }

    private SellModel getSalesTransaction() {
        String settled = "false";
        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if (UHelper.parseDouble(balance.getText().toString()) == 0)
            settled = "true";
        String modelname = "";

        if (modelNameSpinner.getSelectedItemPosition() > 0)
            modelList.get(modelNameSpinner.getSelectedItemPosition() - 1).getKey();

        return new SellModel(receiptNo.getText().toString(),
                datetime.format(new Date().getTime()),
                name.getText().toString().toLowerCase(),
                mobile.getText().toString(),
                city.getText().toString(),
                comments.getText().toString(),
                price.getText().toString(),
                advance.getText().toString(),
                balance.getText().toString(),
                // modelNameSpinner.getSelectedItem().toString(),
                modelname,
                locationSpinner.getSelectedItem().toString(),
                settled);
    }

    private void receiptno() {
        final DatabaseReference rcptno = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/sales/" + UHelper.getTime("y") + "/receiptno");
        showProgressBar(true);
        rcptno.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//test data
                showProgressBar(false);
                int currentValue = 0;
                String value;
                try {
                    value = dataSnapshot.getValue().toString();
                } catch (Exception e) {
                    value = null;
                }

                if (value == null) {
                    receiptNo.setText(String.format("%04d", 1));
                } else {
                    currentValue = Integer.parseInt(value);
                    receiptNo.setText(String.format("%04d", currentValue + 1));
                }
                if (increse) {
                    rcptno.setValue(String.format("%04d", currentValue + 1));
                    receiptNo.setText(String.format("%04d", currentValue + 2));
                    increse = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SellActivity.this, "Error with firebase :" + databaseError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showProgressBar(final boolean visibility) {

        runOnUiThread(new Runnable() {
            public void run() {
                if (visibility)
                    showpDialog();
                else hidepDialog();
            }
        });
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
