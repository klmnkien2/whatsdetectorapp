package com.crazylifevn.whatsdetectorapp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final int REQUEST_CODE_PICK_WHATSAPP = 111;  
    public static final String MyPREFERENCES = "detectorAppPrefs" ;
    public static final String KEY_VALUE = "usingNumber"; 

    SharedPreferences sharedpreferences;

    private ActivityManager am;
    private Runnable myRunnable;
    private boolean threadDone = true;
    private Handler mHandler;
    boolean is_track;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        
        admob();
        admobInterstitialAd();
        
        setMobileDataEnabled(getApplicationContext(), true);
        setWifiDataEnabled(getApplicationContext(), true);
        
        findViewById(R.id.button_contact).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setMobileDataEnabled(getApplicationContext(), false);
                setWifiDataEnabled(getApplicationContext(), false);
                
                int opened = 0;
                if (sharedpreferences.contains(KEY_VALUE))
                {
                    try {
                        opened = Integer.parseInt(sharedpreferences.getString(KEY_VALUE, "0"));
                    } catch(Exception e) {}
                }
                
                if(opened < 5) {
                    opened ++;
                    Editor editor = sharedpreferences.edit();
                    editor.putString(KEY_VALUE, "" + opened);

                    editor.commit(); 
                }
                
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setPackage("com.whatsapp");
                try{
                    startActivityForResult(intent, REQUEST_CODE_PICK_WHATSAPP);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, R.string.notif_no_whatsapp, Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                findViewById(R.id.home_view).setVisibility(View.VISIBLE);
                findViewById(R.id.tracker_view).setVisibility(View.GONE);
                
                is_track = false;
                admobInterstitialAd();
                loadingData();
            }
        });
        
        findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmExit();  
            }
        });
        
        findViewById(R.id.button_config).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                
                final String appPackageName = "com.crazylifevn.hideonlinestatuspro"; 
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        
        findViewById(R.id.button_rateus).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String appPackageName = getPackageName(); 
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        
        
        onStartTracker(); 
        
        is_track = getIntent().getBooleanExtra("IS_TRACK", false);
        if(is_track) {
            findViewById(R.id.home_view).setVisibility(View.GONE);
            findViewById(R.id.tracker_view).setVisibility(View.VISIBLE);
            findViewById(R.id.button_back).requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            loadingData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
        case REQUEST_CODE_PICK_WHATSAPP:
            if(resultCode == RESULT_OK){
                if(intent.hasExtra("contact")){
                    String address = intent.getStringExtra("contact");
                    
                    openConversation(address);
                }
            }
            break;

        default:
            break;
        }
    }

    public void openConversation(String id) {  
        
        Cursor c = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[] { ContactsContract.Contacts.Data._ID }, ContactsContract.Data.DATA1 + "=?",
                new String[] { id }, null);
        c.moveToFirst();
        Intent i = new Intent(Intent.ACTION_DEFAULT, Uri.parse("content://com.android.contacts/data/" + c.getString(0)));

        startActivity(i);
        c.close();

        new Thread(new Runnable() {
            public void run() {
                while (threadDone) {

                    try {
                        mHandler.post(myRunnable);
                    } catch (Exception e) {

                    }
                }
                
            }
        }).start();
        
    }

    public void onStartTracker() {
        
        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mHandler = new Handler();
        
        myRunnable = new Runnable() {

            @Override
            public void run() {
                checkFunction();
            }
        };

    }

    private void checkFunction() {

        RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
        String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getClassName();        
        
        if (foregroundTaskPackageName.equals("com.whatsapp.Conversation")) {
                          
            finish();
            Intent lockIntent = new Intent(getBaseContext(), MainActivity.class);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            lockIntent.putExtra("IS_TRACK", true);
            getBaseContext().startActivity(lockIntent);            
            
        } else {
            threadDone = false;
            mHandler.removeCallbacks(myRunnable); 
        }
    }
    
    public void setWifiDataEnabled(Context context, boolean status) {
        
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(status);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setMobileDataEnabled(Context context, boolean enabled) {
        
        try {
            final ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
            connectivityManagerField.setAccessible(true);
            final Object connectivityManager = connectivityManagerField.get(conman);
            final Class connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
        }catch (Exception e) {

        }
    }
    
    public void confirmExit() {

        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setTitle(null);
        confirm.setMessage("Does application help you? If it does, please rate us!");

        confirm.setNegativeButton("Rate us", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface downloadDialog, int which) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        confirm.setPositiveButton("Later", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showAdWhenExit();
            }
        });

        confirm.show().show();
    }

    private AdView adView;
    private InterstitialAd interstitial;

    private void admobInterstitialAd() {
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-1437033819551815/4728700283");

        AdRequest adRequest = new AdRequest.Builder().build();

        interstitial.loadAd(adRequest);
    }
    
    /*
     * Open app only when ads loaded
     */
    private void loadingData() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading contact data...");
        dialog.setCancelable(false);
        dialog.show();
        
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (dialog!=null) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();     
                        saveOpenedData();
                    }
                }  
            }
        }, 7000);
    }
    
    public void saveOpenedData() {
        
        int opened = 0;
        if (sharedpreferences.contains(KEY_VALUE))
        {
            try {
                opened = Integer.parseInt(sharedpreferences.getString(KEY_VALUE, "0"));
            } catch(Exception e) {}
        }
        
        if(opened >= 5) {
            if (interstitial.isLoaded()) {
                interstitial.show();
            }
        }
    }

    public void showAdWhenExit() {        

        if (interstitial.isLoaded()) {
            interstitial.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
                }
            });
            interstitial.show();
        } else {
            startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
        }
    }

    private void admob() {
        adView = (AdView) findViewById(R.id.adView); 

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
        
        setMobileDataEnabled(getApplicationContext(), true);
        setWifiDataEnabled(getApplicationContext(), true);
        admobInterstitialAd();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adView != null) adView.pause();  
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adView != null) adView.destroy();   
    }

    @Override
    public void onBackPressed() {  
        if(is_track) {
            
        } else {
            confirmExit();
        }
    }
}
