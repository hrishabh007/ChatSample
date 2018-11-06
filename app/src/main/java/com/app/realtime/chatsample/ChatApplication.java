package com.app.realtime.chatsample;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.app.realtime.chatsample.util.Constants;
import com.app.realtime.chatsample.util.PrefKeys;
import com.app.realtime.chatsample.util.Prefs;
import com.app.realtime.chatsample.xmpp.LocalBinder;

import com.app.realtime.chatsample.xmpp.XMPPEventReceiver;
import com.app.realtime.chatsample.xmpp.XMPPHandler;
import com.app.realtime.chatsample.xmpp.XMPPService;

import java.util.List;


public class ChatApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private final String TAG = getClass().getSimpleName();
    private static ChatApplication mInstance = null;

    public static XMPPService xmppService;
    public Boolean mBounded = false;

    //Our broadCast receive to update us on various events
    private XMPPEventReceiver mEventReceiver;

    /*
     * We need a service connection because we are starting XMPP service with `bindService` instead of `startService`.
     * BindService is used whenever your activity needs a back and forth communication with your service, as opposed to
     * Startservice, where you activity and services are not in communication
     *
     * Bindservice communicates with your activity using ServiceConnection, which is why you have this block of code below.
     * ReadMore: https://developer.android.com/guide/components/bound-services.html#Binder
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(final ComponentName name,
                                       final IBinder service) {
            xmppService = ((LocalBinder<XMPPService>) service).getService();
            mBounded = true;
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            xmppService = null;
            mBounded = false;
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        registerActivityLifecycleCallbacks(this);
        new Prefs.Builder().
                setContext(this).
                setMode(Context.MODE_PRIVATE).
                setUseDefaultSharedPreference(true).build();
        if (mEventReceiver == null) mEventReceiver = new XMPPEventReceiver();

        IntentFilter intentFilter = new IntentFilter(Constants.EVT_LOGGED_IN);
        intentFilter.addAction(Constants.EVT_SIGNUP_SUC);
        intentFilter.addAction(Constants.EVT_SIGNUP_ERR);
        intentFilter.addAction(Constants.EVT_NEW_MSG);
        intentFilter.addAction(Constants.EVT_AUTH_SUC);
        intentFilter.addAction(Constants.EVT_RECONN_ERR);
        intentFilter.addAction(Constants.EVT_RECONN_WAIT);
        intentFilter.addAction(Constants.EVT_RECONN_SUC);
        intentFilter.addAction(Constants.EVT_CONN_SUC);
        intentFilter.addAction(Constants.EVT_CONN_CLOSE);
        intentFilter.addAction(Constants.EVT_LOGIN_ERR);
        intentFilter.addAction(Constants.EVT_PRESENCE_CHG);
        intentFilter.addAction(Constants.EVT_CHATSTATE_CHG);
        intentFilter.addAction(Constants.EVT_REQUEST_SUBSCRIBE);

        registerReceiver(mEventReceiver, intentFilter);
    }

    public XMPPEventReceiver getEventReceiver() {
        return mEventReceiver;
    }


    public static synchronized ChatApplication getInstance() {
        return mInstance;
    }

    public void BindService(Intent intent) {
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void UnbindService() {
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    public static boolean isLogin() {
        return Prefs.getBoolean(PrefKeys.PREF_IS_LOGIN, false);
    }

    public static XMPPService getmService() {
        return xmppService;
    }

    //LifeCycle Methods

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {


    }

    @Override
    public void onActivityStarted(Activity activity) {

        try {
            if (getmService().xmpp != null) {
                if (isAppOnForeground(this)) {
                    getmService().xmpp.available();
                } else {
                    getmService().xmpp.unavailable();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onActivityResumed(Activity activity) {


    }

    @Override
    public void onActivityStopped(Activity activity) {
        try {
            if (getmService().xmpp != null) {
                if (isAppOnForeground(this)) {
                    getmService().xmpp.available();
                } else {
                    getmService().xmpp.unavailable();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static boolean isAppOnForeground(Context context) {
        boolean isForeGround = false;
        boolean isNotSleep = false;
        boolean isFound = false;
        try {
            try {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    if (pm.isInteractive()) {
                        isNotSleep = true;
                    } else {
                        isNotSleep = false;
                    }
                } else {
                    if (pm.isScreenOn()) {
                        isNotSleep = true;
                    } else {
                        isNotSleep = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                    if (appProcesses == null) {
                        isFound = false;
                    } else {
                        final String packageName = context.getPackageName();
                        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                                isFound = true;
                            }
                        }
                    }
                } else {
                    List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
                    ComponentName componentInfo = taskInfo.get(0).topActivity;
                    if (componentInfo.getPackageName().equals(context.getPackageName())) {
                        isFound = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("APP_STATUS", "isNotSleep > " + isNotSleep);
            Log.e("APP_STATUS", "isFound > " + isFound);

            if (isNotSleep && isFound) {
                isForeGround = true;
            } else {
                isForeGround = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("APP_STATUS", "isForeGround > " + isForeGround);

        return isForeGround;
    }
}
