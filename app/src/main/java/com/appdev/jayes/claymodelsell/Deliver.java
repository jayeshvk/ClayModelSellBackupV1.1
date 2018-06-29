package com.appdev.jayes.claymodelsell;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cie.btp.CieBluetoothPrinter;
import com.cie.btp.DebugLog;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    ArrayList<Location> locationList = new ArrayList<>();
    List<String> tempLocationList = new ArrayList<>();
    List<String> tempModelNameList = new ArrayList<>();

    int transactionID;

    public static CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    ProgressDialog pdWorkInProgress;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String tempPrice, tempAdvance, tempBalance, tempModelName;
    private static final int LEFT = 1;
    private static final int RIGHT = -1;
    private static final int CENTER = 0;
    private boolean continueWithoutPrint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver);

        //printer relevant
        pdWorkInProgress = new ProgressDialog(this);
        pdWorkInProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

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

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(salesArray.get(position).getName());
                transactionID = position;
                showInputDialog(salesArray.get(transactionID));
            }
        });

        { // load the datat into array list
            tempLocationList.add("Location");
            tempModelNameList.add("Model");
            refLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Location loc = child.getValue(Location.class);
                        loc.setGuid(child.getKey());
                        tempLocationList.add(loc.getLocationName());
                        locationList.add(loc);
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
                        tempModelNameList.add(mdl.getModelName());
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

    void showInputDialog(final SellModel temp) {
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
        final TextView date = (TextView) promptView.findViewById(R.id.date);

        receiptNo.setText(temp.getReceiptNo());
        name.setText(temp.getName());
        mobile.setText(temp.getMobile());
        city.setText(temp.getCity());
        price.setText(temp.getPrice());
        comments.setText(temp.getComments());
        advance.setText(temp.getAdvance());
        balance.setText(temp.getBalance());
        date.setText(UHelper.dateFormatymdhmsTOddmyyyy(temp.getDate()));

        final Spinner modelNameSpinner = (Spinner) promptView.findViewById(R.id.modelNameSpinner);
        final Spinner locationSpinner = (Spinner) promptView.findViewById(R.id.locationSpinner);

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tempLocationList);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tempModelNameList);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelNameSpinner.setAdapter(modelAdapter);

        for (int i = 0; i < modelList.size(); i++) {
            if (temp.getModelName().equals(modelList.get(i).getKey())) {
                modelNameSpinner.setSelection(i + 1);
                break;
            }
        }
        for (int i = 0; i < locationList.size(); i++) {
            if (temp.getLocation().equals(locationList.get(i).getGuid())) {
                locationSpinner.setSelection(i + 1);
                break;
            }
        }

        modelNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String temp = parent.getItemAtPosition(position).toString();
                if (position == 0) {

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
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
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
                        if (UHelper.parseDouble(balance.getText().toString()) == 0)
                            settled = "true";
                        String modelname = "";
                        String locationname = "";
                        if (modelNameSpinner.getSelectedItemPosition() > 0)
                            modelname = modelList.get(modelNameSpinner.getSelectedItemPosition() - 1).getKey();
                        if (locationSpinner.getSelectedItemPosition() > 0)
                            locationname = locationList.get(locationSpinner.getSelectedItemPosition() - 1).getGuid();

                        return new SellModel(salesArray.get(transactionID).getReceiptNo(),
                                temp.getDate(),
                                name.getText().toString().toLowerCase(),
                                mobile.getText().toString(),
                                city.getText().toString(),
                                comments.getText().toString(),
                                price.getText().toString(),
                                advance.getText().toString(),
                                balance.getText().toString(),
                                modelname,
                                locationname,
                                settled);
                    }
                })
                .setNegativeButton("Print",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                tempPrice = price.getText().toString();
                                tempAdvance = advance.getText().toString();
                                tempBalance = balance.getText().toString();
                                tempModelName = modelNameSpinner.getSelectedItem().toString();
                                connectPrinter();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void connectPrinter() {

        if (!isBluetoothEnabled()) {
            toast("Bluetooth is not switched on");

        } else {
            connect();
        }
    }

    void connect() {
        {
            if (!continueWithoutPrint) {
                pdWorkInProgress.setIndeterminate(true);
                pdWorkInProgress.setMessage("Connecting to printer ...");
                pdWorkInProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                pdWorkInProgress.show();
                try {
                    mPrinter.initService(Deliver.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mPrinter.connectToPrinter("D8:80:39:F8:37:A5");
            }
        }
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
/*        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Printing Issue");
        builder.setMessage("Continue without printer?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                continueWithoutPrint = true;
                dialog.dismiss();
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();*/
        toast("Issue connecting to printer, please check the printer!");
    }

    private Bitmap combineImageIntoOneFlexWidth(ArrayList<Bitmap> bitmap) {
        int w = 0, h = 0;
        for (int i = 0; i < bitmap.size(); i++) {
            if (i < bitmap.size() - 1) {
                h = bitmap.get(i).getHeight() > bitmap.get(i + 1).getHeight() ? bitmap.get(i).getHeight() : bitmap.get(i + 1).getHeight();
            }
            w += bitmap.get(i).getWidth();
        }

        Bitmap temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        int top = 0;
        for (int i = 0; i < bitmap.size(); i++) {
            Log.e("HTML", "Combine: " + i + "/" + bitmap.size() + 1);

            top = (i == 0 ? 0 : top + bitmap.get(i).getWidth());
            //attributes 1:bitmap,2:width that starts drawing,3:height that starts drawing
            canvas.drawBitmap(bitmap.get(i), top, 0f, null);
        }
        return temp;
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

    void toast(int message) {
        Toast.makeText(Deliver.this, message, Toast.LENGTH_LONG).show();
    }

    void toast(String message) {
        Toast.makeText(Deliver.this, message, Toast.LENGTH_LONG).show();
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
                    toast(R.string.ready_for_conn);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTING:
                    toast(R.string.printer_connecting);
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTED:
                    toast(R.string.printer_connected);
                    pdWorkInProgress.cancel();
                    printReceipt();
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    toast(n);
                    pdWorkInProgress.cancel();
                    checkFinish();
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    toast(m);
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

    private void printReceipt() {
        mPrinter.setPrinterWidth(PrinterWidth.PRINT_WIDTH_48MM);
        mPrinter.setAlignmentCenter();
        int textSize = 25;

        SellModel salesData = salesArray.get(transactionID);

        //Print logo
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        mPrinter.printGrayScaleImage(logo, 1);

        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);

        //Print receipt number
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/ErasBoldITC.ttf");
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(125);
        textPaint.setTypeface(custom_font);
        mPrinter.printUnicodeText(salesData.getReceiptNo(), Layout.Alignment.ALIGN_CENTER, textPaint);

        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);
        mPrintUnicodeText("ನಮ್ಮಲ್ಲಿ ಸುಂದರವಾದ ಶಿರಸಿಯ ಗಣಪತಿ ಮೂರ್ತಿಗಳು ಸಿಗುತ್ತವೆ", 22, CENTER);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mPrintUnicodeText("Dt:" + UHelper.dateFormatymdhmsTOddmyyyy(salesData.getDate()), textSize, RIGHT);
        mPrintUnicodeText("Name  :" + salesData.getName(), textSize, LEFT);
        mPrintUnicodeText("Mob   :" + salesData.getMobile(), textSize, LEFT);
        if (!tempModelName.contains("Model"))
            mPrintUnicodeText("Model : " + tempModelName, 32, LEFT);
        mPrintUnicodeText("City  :" + salesData.getCity(), textSize, LEFT);
        mPrintUnicodeText("Price :₹" + tempPrice, textSize, LEFT);
        mPrintUnicodeText("Advnc :₹" + tempAdvance, textSize, LEFT);
        mPrintUnicodeText("Bal :₹" + tempBalance, 34, LEFT);
        mPrintUnicodeText("Text  :" + salesData.getComments(), textSize, LEFT);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);
        mPrintUnicodeText("ವಿಶೇಷ ಸೂಚನೆ : ಗಣಪತಿ ಮೂರ್ತಿಯನ್ನು ಚವತಿಯ ದಿವಸ ಮಧ್ಯಾನ್ಹ 12 ಘಂಟೆಯ ಒಳಗೆ ವಯ್ಯಬೇಕು. ಬರುವಾಗ ಇ ಚೀಟಿಯನ್ನುತಪ್ಪದೆ ತರಬೇಕು.", 22, CENTER);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mPrintUnicodeText("ತಯಾರಕರು : ಸಿ. ವಿ. ಚಿತ್ರಗಾರ, ಮರಾಠಿಕೊಪ್ಪ, ಶಿರಸಿ.", 22, CENTER);
        mPrintUnicodeText("9448629160/9916278538/9141646176", 20, CENTER);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);

        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.resetPrinter();
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
        mPrinter.printUnicodeText(text, alignment, textPaint);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPrinter.onActivityRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
