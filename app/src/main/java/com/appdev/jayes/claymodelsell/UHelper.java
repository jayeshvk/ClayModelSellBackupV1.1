package com.appdev.jayes.claymodelsell;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// File Name: Singleton.java
public class UHelper {

    private static UHelper UHelper = new UHelper();

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private UHelper() {
    }

    /* Static 'instance' method */
    public static UHelper getInstance() {
        return UHelper;
    }

    /* Other methods protected by singleton-ness */
    protected static void demoMethod() {
    }

    public static String dateFormatdmyTOymd(String date) {
        if (date.length() != 0) {
            SimpleDateFormat from = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String dateString;
            try {
                dateString = to.format(from.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
                dateString = "";
            }
            return dateString;
        }
        return "";
    }

    public static String dateFormatdmyTOymdhms(String date) {
        if (date.length() != 0) {
            SimpleDateFormat from = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd 12:00:00", Locale.ENGLISH);
            String dateString;
            try {
                dateString = to.format(from.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
                dateString = "";
            }
            return dateString;
        }
        return "";
    }

    public static String dateFormatymdhmsTOdmy(String date) {
        SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        SimpleDateFormat to = new SimpleDateFormat("dd-MM-yy", Locale.ENGLISH);
        String dateString;
        try {
            dateString = to.format(from.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            dateString = "";
        }
        return dateString;
    }

    public static String dateFormatymdTOdmy(String date) {
        SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat to = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String dateString;
        try {
            dateString = to.format(from.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            dateString = "";
        }
        return dateString;
    }

    public static String dateFormatymdhmsTOddmyyyy(String date) {
        SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        SimpleDateFormat to = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        String dateString;
        try {
            dateString = to.format(from.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            dateString = "";
        }
        return dateString;
    }

    public static String setPresentDateyyyyMMdd() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateyyyyMMddCP() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateyyyyMMddhhmmss() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateyyyyMMddhhmm() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mma", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateddMMyyyy() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateDDMMYYhhmm() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy-hhmm", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String setPresentDateDDMMYYhhmmss() {
        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy-hhmmss", Locale.ENGLISH);
        return df.format(c.getTime());
    }

    public static String getEmijoByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    public static double parseDouble(EditText value) {
        double d = 0;
        String str = value.getText().toString();
        if (str != null) {
            try {
                d = Double.parseDouble(str);
            } catch (NumberFormatException e) {
                d = 0;
            }
        }
        return Double.parseDouble(String.format("%.2f", d));
    }

    public static double parseDouble(TextView value) {
        double d = 0;
        String str = value.getText().toString();
        if (str != null) {
            try {
                d = Double.parseDouble(str);
            } catch (NumberFormatException e) {
                d = 0;
            }
        }
        return Double.parseDouble(String.format("%.2f", d));
    }

    public static String stringDouble(String v) {

        return String.format(Locale.ENGLISH, "%.2f", parseDouble(v));

    }

    public static double parseDouble(String value) {
        double d;
        if (value == null)
            return 0;
        else
            try {
                d = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                d = 0;
            }
        return Double.parseDouble(String.format("%.2f", d));
    }

    public static int parseInt(String text) {
        int i = 0;
        try {
            i = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            i = 0;
            e.printStackTrace();
        }
        return i;
    }

    public static void showAlert(Activity act, String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle("Confirmation");
        alert.setMessage(msg);
        alert.setPositiveButton("OK", listener);
        alert.setNegativeButton("Cancel", listener);
        alert.show();
    }

    //added on 17-06-2018
    public static String getTime(String time) {
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

    //for use with Emoji 24/06/2018 jayesh
    public String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    //for adding leading x number of zeros
    public static String intLeadingZero(int numberofZero, int number) {
        String digits = "%0" + numberofZero + "d";
        return String.format(digits, number);
    }
}
