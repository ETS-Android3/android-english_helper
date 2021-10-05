package com.alexsp.englishhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class MainActivity extends AppCompatActivity {

    TranslateFragment translateFragment;
    DictionaryFragment dictionaryFragment;
    SettingsFragment settingsFragment;
    CircularProgressIndicator progressBar;
    static ProgressBarInterface progressBarInterface;
    static Context context;
    static MainActivity me;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
        setContentView(R.layout.activity_main);
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            System.exit(1);
        } catch (GooglePlayServicesNotAvailableException e) {
            System.exit(1);
        }
        context = getApplicationContext();

        // initialize settings accessor
        GlobalSettings.setContext(this);

        // initialize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize progress bar (set to stopped)
        progressBar = findViewById(R.id.titleProgressBar);
        progressBarHide();

        // initialize fragments
        translateFragment = TranslateFragment.newInstance();
        dictionaryFragment = DictionaryFragment.newInstance();
        settingsFragment = new SettingsFragment();

        // initialize app with start page setting
        FragmentContainerView fragmentContainerView = (FragmentContainerView) findViewById(R.id.fragmentContainerView);
        if (GlobalSettings.getString("StartPage", "translation").compareTo("translation") == 0)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, translateFragment).commit();
        else
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, dictionaryFragment).commit();

        // set translate icon event
        ImageView iconTranslate = (ImageView) findViewById(R.id.iconTranslate);
        iconTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, translateFragment)
                        .commit();
            }
        });

        // set dictionary icon event
        ImageView iconDictionary = (ImageView) findViewById(R.id.iconDictionary);
        iconDictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, dictionaryFragment)
                        .commit();

            }
        });

        // set settings icon event
        ImageView iconSettings = (ImageView) findViewById(R.id.iconGear);
        iconSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, settingsFragment)
                        .commit();

            }
        });

        // set progress bar interface so fragments can use it
        progressBarInterface = new ProgressBarInterface() {
            @Override
            public void Start() {
                progressBarShow();
            }

            @Override
            public void Stop() { progressBarHide(); }
        };

    }

    public static Context getContext()
    {
        return context;
    }
    public static ProgressBarInterface getProgressBarInterface()
    {
        return progressBarInterface;
    }

    public static void stopProgressBar()
    {
        progressBarInterface.Stop();
    }

    public void progressBarHide()
    {
        progressBar.hide();
        progressBar.setProgress(1);
        progressBar.setMax(1);
        progressBar.setIndeterminate(false);
        progressBar.show();
    }

    public void progressBarShow()
    {
        progressBar.hide();
        progressBar.setIndeterminate(true);
        progressBar.show();
    }

    static public void End()
    {
        System.exit(1);
    }
}