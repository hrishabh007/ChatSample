package com.app.realtime.chatsample.xmpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.app.realtime.chatsample.ChatApplication;
import com.app.realtime.chatsample.util.Prefs;

public class NetworkStateReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.d("app", "Network connectivity change");
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                if (!XMPPService.isServiceRunning){
                    Intent in = new Intent(context, XMPPService.class);
                    ChatApplication mChatApp = ChatApplication.getInstance();
                    mChatApp.UnbindService();
                    mChatApp.BindService(in);
                    XMPPHandler xmppHandler=ChatApplication.getmService().xmpp;
                    xmppHandler.setUserPassword(Prefs.getString("username",""),Prefs.getString("password",""));
                    xmppHandler.login();
                }

               /* if (ChatApplication.getmService().xmpp != null){

                }*/
                Log.i("app", "Network " + ni.getTypeName() + " connected");
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {


                Log.d("app", "There's no network connectivity");
            }
        }
    }
}
