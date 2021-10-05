package com.alexsp.englishhelper;

import android.content.Context;

import java.util.Arrays;
import java.util.Locale;

public class UrlComposerGoogleTranslate implements UrlComposer
{
    public String composeUrl(Context context, String originalUrl, String text, String languageFrom, String languageTo)
    {
        String lang_from = languageFrom.toLowerCase(Locale.ROOT);
        String lang_to = languageTo.toLowerCase(Locale.ROOT);
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

