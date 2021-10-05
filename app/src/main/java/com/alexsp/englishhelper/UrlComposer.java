package com.alexsp.englishhelper;

import android.content.Context;

import java.io.UnsupportedEncodingException;

public interface UrlComposer
{
    public String composeUrl(Context context, String url, String text, String languageFrom, String languageTo);
}
