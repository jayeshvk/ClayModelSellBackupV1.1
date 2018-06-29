package com.appdev.jayes.claymodelsell;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cie.btp.CieBluetoothPrinter;
import com.cie.btp.DebugLog;
import com.cie.btp.PrinterWidth;

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

public class Settings extends AppCompatActivity {

    private TextView tvStatus;

    private static final int BARCODE_WIDTH = 384;
    private static final int BARCODE_HEIGHT = 100;

    public static CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    private int imageAlignment = 1;

    private BroadcastReceiver mReceiver;

    ProgressDialog pdWorkInProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pdWorkInProgress = new ProgressDialog(this);
        pdWorkInProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mAdapter == null) {
            Toast.makeText(this, "No Bluetooth devide found", Toast.LENGTH_SHORT).show();
        }

        try {
            mPrinter.initService(Settings.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void btnConnect(View view) {
        mPrinter.disconnectFromPrinter();
        mPrinter.selectPrinter(Settings.this);
        mPrinter.setPrinterWidth(PrinterWidth.PRINT_WIDTH_48MM);

    }

    public void btnClearPrinter(View view) {
        mPrinter.clearPreferredPrinter();
    }

    public void btnPrint(View view) {

    }

    public void buttonLocations(View view) {
        startActivity(new Intent(this, LocationsActivity.class));
    }

    public void buttonClayModels(View view) {
        startActivity(new Intent(this, ClayModelsActivity.class));
    }


    private void savePrinterMac(String sMacAddr) {
        if (sMacAddr.length() > 4) {
            System.out.println("Mac Address " + sMacAddr);
            tvStatus.setText("Preferred Printer saved");
        } else {
            tvStatus.setText("Preferred Printer cleared");
        }
    }


}
