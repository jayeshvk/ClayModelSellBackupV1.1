package com.appdev.jayes.claymodelsell;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;

public class DailyReport extends AppCompatActivity {

    int counter;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    ArrayList<SellModel> salesArray = new ArrayList<>();
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // get a reference for the TableLayout
        final TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/sales");

        final ArrayList<String> dataList = new ArrayList<>();

        Query q = null;
        showProgressBar(true);
        q = mDatabase.child(UHelper.getTime("y")).orderByChild("date").startAt("2018-07-03 00:00:00").endAt("2018-09-03 23:59:59" + "\uf8ff");
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                System.out.println("Inside onDataChanged");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        System.out.println("\nQuery : " + data.getValue() + data.getKey());
                        SellModel temp = data.getValue(SellModel.class);
                        temp.setKey(data.getKey());
                        salesArray.add(temp);
                        System.out.println("*****\n" + temp.getDate());
                        dataList.add(temp.getReceiptNo() + "     R:" + temp.getPrice() + "     B:" + temp.getBalance() + "\n" + temp.getName() + "\n" + temp.getModelName() + "\n" + temp.getMobile());

                        TableRow row = new TableRow(DailyReport.this);

                        LinearLayout l = new LinearLayout(DailyReport.this);
                        l.setOrientation(LinearLayout.HORIZONTAL);

                        TextView date = new TextView(DailyReport.this);
                        date.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3f));

                        TextView receiptNo = new TextView(DailyReport.this);
                        receiptNo.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));

                        TextView name = new TextView(DailyReport.this);
                        name.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4f));

                        TextView model = new TextView(DailyReport.this);
                        model.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 6f));

                        TextView mobile = new TextView(DailyReport.this);
                        mobile.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2.75f));

                        TextView rate = new TextView(DailyReport.this);
                        rate.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));

                        TextView balance = new TextView(DailyReport.this);
                        balance.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));


                        date.setText(temp.getDate().substring(0, 10));
                        receiptNo.setText(temp.getReceiptNo());
                        name.setText(temp.getName());
                        model.setText(temp.getModelName());
                        mobile.setText(temp.getMobile());
                        rate.setText(temp.getPrice());
                        balance.setText(temp.getBalance());

                        l.addView(date);
                        l.addView(receiptNo);
                        l.addView(name);
                        l.addView(model);
                        l.addView(mobile);
                        l.addView(rate);
                        l.addView(balance);

                        row.addView(l);
                        // add the TableRow to the TableLayout
                        table.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        showProgressBar(false);
                    }

                } else {
                    System.out.println("no data");
                    showProgressBar(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgressBar(false);
            }
        });


/*        for (int i = 0; i <= 100; i++) {
            // create a new TableRow
            TableRow row = new TableRow(this);
            // count the counter up by one
            counter++;
            // create a new TextView
            TextView t = new TextView(this);
            TextView u = new TextView(this);
            // set the text to "text xx"
            t.setText("textsADFSDAFSADKLFJHKLSDFJ SDFLK JSDFLKJ SDF " + counter);
            u.setText("text SADFSADF SDALJKF L;SDKF ;SKDF;LKSD;FALK S;LDFK" + counter);
            // add the TextView and the CheckBox to the new TableRow
            row.addView(t);
            row.addView(u);
            // add the TableRow to the TableLayout
            table.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }*/
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
