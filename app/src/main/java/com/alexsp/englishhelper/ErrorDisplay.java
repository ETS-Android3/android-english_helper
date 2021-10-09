package com.alexsp.englishhelper;

import android.app.Application;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

public class ErrorDisplay
{
    static View view = null;

    static void stopProgressBar()
    {
        // from https://stackoverflow.com/questions/31225142/async-error-current-thread-must-have-a-looper-when-clicking-retry-button
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run() {
                MainActivity.stopProgressBar();
            }
        });
    }

    public static void SetView(View view)
    {
        ErrorDisplay.view = view;
    }

    public static void Show(String errorMessage)
    {
        if (view != null)
            Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show();
        else
            ShowDialog(errorMessage);
        stopProgressBar();
    }

    public static void Show(Exception e)
    {
        String msg = e.getMessage();
        if (msg == null || msg.isEmpty())
            msg = e.toString();
        if (view != null)
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        else
            ShowDialog(msg);
        stopProgressBar();
    }

    public static void ShowDialog(String msg)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.getContext()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static void Fatal(String msg)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.me).create();
        alertDialog.setTitle("Fatal error");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        System.exit(1);
                    }
                });
        alertDialog.show();
    }

}
