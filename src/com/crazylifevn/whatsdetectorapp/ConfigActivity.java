package com.crazylifevn.whatsdetectorapp;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.CheckBox;

public class ConfigActivity extends Activity {

    private int networkMode; // 1,all - 2,wifi - 3,mobile
    private CheckBox check1, check2, check3;
    public static final String MyPREFERENCES = "detectorAppPrefs" ;
    public static final String KEY_VALUE = "networkMode"; 

    SharedPreferences sharedpreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        
        admob();
        admobInterstitialAd();
        
        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showAdWhenBack();
            }
        });
        
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(KEY_VALUE))
        {
            try {
                networkMode = Integer.parseInt(sharedpreferences.getString(KEY_VALUE, "1"));
            } catch(Exception e) {
                networkMode = 1;
            }
        }
        
        addListenerOnCheckbox();    
    }
    
    public void addListenerOnCheckbox() {

        check1 = (CheckBox) findViewById(R.id.check1);
        check2 = (CheckBox) findViewById(R.id.check2);
        check3 = (CheckBox) findViewById(R.id.check3);
        
        if(networkMode == 1) {
            check1.setChecked(true);
            check2.setChecked(false);
            check3.setChecked(false);
        } else if(networkMode == 2) {
            check1.setChecked(false);
            check2.setChecked(true);
            check3.setChecked(false);
        } else if(networkMode == 3) {
            check1.setChecked(false);
            check2.setChecked(false);
            check3.setChecked(true);
        }

        check1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {                              
                if (((CheckBox) v).isChecked()) {
                    check2.setChecked(false);
                    check3.setChecked(false);
                    networkMode = 1;
                    savePreference();
                } else {
                    ((CheckBox) v).setChecked(true);
                }
                
            }
        });

        check2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {                              
                if (((CheckBox) v).isChecked()) {
                    check1.setChecked(false);
                    check3.setChecked(false);
                    networkMode = 2;
                    savePreference();
                }     else {
                    ((CheckBox) v).setChecked(true);
                }       
            }
        });

        check3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {                              
                if (((CheckBox) v).isChecked()) {
                    check2.setChecked(false);
                    check1.setChecked(false);
                    networkMode = 3;
                    savePreference();
                }   else {
                    ((CheckBox) v).setChecked(true);
                }         
            }
        });

    }
    
    public void savePreference() {
        Editor editor = sharedpreferences.edit();
        editor.putString(KEY_VALUE, "" + networkMode);

        editor.commit(); 
    }

    private AdView adView;
    private InterstitialAd interstitial;

    private void admobInterstitialAd() {
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-1437033819551815/4739033489");

        AdRequest adRequest = new AdRequest.Builder().build();

        interstitial.loadAd(adRequest);
    }

    public void showAdWhenBack() {        

        if (interstitial.isLoaded()) {
            interstitial.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    NavUtils.navigateUpFromSameTask(ConfigActivity.this);
                }
            });
            interstitial.show();
        } else {
            NavUtils.navigateUpFromSameTask(ConfigActivity.this);
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
        showAdWhenBack(); 
    }
}
