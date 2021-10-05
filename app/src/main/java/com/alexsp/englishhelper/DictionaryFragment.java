package com.alexsp.englishhelper;

import com.alexsp.englishhelper.databinding.FragmentDictionaryBinding;
import com.google.android.material.tabs.TabLayout;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ScrollView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DictionaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DictionaryFragment extends Fragment
{

    private final Handler uiHandler = new Handler();
    private boolean alreadyCreated = false;
    DictionarySelection dictionarySelection;
    private FragmentDictionaryBinding binding;
    private View view;
    private final IgnoreFlag englishTextModified = new IgnoreFlag();     // avoid recursion between EditBox.setText() and EditBox.afterTextChanged
    ProgressBarInterface progressBarInterface;

    public DictionaryFragment()
    {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DictionaryFragment.
     */
    public static DictionaryFragment newInstance()
    {
        DictionaryFragment fragment = new DictionaryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //CreateDictionaryList();
        progressBarInterface = ((MainActivity)getActivity()).getProgressBarInterface();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (!alreadyCreated)
        {
            // Inflate the layout for this fragment
            binding = FragmentDictionaryBinding.inflate(inflater, container, false);
            // configure dictionary selection tabs
            dictionarySelection = new DictionarySelection(this, binding.tabDictionariesLayout, () -> GetDictionary());
            // remove translation tab and select dictionary tab
            ((ViewGroup)binding.tabDictionariesLayout.getTabAt(0).view).setVisibility(View.GONE);
            binding.tabDictionariesLayout.selectTab(binding.tabDictionariesLayout.getTabAt(1));
            // set webview settings
            binding.web.getSettings().setBuiltInZoomControls(true);
            // hide tabs and webview
            binding.web.setVisibility(View.GONE);
            binding.cardWeb.setVisibility(View.GONE);
            binding.buttonUp.setVisibility(View.GONE);
            binding.tabDictionariesLayout.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        ErrorDisplay.SetView(view);

        // enable/disable search button from preference
        if (GlobalSettings.getBoolean("EnableAutomaticSearch", true))
        {
            binding.searchButton.setVisibility(View.GONE);
        }
        else
        {
            binding.searchButton.setVisibility(View.VISIBLE);
            binding.searchButton.setOnClickListener(view1 -> {
                GetDictionary();
            });
        }
        binding.web.getSettings().setTextZoom(GlobalSettings.getInt("WebTextZoom", 100));

        if (!alreadyCreated)
        {
            // web browser
            binding.web.setWebViewClient(new WvClient(progressBarInterface));
            binding.englishText.requestFocus();

            // buttonUp click
            binding.buttonUp.setOnClickListener(view1 -> {
                binding.scrollView.fullScroll(ScrollView.FOCUS_UP);
            });

            // tabs click
            binding.tabDictionariesLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override public void onTabSelected(TabLayout.Tab tab) {
                    GetDictionary();
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) { dictionarySelection.showMenu(tab); }
            });

            // English text timeout
            binding.englishText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                final DelayTimer delay = new DelayTimer(uiHandler, new DelayTimer.OnRunTask() {
                    @Override
                    public void run() {
                        GetDictionary();
                    }
                });
                @Override
                public void afterTextChanged(Editable editable) {
                    if (!GlobalSettings.getBoolean("EnableAutomaticSearch", true))
                        return;
                    if (!englishTextModified.get()) {
                        delay.Start();
                    }
                }
            });
            alreadyCreated = true;
        }
        else
        {
            // avoid text changed event to raise when re-activating the fragment
            englishTextModified.setIgnore();
        }
    }

    private void GetDictionary()
    {
        try
        {
            String text = binding.englishText.getText().toString();
            if (!text.isEmpty())
            {
                int tab = binding.tabDictionariesLayout.getSelectedTabPosition();
                setWebSettingsForSelectedTab(tab);
                binding.web.loadUrl(dictionarySelection.composeUrl(tab, text, "", ""));
                binding.web.setVisibility(View.VISIBLE);
                binding.cardWeb.setVisibility(View.VISIBLE);
                binding.buttonUp.setVisibility(View.VISIBLE);
                binding.tabDictionariesLayout.setVisibility(View.VISIBLE);
            }
            else
            {
                Clear();
            }
        }
        catch (Exception e)
        {
            ErrorDisplay.Show(e);
        }
        KeyboardUtils.hideKeyboard(getActivity());
    }

    private void Clear()
    {
        // clear controls
        englishTextModified.setIgnore();
        binding.englishText.setText("");
        // switch view
        binding.web.setVisibility(View.GONE);
        binding.cardWeb.setVisibility(View.GONE);
        binding.tabDictionariesLayout.setVisibility(View.GONE);
        binding.buttonUp.setVisibility(View.GONE);
        KeyboardUtils.showKeyboard(getActivity());
    }

    private void setWebSettingsForSelectedTab(int tab)
    {
        if (dictionarySelection.getSelectedEntry(tab).javascript)
            binding.web.getSettings().setJavaScriptEnabled(true);
        else
            binding.web.getSettings().setJavaScriptEnabled(false);
        if (dictionarySelection.getSelectedEntry(tab).cookies)
            CookieManager.getInstance().setAcceptCookie(true);
        else
            CookieManager.getInstance().setAcceptCookie(false);
    }

}
