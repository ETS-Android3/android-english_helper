package com.alexsp.englishhelper;

import android.content.Context;

import java.util.Arrays;
import java.util.Locale;

public class UrlComposerLinguee implements UrlComposer
{
    String[] lang_values;
    String[] lang_entries;

    private String searchLanguage(String lang)
    {
        if (lang.compareTo("EN") == 0)
        {
            return "english";
        }
        else
        {
            int pos = Arrays.asList(lang_values).indexOf(lang);
            if (pos > -1)
                return lang_entries[pos].toLowerCase(Locale.ROOT);
            else
                return "";
        }
    }


    public String composeUrl(Context context, String originalUrl, String text, String languageFrom, String languageTo)
    {
        lang_values = context.getResources().getStringArray(R.array.lang_values);
        lang_entries = context.getResources().getStringArray(R.array.lang_entries);

        String lang_from = searchLanguage(languageFrom);
        String lang_to = searchLanguage(languageTo);
        if (lang_from.isEmpty() || lang_to.isEmpty())
            return "";
        String url = originalUrl.replaceAll("%TEXT%", text);
        if (!languageFrom.isEmpty())
            url = url.replaceAll("%FROM%", lang_from);
        if (!languageTo.isEmpty())
            url = url.replaceAll("%TO%", lang_to);
        return url;
    }
}
