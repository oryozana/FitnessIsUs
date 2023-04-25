package com.example.fitnessisus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class NetworkConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            if(!isOnline(context))
                noInternetAccess(context);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline(Context context){
        try{
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void noInternetAccess(Context context){
        AlertDialog ad;
        AlertDialog.Builder adb;
        adb = new AlertDialog.Builder(context);
        adb.setCancelable(false);

        View customAlertDialog = LayoutInflater.from(context).inflate(R.layout.alert_dialog_no_internet, null);
        Button btRecheckInternetConnectionStatus = customAlertDialog.findViewById(R.id.btRecheckInternetConnectionStatus);

        adb.setView(customAlertDialog);
        ad = adb.create();

        btRecheckInternetConnectionStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline(context))
                    ad.cancel();
                else
                    Toast.makeText(context, "Internet connection unavailable!", Toast.LENGTH_SHORT).show();
            }
        });

        ad.show();
    }
}