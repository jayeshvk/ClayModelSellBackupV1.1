package com.appdev.jayes.claymodelsell;

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
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cie.btp.CieBluetoothPrinter;
import com.cie.btp.DebugLog;
import com.cie.btp.FontStyle;
import com.cie.btp.FontType;
import com.cie.btp.PrinterWidth;
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

import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_DEVICE_NAME;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTING;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_LISTEN;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_NONE;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MESSAGES;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NAME;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_FOUND;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_SAVED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_STATUS;

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
    ArrayList<Location> locationList;

    public static CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    private int imageAlignment = 1;
    private BroadcastReceiver mReceiver;
    ProgressDialog pdWorkInProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);
        //Typeface font = Typeface.createFromAsset(getAssets(), "Nirmala.ttf");

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

        //printer relevant
        pdWorkInProgress = new ProgressDialog(this);
        pdWorkInProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        connectPrinter();

        modelList = new ArrayList<>();
        locationList = new ArrayList<>();

        refModelName = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/claymodels");
        refLocation = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/locations");
        refYear = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/sales/2018/receiptno");


        locationSpinner = (Spinner) findViewById(R.id.locationSpinner);
        modelNameSpinner = (Spinner) findViewById(R.id.modelNameSpinner);

        final List<String> tempLocationList = new ArrayList<>();
        final List<String> tempModelNameList = new ArrayList<>();

        tempLocationList.add("Location");
        tempModelNameList.add("Model");

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tempLocationList);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        ArrayAdapter<String> modelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tempModelNameList);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelNameSpinner.setAdapter(modelAdapter);

        receiptno();
        showProgressBar(true);
        refLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showProgressBar(false);
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
        showProgressBar(true);
        refModelName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showProgressBar(false);
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
/*        if (name.getText().toString().length() == 0 || mobile.getText().toString().length() == 0 || price.getText().toString().length() == 0)
            Toast.makeText(SellActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
        else*/
        {

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
                        Toast.makeText(SellActivity.this, "Error saving, try again!", Toast.LENGTH_LONG).show();
                    }
                    if (databaseReference != null) {
                        Toast.makeText(SellActivity.this, "Saved", Toast.LENGTH_LONG).show();
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
            printReceipt();
            synchronized (this) {
                try {
                    wait(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            printReceipt();
            //generate next receipt number
            receiptno();
        }
    }

    private void printReceipt() {
        ArrayList<Bitmap> bmps = new ArrayList<Bitmap>();

        for (int i = 0; i < receiptNo.getText().toString().length(); i++) {
            char number = receiptNo.getText().toString().charAt(i);
            Bitmap bmp = null;
            switch (number) {
                case '0':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.zero);
                    bmps.add(bmp);
                    break;
                case '1':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.one);
                    bmps.add(bmp);
                    break;
                case '2':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.two);
                    bmps.add(bmp);
                    break;
                case '3':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.three);
                    bmps.add(bmp);
                    break;
                case '4':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.four);
                    bmps.add(bmp);
                    break;
                case '5':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.five);
                    bmps.add(bmp);
                    break;
                case '6':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.six);
                    bmps.add(bmp);
                    break;
                case '7':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.seven);
                    bmps.add(bmp);
                    break;
                case '8':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.eight);
                    bmps.add(bmp);
                    break;
                case '9':
                    bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.nine);
                    bmps.add(bmp);
                    break;
            }
        }
        TextPaint mDefaultTextPaint = new TextPaint();

        Bitmap bmpFinal = combineImageIntoOneFlexWidth(bmps);
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        mPrinter.setPrinterWidth(PrinterWidth.PRINT_WIDTH_48MM);
        mPrinter.setAlignmentCenter();

        //mPrinter.setLineSpacing(0);
        //logo
        mPrinter.printGrayScaleImage(logo, 1);

        //mPrinter.setLineSpacing(0);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        //mPrinter.setLineSpacing(0);

        //receipt no in big letters
        mPrinter.printGrayScaleImage(bmpFinal, 1);

        //mPrinter.setLineSpacing(0);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mDefaultTextPaint.setColor(Color.BLACK);
        mDefaultTextPaint.setTextSize(22);
        mPrinter.printUnicodeText("ನಮ್ಮಲ್ಲಿ ಸುಂದರವಾದ ಶಿರಸಿಯ ಗಣಪತಿ ಮೂರ್ತಿಗಳು ಸಿಗುತ್ತವೆ", Layout.Alignment.ALIGN_CENTER, mDefaultTextPaint);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mPrinter.setAlignmentLeft();
        mPrinter.printTextLine("Dt:" + UHelper.setPresentDateddMMyyyy() + "\n");
        mPrinter.printTextLine("Name    :" + name.getText().toString() + "\n");
        mPrinter.printTextLine("Mob     :" + mobile.getText().toString() + "\n");
        mDefaultTextPaint.setColor(Color.BLACK);
        mDefaultTextPaint.setTextSize(28);
        mPrinter.printUnicodeText("Model   :" + modelNameSpinner.getSelectedItem(), Layout.Alignment.ALIGN_NORMAL, mDefaultTextPaint);
        mPrinter.printTextLine("City    :" + city.getText().toString() + "\n");
        mPrinter.printTextLine("Price   :₹" + price.getText().toString() + "\n");
        mPrinter.printTextLine("Advance :₹" + advance.getText().toString() + "\n");
        mPrinter.setFontStyle(true, false, FontStyle.DOUBLE_WIDTH_HEIGHT, FontType.FONT_A);
        mPrinter.printTextLine("Bal :₹" + balance.getText().toString() + "\n");
        mPrinter.setFontStyle(false, false, FontStyle.NORMAL, FontType.FONT_A);
        mPrinter.printTextLine("Comm    :" + comments.getText().toString() + "\n");
        mPrinter.printTextLine("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mDefaultTextPaint.setColor(Color.BLACK);
        mDefaultTextPaint.setTextSize(20);
        mPrinter.printUnicodeText("ವಿಶೇಷ ಸೂಚನೆ : ಗಣಪತಿ ಮೂರ್ತಿಯನ್ನು ಚವತಿಯ ದಿವಸ ಮಧ್ಯಾನ್ಹ 12 ಘಂಟೆಯ ಒಳಗೆ ವಯ್ಯಬೇಕು. ಬರುವಾಗ ಇ ಚೀಟಿಯನ್ನುತಪ್ಪದೆ ತರಬೇಕು.", Layout.Alignment.ALIGN_CENTER, mDefaultTextPaint);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mDefaultTextPaint.setColor(Color.BLACK);
        mDefaultTextPaint.setTextSize(20);
        mPrinter.printUnicodeText("ತಯಾರಕರು : ಸಿ. ವಿ. ಚಿತ್ರಗಾರ, ಮರಾಠಿಕೊಪ್ಪ, ಶಿರಸಿ.", Layout.Alignment.ALIGN_CENTER, mDefaultTextPaint);
        mPrinter.printTextLine("9448629160/9916278538/9141646176\n");
        mPrinter.printTextLine("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.resetPrinter();

    }

    private SellModel getSalesTransaction() {
        String settled = "false";
        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if (UHelper.parseDouble(balance.getText().toString()) == 0)
            settled = "true";
        String modelname = "";
        String locationname = "";

        if (modelNameSpinner.getSelectedItemPosition() > 0)
            modelname = modelList.get(modelNameSpinner.getSelectedItemPosition() - 1).getKey();

        if (locationSpinner.getSelectedItemPosition() > 0)
            locationname = locationList.get(locationSpinner.getSelectedItemPosition() - 1).getGuid();


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
                locationname,
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        System.out.println(requestCode + "*" + resultCode + "*" + data);
        if (resultCode == 0) {
            pdWorkInProgress.cancel();
            toast("Bluetooth access denied");
            chechFinish();
        } else if (resultCode == -1) {
            pdWorkInProgress.cancel();
            mPrinter.connectToPrinter("D8:80:39:F8:37:A5");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPrinter.onActivityRequestPermissionsResult(requestCode, permissions, grantResults);
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
                    chechFinish();
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
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    toast(n);
                    pdWorkInProgress.cancel();
                    chechFinish();
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    toast(m);
                    break;
                case RECEIPT_PRINTER_NOT_CONNECTED:
                    toast("Status : Printer Not Connected");
                    pdWorkInProgress.cancel();
                    chechFinish();
                    break;
                case RECEIPT_PRINTER_NOT_FOUND:
                    toast("Status : Printer Not Found");
                    pdWorkInProgress.cancel();
                    chechFinish();
                    break;
                case RECEIPT_PRINTER_SAVED:
                    toast(R.string.printer_saved);
                    break;
            }
        }
    };

    private void chechFinish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Printing Issue");
        builder.setMessage("Continue without printer?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
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
        alert.show();
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

    void toast(int message) {
        Toast.makeText(SellActivity.this, message, Toast.LENGTH_LONG).show();
    }

    void toast(String message) {
        Toast.makeText(SellActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void connectPrinter() {
        pdWorkInProgress.setIndeterminate(true);
        pdWorkInProgress.setMessage("Connecting to printer ...");
        pdWorkInProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        pdWorkInProgress.show();
        try {
            mPrinter.initService(SellActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPrinter.connectToPrinter("D8:80:39:F8:37:A5");
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

}
