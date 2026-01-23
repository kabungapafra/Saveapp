package com.example.save.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NetworkMonitor {

    private static NetworkMonitor instance;
    private final Context context;
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(true);
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        startMonitoring();
    }

    public static synchronized NetworkMonitor getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkMonitor(context);
        }
        return instance;
    }

    private void startMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    isConnected.postValue(true);
                }

                @Override
                public void onLost(Network network) {
                    isConnected.postValue(false);
                }
            };

            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } else {
            // Fallback for older Android versions
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    isConnected.postValue(isNetworkAvailable());
                }
            }, filter);
        }

        // Set initial state
        isConnected.postValue(isNetworkAvailable());
    }

    public LiveData<Boolean> getConnectionStatus() {
        return isConnected;
    }

    public boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null)
                return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    public void stopMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}
