package com.appdev.jayes.claymodelsell;

import android.icu.util.Calendar;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
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
                    model mdl = dts.getValue(model.class);
                    System.out.println(mdl.getModelName());
                    modelNameList.add(mdl.getModelName() + "-" + mdl.modelPrice);
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
                String[] data = temp.split("-", 2);
                if (data.length > 1)
                    price.setText(data[1]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

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
                Double bal = convertDouble(s.toString()) - convertDouble(advance.getText().toString());
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


                Double bal = convertDouble(price.getText().toString()) - convertDouble(s.toString());
                balance.setText(bal.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private Double convertDouble(String data) {
        Double d = 0.00;
        try {
            d = Double.parseDouble(data);
        } catch (Exception e) {
            d = 0.00;
        }
        return d;
    }

    public void buttonSave(View view) {
        //Collect all data

        //save to database
        DatabaseReference refSale = FirebaseDatabase.getInstance().getReference(
                "users/" +
                        user.getUid() +
                        "/sales/" +
                        getTime("y") + "/" +
                        getTime("m") + "/");
        refSale.push().setValue(getSalesTransaction());
        //on successfull save print 2 copies
        increse = true;
        receiptno();
    }

    private Sell getSalesTransaction() {

        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return new Sell(receiptNo.getText().toString(),
                datetime.format(new Date().getTime()),
                name.getText().toString(),
                mobile.getText().toString(),
                city.getText().toString(),
                comments.getText().toString(),
                price.getText().toString(),
                advance.getText().toString(),
                balance.getText().toString(),
                modelNameSpinner.getSelectedItem().toString(),
                locationSpinner.getSelectedItem().toString(),
                "false");
    }

    private void receiptno() {
        final int[] currentValue = new int[1];
        final DatabaseReference rcptno = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/sales/2018/receiptno");
        rcptno.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    currentValue[0] = dataSnapshot.getValue(Integer.class);
                } catch (Exception e) {
                    currentValue[0] = 0;
                    receiptNo.setText(String.format("%04d", 1));
                }
                receiptNo.setText(String.format("%04d", currentValue[0] + 1));

                System.out.println("This is the new value" + currentValue[0]);
                if (increse) {
                    rcptno.setValue(String.format("%04d", currentValue[0] + 1));
                    receiptNo.setText(String.format("%04d", currentValue[0] + 2));
                    increse = false;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SellActivity.this, "Error with firebase :" + databaseError, Toast.LENGTH_LONG).show();
            }
        });
    }

    /*private void receiptno() {
        refYear.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                receiptNo.setText(currentValue+1+"");
                System.out.println("This is the curent value x" + currentValue);

                if (currentValue == null) {
                    mutableData.setValue(1);
                    receiptNo.setText("1");
                } else {
                    if (increse) {
                        mutableData.setValue(currentValue + 1);
                        receiptNo.setText(currentValue+2+"");
                        increse = false;
                    }
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(
                    DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                System.out.println("Transaction completed");

            }
        });
    }*/

    private String getTime(String time) {
        Date dt = new Date();
        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        SimpleDateFormat month = new SimpleDateFormat("MMM");
        SimpleDateFormat date = new SimpleDateFormat("dd");

        switch (time) {
            case "d":
                return date.format(dt.getTime());
            case "m":
                return month.format(dt.getTime());
            case "y":
                return year.format(dt.getTime());
            case "dt":
                return datetime.format(dt.getTime());
            default:
                datetime.format(dt.getTime());
        }
        return time;
    }

    private static class model {
        String modelName;
        String modelPrice;

        model(String modelName, String modelPrice) {
            this.modelName = modelName;
            this.modelPrice = modelPrice;
        }

        public String getModelName() {
            return modelName;
        }

        public String getModelPrice() {
            return modelPrice;
        }

        model() {
        }
    }

}
