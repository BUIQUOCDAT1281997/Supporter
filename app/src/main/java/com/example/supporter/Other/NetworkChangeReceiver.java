package com.example.supporter.Other;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.example.supporter.Activity.InternetDisconnectedActivity;


//o day chung ta cos the nhan va xu lis nhieu receive khac nhau
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = getConnectivityStatusString(context);
        if (status.isEmpty()){
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
        else if (status.equals("No internet is available")){
            Intent intent1 = new Intent(context, InternetDisconnectedActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent1);
        }
    }

    private String getConnectivityStatusString(Context context){
        String status = null;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                status = "Wifi enabled";
                return status;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                status = "Mobile data enabled";
                return status;
            }
        } else {
            status = "No internet is available";
            return status;
        }
        return status;
    }
}
