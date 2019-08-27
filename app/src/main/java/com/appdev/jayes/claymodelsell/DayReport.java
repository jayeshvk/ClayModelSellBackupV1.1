package com.appdev.jayes.claymodelsell;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextPaint;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cie.btp.CieBluetoothPrinter;
import com.cie.btp.DebugLog;
import com.cie.btp.PrintDensity;
import com.cie.btp.PrinterWidth;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

import static com.appdev.jayes.claymodelsell.SellActivity.REQUEST_ENABLE_BT;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_DEVICE_NAME;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTING;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_LISTEN;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_NONE;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MESSAGES;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_FOUND;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_SAVED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_STATUS;

public class DayReport extends AppCompatActivity {

    public static CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    ProgressDialog pdWorkInProgress;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int LEFT = 1;
    private static final int RIGHT = -1;
    private static final int CENTER = 0;
    boolean checked, disconnect;


    private DatabaseReference mDatabase;
    private FirebaseUser user;
    ArrayList<SellModel> salesArray = new ArrayList<>();
    private static final String[] TABLE_HEADERS = {"R.No", "Name", "Rate", "Bal"};
    String[][] SellModels;
    String[][] SellModels1;
    private ProgressDialog pDialog;
    Boolean flag;
    SortableTableView<String[]> tableView;
    Calendar myCalendar = Calendar.getInstance();

