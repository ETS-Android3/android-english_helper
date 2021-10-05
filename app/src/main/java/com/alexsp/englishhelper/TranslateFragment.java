package com.alexsp.englishhelper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.alexsp.englishhelper.databinding.FragmentTranslateBinding;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TranslateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TranslateFragment extends Fragment {
    private View view;
    private FragmentTranslateBinding binding;
    private final Handler uiHandler = new Handler();
    private String MyLanguage = "es";
    private String MyLanguageButtonString = "Es";
    private final IgnoreFlag englishTextModified = new IgnoreFlag();     // avoid recursion between EditBox.setText() and EditBox.afterTextChanged
    private final IgnoreFlag myLanguageTextModified = new IgnoreFlag();  // avoid recursion between EditBox.setText() and EditBox.afterTextChanged
    private TranslationDirection lastSearchDirection;
    private boolean alreadyCreated = false;
    private DictionarySelection dictionarySelection;
    ProgressBarInterface progressBarInterface;
    private TranslationDirection lastEdit = TranslationDirection.fromEnglish;
    private long search_delay_ms = (long)(Float.parseFloat(GlobalSettings.getString("SearchDelay", "1"))*1000);



    public TranslateFragment()
    {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TranslateFragment.
     */
    public static TranslateFragment newInstance()
    {
        TranslateFragment fragment = new TranslateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (!alreadyCreated) {
            // Inflate the layout for this fragment
            binding = FragmentTranslateBinding.inflate(inflater, container, false);
            binding.web.getSettings().setBuiltInZoomControls(true);
            // hide web controls
            binding.cardWeb.setVisibility(View.GONE);
            binding.tabDictionariesLayout.setVisibility(View.GONE);
            binding.buttonUp.setVisibility(View.GONE);
            // load dictionaries list
            dictionarySelection = new DictionarySelection(this, binding.tabDictionariesLayout, () -> GetDictionary());
            binding.tabDictionariesLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            // progress bar
            progressBarInterface = ((MainActivity)getActivity()).getProgressBarInterface();
        }
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        ErrorDisplay.SetView(view);
        MyLanguage = GlobalSettings.getString("MyLanguage", "ES");
        MyLanguageButtonString = MyLanguage.substring(0, 1).toUpperCase() + MyLanguage.substring(1).toLowerCase();
        resizeProportions();
        search_delay_ms = (long)(Float.parseFloat(GlobalSettings.getString("SearchDelay", "1"))*1000);
        binding.web.getSettings().setTextZoom(GlobalSettings.getInt("WebTextZoom", 100));

        // show translation buttons when automatic translation is disabled
        if (!GlobalSettings.getBoolean("EnableAutomaticSearch", true))
        {
            binding.buttonFromEnglish.setText("En→" + MyLanguageButtonString);
            binding.buttonFromEnglish.setVisibility(View.VISIBLE);
            binding.buttonFromMyLanguage.setText(MyLanguageButtonString+"→En");
            binding.buttonFromMyLanguage.setVisibility(View.VISIBLE);
            binding.buttonFromEnglish.setOnClickListener(view1 -> {
                lastSearchDirection = TranslationDirection.fromMyLanguage;
                doTranslate(TranslationDirection.fromEnglish);
            });
            binding.buttonFromMyLanguage.setOnClickListener(view1 -> {
                lastSearchDirection = TranslationDirection.fromMyLanguage;
                doTranslate(TranslationDirection.fromMyLanguage);
            });
        }
        else
        {
            binding.buttonFromEnglish.setVisibility(View.GONE);
            binding.buttonFromMyLanguage.setVisibility(View.GONE);
        }


        if (!alreadyCreated)
        {
            progressBarInterface.Stop();
            binding.web.setWebViewClient(new WvClient(progressBarInterface));

            // button clear click
            binding.buttonClear.setOnClickListener(view1 -> {
                SetPreTranslateState();
            });

            // button edit click
            binding.buttonEdit.setOnClickListener(view1 -> {
                ShowEnglishText();
                binding.englishText.requestFocus();
            });

            // button up click
            binding.buttonUp.setOnClickListener(view1 -> {
                binding.scrollView.fullScroll(ScrollView.FOCUS_UP);
            });

            // dictionary tabs click
            binding.tabDictionariesLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
            {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    GetDictionary();
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) { dictionarySelection.showMenu(tab); }
            });

            // English text timeout
            binding.englishText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                final DelayTimer delay = new DelayTimer(uiHandler, search_delay_ms, new DelayTimer.OnRunTask() {
                    @Override
                    public void run() {
                        doTranslate(TranslationDirection.fromEnglish);
                    }
                });

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!GlobalSettings.getBoolean("EnableAutomaticSearch", true))
                        return;
                    if (!englishTextModified.get())
                    {
                        lastEdit = TranslationDirection.fromEnglish;
                        delay.Start();
                    }
                }
            });

            // My language text timeout
            binding.mylanguageText.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                final DelayTimer delay = new DelayTimer(uiHandler, search_delay_ms, new DelayTimer.OnRunTask() {
                    @Override
                    public void run() {
                        doTranslate(TranslationDirection.fromMyLanguage);
                    }
                });

                @Override
                public void afterTextChanged(Editable editable)
                {
                    if (!GlobalSettings.getBoolean("EnableAutomaticSearch", true))
                        return;
                    if (!myLanguageTextModified.get())
                    {
                        lastEdit = TranslationDirection.fromMyLanguage;
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
            myLanguageTextModified.setIgnore();
        }
    }

    /**
     * Resizes texts/buttons card and web card proportionally from screen resolution
     */
    private void resizeProportions()
    {
        ViewGroup.LayoutParams params;
        int screen_height = ScreenUtils.screenHeight(getContext());
        int buttons_height = binding.layoutButtons.getLayoutParams().height;
        float buttons_height_proportion = (float)buttons_height / screen_height;
        int text_height = Math.round((float)screen_height / 12);
        int card_height = Math.round((float)screen_height / (float)6) + buttons_height;
        ScreenUtils.ViewGroupSetHeight(binding.englishTextLayout, text_height);
        ScreenUtils.ViewGroupSetHeight(binding.mylanguageTextLayout, text_height);
        ScreenUtils.ViewGroupSetHeight(binding.cardTranslation, card_height);
        binding.englishChipsLayoutScroll.setMaxHeight(text_height);
        binding.englishChipsLayoutScroll.setMinimumHeight(text_height);
    }


    private void doTranslate(TranslationDirection direction)
    {
        lastSearchDirection = direction;
        binding.englishChipsLayout.removeAllViews();
        progressBarInterface.Start();
        TranslationProvider tp = null;
        if (GlobalSettings.getString("TranslationProvider", "libretranslate").compareTo("deepl") == 0)
        {
            tp = new TranslationProviderDeepL(new TranslationProvider.OnCompleted()
            {
                @Override
                public void completed(ArrayList<String> translations, TranslationDirection direction)
                {
                    if (direction == TranslationDirection.fromEnglish)
                    {
                        myLanguageTextModified.setIgnore();
                        binding.mylanguageText.setText(translations.get(0));
                    }
                    else
                    {
                        englishTextModified.setIgnore();
                        binding.englishText.setText(translations.get(0));
                    }
                    SetPostTranslateState();
                }
            });
            String text;
            if (direction == TranslationDirection.fromEnglish)
                text = binding.englishText.getText().toString().trim();
            else
                text = binding.mylanguageText.getText().toString().trim();
            tp.translate(text, direction);
        }
        else if (GlobalSettings.getString("TranslationProvider", "libretranslate").compareTo("libretranslate") == 0)
        {
            tp = new TranslationProviderLibreTranslate((translations, direction1) -> {
                if (direction1 == TranslationDirection.fromEnglish)
                {
                    myLanguageTextModified.setIgnore();
                    binding.mylanguageText.setText(translations.get(0));
                }
                else
                {
                    englishTextModified.setIgnore();
                    binding.englishText.setText(translations.get(0));
                }
                SetPostTranslateState();
            });
            String text;
            if (direction == TranslationDirection.fromEnglish)
                text = binding.englishText.getText().toString().trim();
            else
                text = binding.mylanguageText.getText().toString().trim();
            tp.translate(text, direction);
        }
    }

    private Chip CreateChip(String w)
    {
        Chip chip = new Chip(this.getContext());
        chip.setText(w);
        chip.setCheckable(true);
        chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            final DelayTimer t = new DelayTimer(uiHandler, new DelayTimer.OnRunTask() {
                @Override
                public void run()
                {
                    GetDictionary();
                }
            });
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                t.Start();
            }
        });
        return chip;
    }


    private void AddChips(String text, FlexboxLayout chipGroup)
    {
        for (String w : text.split(" "))
            chipGroup.addView(CreateChip(w));
    }

    private void AddChips(String text, ChipGroup chipGroup)
    {
        for (String w : text.split(" "))
            chipGroup.addView(CreateChip(w));
    }

    private void SetPostTranslateState()
    {
        String text = binding.englishText.getText().toString();
        if (!text.isEmpty())
        {
            // add chips from text
            AddChips(text, binding.englishChipsLayout);
            // switch view
            binding.englishChipsLayoutScroll.setVisibility(View.VISIBLE);
            binding.englishTextLayout.setVisibility(View.GONE);
            if (binding.englishChipsLayout.getChildCount() == 1)
            {
                ((Chip)binding.englishChipsLayout.getChildAt(0)).setChecked(true);
            }
        }
        else
        {
            // if no text, it's a clear
            binding.englishChipsLayoutScroll.setVisibility(View.GONE);
            binding.englishTextLayout.setVisibility(View.VISIBLE);
        }
        progressBarInterface.Stop();
        KeyboardUtils.hideKeyboard(getActivity());
    }

    private void ShowEnglishText()
    {
        // switch view
        binding.englishChipsLayout.removeAllViews();
        binding.englishChipsLayoutScroll.setVisibility(View.GONE);
        binding.englishTextLayout.setVisibility(View.VISIBLE);
    }


    private void SetPreTranslateState()
    {
        // clear controls
        binding.englishChipsLayout.removeAllViews();
        englishTextModified.setIgnore();
        binding.englishText.setText("");
        myLanguageTextModified.setIgnore();
        binding.mylanguageText.setText("");
        if (lastEdit == TranslationDirection.fromEnglish)
            binding.englishText.requestFocus();
        else
            binding.mylanguageText.requestFocus();
        KeyboardUtils.showKeyboard(getActivity());

        // switch view
        binding.englishChipsLayoutScroll.setVisibility(View.GONE);
        binding.englishTextLayout.setVisibility(View.VISIBLE);
        binding.tabDictionariesLayout.setVisibility(View.GONE);
        binding.cardWeb.setVisibility(View.GONE);
        binding.web.setVisibility(View.GONE);
        binding.tabDictionariesLayout.setVisibility(View.GONE);
        binding.buttonUp.setVisibility(View.GONE);
        progressBarInterface.Stop();
    }

    private void GetDictionary()
    {
        StringBuilder text = new StringBuilder();
        int tab = binding.tabDictionariesLayout.getSelectedTabPosition();


        if (lastSearchDirection == TranslationDirection.fromMyLanguage &&
            dictionarySelection.getSelectedEntry(tab).type == DictionaryType.Translate)
        {
            text.append(binding.mylanguageText.getText());
        }
        else
        {
            for (int i = 0; i < binding.englishChipsLayout.getChildCount(); ++i) {
                Chip c = (Chip) binding.englishChipsLayout.getChildAt(i);
                if (c.isChecked())
                    text.append(" ").append(c.getText());
            }
        }


        try
        {
            if (!text.toString().isEmpty())
            {
                progressBarInterface.Start();
                String languageFrom;
                String languageTo;
                if (lastSearchDirection == TranslationDirection.fromMyLanguage)
                {
                    languageFrom = MyLanguage;
                    languageTo = "EN";
                }
                else
                {
                    languageFrom = "EN";
                    languageTo = MyLanguage;
                }
                binding.web.setVisibility(View.VISIBLE);
                binding.cardWeb.setVisibility(View.VISIBLE);
                binding.tabDictionariesLayout.setVisibility(View.VISIBLE);
                binding.tabDictionariesLayout.setVisibility(View.VISIBLE);
                binding.buttonUp.setVisibility(View.VISIBLE);
                String url = dictionarySelection.composeUrl(tab, text.toString().trim(), languageFrom, languageTo);
                setWebSettingsForSelectedTab(tab);
                binding.web.loadUrl(url);
            }
        }
        catch (Exception e)
        {
            ShowError(e.getMessage());
        }
        progressBarInterface.Stop();
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

    private void ShowError(String msg)
    {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

}