package com.alexsp.englishhelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class TranslationProviderLibreTranslate extends TranslationProvider
{

    public TranslationProviderLibreTranslate(OnCompleted onCompleted) {
        super(onCompleted);
        requestType = RequestType.post;
    }

    protected String composeUrl(String textToTranslate)
    {
        try
        {
            String baseUrl = "https://" + GlobalSettings.getString("LibreTranslateServer", "") + "/translate";
            String encoded_text = URLEncoder.encode(textToTranslate, "utf-8");
            String from_lang;
            String to_lang;
            if (direction == TranslationDirection.fromEnglish)
            {
                from_lang = "en";
                to_lang = GlobalSettings.getString("MyLanguage", "ES").toLowerCase(Locale.ROOT);
            }
            else
            {
                from_lang = GlobalSettings.getString("MyLanguage", "ES").toLowerCase(Locale.ROOT);
                to_lang = "en";
            }
            String url = baseUrl + "?&q=" + encoded_text + "&source=" + from_lang + "&target=" + to_lang + "&format=text";
            return url;
        }
        catch (Exception e)
        {
            ErrorDisplay.Show(e);
            return "";
        }
    }

    protected ArrayList<String> parseResponse(JSONObject response)
    {
        ArrayList<String> ret = new ArrayList<>();
        try
        {
            String translatedText = response.getString("translatedText");
            ret.add(translatedText);
        }
        catch (JSONException e)
        {
            ErrorDisplay.Show(e);
        }
        return ret;
    }


}