    TextView fromDate, toDate;
    Boolean fromDateClicked = false;
    Boolean toDateClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_report);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        fromDate = findViewById(R.id.fromDate);
        toDate = findViewById(R.id.toDate);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/sales");

        final ArrayList<String> dataList = new ArrayList<>();


        tableView = (SortableTableView<String[]>) findViewById(R.id.tableView);
        tableView.setHeaderElevation(10);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(DayReport.this, 4, 100);
        columnModel.setColumnWidth(0, 90);
        columnModel.setColumnWidth(1, 130);
        columnModel.setColumnWidth(2, 100);
        columnModel.setColumnWidth(3, 100);
        tableView.setColumnModel(columnModel);
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(this, TABLE_HEADERS));
        tableView.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                Toast.makeText(DayReport.this, ((String[]) clickedData)[2], Toast.LENGTH_LONG).show();
            }
        });

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(DayReport.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                fromDateClicked = true;
            }
        });
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(DayReport.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                toDateClicked = true;
            }
        });


        //printer relevant
        pdWorkInProgress = new ProgressDialog(this);
        pdWorkInProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    private void populateData() {

        Collections.sort(salesArray, new Comparator<SellModel>() {
            @Override
            public int compare(SellModel o1, SellModel o2) {
                return o1.getReceiptNo().compareTo(o2.getReceiptNo());
            }
        });

        SellModels = new String[salesArray.size()][4];
        System.out.println("\n*********************" + salesArray.size());
        if (salesArray.size() > 0) {
            for (int i = 0; i < salesArray.size(); i++) {

                SellModel s = salesArray.get(i);
                SellModels[i][0] = s.getReceiptNo();
                SellModels[i][1] = s.getName();
                SellModels[i][2] = s.getPrice();
                SellModels[i][3] = s.getBalance();
                System.out.println("\n********" + s);

            }
        }

        SellModels1 = new String[salesArray.size()][4];
        if (salesArray.size() > 0) {
            for (int i = 0; i < salesArray.size(); i++) {
                SellModel s = salesArray.get(i);
                SellModels1[i][0] = s.getReceiptNo();
                SellModels1[i][1] = s.getModelName();
                SellModels1[i][2] = s.getMobile();
                SellModels1[i][3] = s.getDate().substring(5, 10);
            }
        }
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

    public void buttonFind(View view) {
        salesArray.clear();
        SellModels = null;
        SellModels1 = null;
        Query q = null;
        showProgressBar(true);
        if (fromDate.getText().toString().length() > 0 && toDate.getText().toString().length() > 0) {

            q = mDatabase.child(UHelper.getTime("y")).orderByChild("date").startAt(fromDate.getText().toString().trim() + " 00:00:00").endAt(toDate.getText().toString().trim() + " 23:59:59" + "\uf8ff");
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    showProgressBar(false);

                    System.out.println("Inside onDataChanged");
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            System.out.println("\nQuery : " + data.getValue() + data.getKey());
                            SellModel temp = data.getValue(SellModel.class);
                            temp.setKey(data.getKey());
                            salesArray.add(temp);
                        }

                    } else {
                        System.out.println("no data");
                        showProgressBar(false);
                    }
                    populateData();
                    tableView.setDataAdapter(new SimpleTableDataAdapter(DayReport.this, SellModels));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showProgressBar(false);
                }
            });
        }
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        if (fromDateClicked) {
            fromDate.setText(sdf.format(myCalendar.getTime()));
            fromDateClicked = false;
        } else if (toDateClicked) {
            toDate.setText(sdf.format(myCalendar.getTime()));
            toDateClicked = false;
        } else {
            fromDate.setText(null);
            toDate.setText(null);
        }
    }

    public void buttonPrint(View view) {
        connectPrinter();
    }

    private void connectPrinter() {
        if (!isBluetoothEnabled()) {
            toast("Bluetooth is not switched on");

        } else {
            connect();
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    void toast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    void connect() {
        pdWorkInProgress.setIndeterminate(true);
        pdWorkInProgress.setMessage("Connecting to printer ...");
        pdWorkInProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        pdWorkInProgress.show();
        try {
            mPrinter.initService(DayReport.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPrinter.connectToPrinter("D8:80:39:F8:37:A5");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        System.out.println(requestCode + "*" + resultCode + "*" + data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    pdWorkInProgress.cancel();
                    connect();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    pdWorkInProgress.cancel();
                    toast("Bluetooth not switched on");
                    checkFinish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void checkFinish() {
        toast("Issue connecting to printer, please check the printer!");
    }

    private boolean isBluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Switch on Bluetooth to use printer", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    protected void onResume() {
        DebugLog.logTrace();
        mPrinter.onActivityResume();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        DebugLog.logTrace();
        mPrinter.onActivityPause();
        System.out.println("On pause");
        super.onPause();
        //this.unregisterReceiver(this.mReceiver);
    }

    @Override
    protected void onDestroy() {
        DebugLog.logTrace("onDestroy");
        mPrinter.onActivityDestroy();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIPT_PRINTER_MESSAGES);
        LocalBroadcastManager.getInstance(this).registerReceiver(ReceiptPrinterMessageReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        System.out.println("On stop");
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(ReceiptPrinterMessageReceiver);
        } catch (Exception e) {
            DebugLog.logException(e);
        }
    }

    private final BroadcastReceiver ReceiptPrinterMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebugLog.logTrace("Printer Message Received");
            Bundle b = intent.getExtras();
            switch (b.getInt(RECEIPT_PRINTER_STATUS)) {
                case RECEIPT_PRINTER_CONN_STATE_NONE:
                    toast(R.string.printer_not_conn);
                    pdWorkInProgress.cancel();
                    checkFinish();
                    break;
                case RECEIPT_PRINTER_CONN_STATE_LISTEN:
                    //toast(R.string.ready_for_conn);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTING:
                    //toast(R.string.printer_connecting);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTED:
                    toast(R.string.printer_connected);
                    pdWorkInProgress.cancel();
                    new DayReport.AsyncPrint().execute();
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    //toast(n);
                    pdWorkInProgress.cancel();
                    checkFinish();
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    //toast(m);
                    break;
                case RECEIPT_PRINTER_NOT_CONNECTED:
                    toast("Status : Printer Not Connected");
                    pdWorkInProgress.cancel();
                    checkFinish();
                    break;
                case RECEIPT_PRINTER_NOT_FOUND:
                    toast("Status : Printer Not Found");
                    pdWorkInProgress.cancel();
                    checkFinish();
                    break;
                case RECEIPT_PRINTER_SAVED:
                    toast(R.string.printer_saved);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPrinter.onActivityRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void mPrintUnicodeText(String text, int size, int almnt) {
        Layout.Alignment alignment = null;
        switch (almnt) {
            case 0:
                alignment = Layout.Alignment.ALIGN_CENTER;
                break;
            case 1:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
            case -1:
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
        }
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Cousine-Regular.ttf");
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(size);
        textPaint.setTypeface(font);
        System.out.println("Print status **" + mPrinter.printUnicodeText(text, alignment, textPaint));
        System.out.println("Print status Stat**" + mPrinter.getPrinterStatus());
    }

    private class AsyncPrint extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            print();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //wait for printing to complete
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //mPrinter.disconnectFromPrinter();


        }
    }

    private void print() {
        mPrinter.setPrinterWidth(PrinterWidth.PRINT_WIDTH_48MM);
        mPrinter.setAlignmentCenter();
        int textSize = 32;
        int pixellineFeed = 150;
        mPrinter.setPrintDensity(PrintDensity.FADE);
        //Print logo
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        mPrinter.printGrayScaleImage(logo, 1);
        mPrinter.setPrintDensity(PrintDensity.NORMAL);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);

        //Print receipt number
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/ErasBoldITC.ttf");
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);
        textPaint.setTypeface(custom_font);

        mPrinter.printUnicodeText("Report", Layout.Alignment.ALIGN_CENTER, textPaint);
        String previousDate = "";
        for (int i = 0; i < salesArray.size(); i++) {
            if (!salesArray.get(i).getDate().substring(0, 10).contains(previousDate)) {
                mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);
                mPrintUnicodeText(salesArray.get(i).getDate().substring(0, 10), 22, CENTER);
                mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
            }
            previousDate = salesArray.get(i).getDate().substring(0, 10);
            mPrintUnicodeText("R.NO: " + salesArray.get(i).getReceiptNo() + " " + "Rt: " + salesArray.get(i).getPrice() + " " + "Bl: " + salesArray.get(i).getBalance(), 20, CENTER);
            mPrintUnicodeText(salesArray.get(i).getName(), 20, LEFT);
            mPrintUnicodeText(salesArray.get(i).getModelName(), 20, LEFT);
            mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        }

        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.resetPrinter();
    }
}
