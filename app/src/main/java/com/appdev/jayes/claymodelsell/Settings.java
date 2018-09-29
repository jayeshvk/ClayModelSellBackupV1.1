package com.appdev.jayes.claymodelsell;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cie.btp.CieBluetoothPrinter;
import com.cie.btp.DebugLog;
import com.cie.btp.PrintDensity;
import com.cie.btp.PrinterWidth;

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

public class Settings extends AppCompatActivity {

    private TextView tvStatus;

    private static final int BARCODE_WIDTH = 384;
    private static final int BARCODE_HEIGHT = 100;

    public static CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    ProgressDialog pdWorkInProgress;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int LEFT = 1;
    private static final int RIGHT = -1;
    private static final int CENTER = 0;
    String tempFrom, tempTo, tempDiff, tempIdentifier;
    boolean checked, disconnect;
    int tempRepeat, miliSeconds;
    String SHAREDPREFNAME = "ClayModelSell";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //printer relevant
        pdWorkInProgress = new ProgressDialog(this);
        pdWorkInProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    }

    public void buttonLocations(View view) {
        startActivity(new Intent(this, LocationsActivity.class));
    }

    public void buttonBatteryStat(View view) {
        toast("Battery Percentage : " + mPrinter.getBatteryStatus());
    }

    public void buttonReport(View view) {
        startActivity(new Intent(this, DayReport.class));
    }

    public void buttonClayModels(View view) {
        startActivity(new Intent(this, ClayModelsActivity.class));
    }

    public void printEmptyReceipt(View view) {

        LayoutInflater layoutInflater = getLayoutInflater();
        View promptView = layoutInflater.inflate(R.layout.print_empty_receipt_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle("Print Empty Receipt");

        final EditText from = (EditText) promptView.findViewById(R.id.from);
        final EditText to = (EditText) promptView.findViewById(R.id.to);
        final EditText identifier = (EditText) promptView.findViewById(R.id.identifier);
        final EditText pausetime = (EditText) promptView.findViewById(R.id.pauseTime);
        final CheckBox checkBox = (CheckBox) promptView.findViewById(R.id.checkBox);
        final CheckBox printMultiple = (CheckBox) promptView.findViewById(R.id.printMultiple);

        alertDialogBuilder
                .setPositiveButton("Print", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tempFrom = from.getText().toString();
                        tempTo = to.getText().toString();
                        tempIdentifier = identifier.getText().toString();
                        checked = checkBox.isChecked();
                        int pt = UHelper.parseInt(pausetime.getText().toString());
                        if (pt > 5) {
                            miliSeconds = 5000;
                        } else
                            miliSeconds = pt * 1000;
                        if (printMultiple.isChecked())
                            tempRepeat = 2;
                        else tempRepeat = 1;

                        int f = UHelper.parseInt(tempFrom);
                        int t = UHelper.parseInt(tempTo);
                        int b = t - f;
                        if (b < 0)
                            toast("Please enter correct data");
                        else
                            connectPrinter();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
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
        pdWorkInProgress.setIndeterminate(true);
        pdWorkInProgress.setMessage("Connecting to printer ...");
        pdWorkInProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        pdWorkInProgress.show();
        try {
            mPrinter.initService(Settings.this);
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
                    //checkFinish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

/*    private void checkFinish() {
        toast("Issue connecting to printer, please check the printer!");
    }*/

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
                    //checkFinish();
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
                    new AsyncPrint().execute();
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    //toast(n);
                    pdWorkInProgress.cancel();
                    //checkFinish();
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    //toast(m);
                    break;
                case RECEIPT_PRINTER_NOT_CONNECTED:
                    toast("Status : Printer Not Connected");
                    pdWorkInProgress.cancel();
                    //checkFinish();
                    break;
                case RECEIPT_PRINTER_NOT_FOUND:
                    toast("Status : Printer Not Found");
                    pdWorkInProgress.cancel();
                    //checkFinish();
                    break;
                case RECEIPT_PRINTER_SAVED:
                    toast(R.string.printer_saved);
                    break;
            }
        }
    };

    private void print(String receiptNo) {
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
        textPaint.setTextSize(125);
        textPaint.setTypeface(custom_font);
        mPrinter.printUnicodeText(receiptNo, Layout.Alignment.ALIGN_CENTER, textPaint);

        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);
        //mPrintUnicodeText("ನಮ್ಮಲ್ಲಿ ಸುಂದರವಾದ ಶಿರಸಿಯ ಗಣಪತಿ ಮೂರ್ತಿಗಳು ಸಿಗುತ್ತವೆ", 22, CENTER);
        mPrintUnicodeText(readSharedPref("text1"), 22, CENTER);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        mPrintUnicodeText("Dt:____________", textSize, RIGHT);
        mPrintUnicodeText("Name :", textSize, LEFT);
        mPrinter.printLineFeed();
        mPrintUnicodeText("Mob  :", textSize, LEFT);
        mPrinter.printLineFeed();
        mPrintUnicodeText("Model: ", textSize, LEFT);
        mPrinter.printLineFeed();
        mPrintUnicodeText("City :", textSize, LEFT);
        //mPrintUnicodeText("Price:₹", textSize, LEFT);
        //mPrintUnicodeText("Advnc:₹", textSize, LEFT);
        mPrintUnicodeText("Bal  :₹", textSize, LEFT);
        mPrinter.printLineFeed();
        //mPrintUnicodeText("Text :", textSize, LEFT);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 22, CENTER);
        //mPrintUnicodeText("ವಿಶೇಷ ಸೂಚನೆ : ಗಣಪತಿ ಮೂರ್ತಿಯನ್ನು ಚವತಿಯ ದಿವಸ ಮಧ್ಯಾನ್ಹ 12 ಘಂಟೆಯ ಒಳಗೆ ವಯ್ಯಬೇಕು. ಬರುವಾಗ ಇ ಚೀಟಿಯನ್ನುತಪ್ಪದೆ ತರಬೇಕು.", 22, CENTER);
        mPrintUnicodeText(readSharedPref("text2"), 22, CENTER);
        mPrinter.printTextLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        //mPrintUnicodeText("ತಯಾರಕರು : ಸಿ. ವಿ. ಚಿತ್ರಗಾರ, ಮರಾಠಿಕೊಪ್ಪ, ಶಿರಸಿ.", 22, CENTER);
        mPrintUnicodeText(readSharedPref("text3"), 22, CENTER);
        //mPrintUnicodeText("9448629160/9916278538/9141646176", 20, CENTER);
        mPrintUnicodeText(readSharedPref("text4"), 20, CENTER);
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
        System.out.println("Print status **" + mPrinter.printUnicodeText(text, alignment, textPaint));
        System.out.println("Print status Stat**" + mPrinter.getPrinterStatus());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPrinter.onActivityRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void toast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgressBar() {

        runOnUiThread(new Runnable() {
            public void run() {
                pdWorkInProgress.setIndeterminate(true);
                pdWorkInProgress.setMessage("Printing ...");
                pdWorkInProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                pdWorkInProgress.show();
            }
        });
    }

    private void hideProgressBar() {

        runOnUiThread(new Runnable() {
            public void run() {
                pdWorkInProgress.cancel();
            }
        });
    }

    public void buttonOfflinereceipt(View view) {
        //startActivity(new Intent(this, offlineSellActivity.class));
    }

    public void buttonHeaderFooter(View view) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View promptView = layoutInflater.inflate(R.layout.headerfootersetting, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle("Header Footer Text");

        final EditText text1 = promptView.findViewById(R.id.text1);
        final EditText text2 = promptView.findViewById(R.id.text2);
        final EditText text3 = promptView.findViewById(R.id.text3);
        final EditText text4 = promptView.findViewById(R.id.text4);
        text1.setText(readSharedPref("text1"));
        text2.setText(readSharedPref("text2"));
        text3.setText(readSharedPref("text3"));
        text4.setText(readSharedPref("text4"));

        alertDialogBuilder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        writetoSharedPref("text1", text1.getText().toString());
                        writetoSharedPref("text2", text2.getText().toString());
                        writetoSharedPref("text3", text3.getText().toString());
                        writetoSharedPref("text4", text4.getText().toString());
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private class AsyncPrint extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            int f = UHelper.parseInt(tempFrom);
            int t = UHelper.parseInt(tempTo);
            int b = t - f;

            if (t == 0)
                b = 1;
            else if (b == 0)
                b = 1;

            for (int i = 0; i < b; i++) {
                String receiptno = tempIdentifier + UHelper.intLeadingZero(3, f + i);
                for (int j = 0; j < tempRepeat; j++) {
                    print(receiptno);
                    if (checked) {
                        try {
                            Thread.currentThread();
                            Thread.sleep(miliSeconds);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                if (checked) {
                    try {
                        Thread.currentThread();
                        Thread.sleep(miliSeconds);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
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

    private void writetoSharedPref(String key, String text) {
        SharedPreferences preferences = getSharedPreferences(SHAREDPREFNAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, text);
        editor.apply();
    }

    private String readSharedPref(String KEY) {
        String returnData = null;
        SharedPreferences settings = getSharedPreferences(SHAREDPREFNAME, 0);
        switch (KEY) {
            case "text1":
                returnData = settings.getString(KEY, "ನಮ್ಮಲ್ಲಿ ಸುಂದರವಾದ ಶಿರಸಿಯ ಗಣಪತಿ ಮೂರ್ತಿಗಳು ಸಿಗುತ್ತವೆ");
                return returnData;
            case "text2":
                returnData = settings.getString(KEY, "ವಿಶೇಷ ಸೂಚನೆ : ಗಣಪತಿ ಮೂರ್ತಿಯನ್ನು ಚವತಿಯ ದಿವಸ ಮಧ್ಯಾನ್ಹ 12 ಘಂಟೆಯ ಒಳಗೆ ವಯ್ಯಬೇಕು. ಬರುವಾಗ ಇ ಚೀಟಿಯನ್ನುತಪ್ಪದೆ ತರಬೇಕು.");
                return returnData;
            case "text3":
                returnData = settings.getString(KEY, "ತಯಾರಕರು : ಸಿ. ವಿ. ಚಿತ್ರಗಾರ, ಮರಾಠಿಕೊಪ್ಪ, ಶಿರಸಿ.");
                return returnData;
            case "text4":
                returnData = settings.getString(KEY, "9448629160/9916278538/9141646176");
                return returnData;
            default:
                returnData = settings.getString(KEY, null);
                return returnData;
        }
    }

}
