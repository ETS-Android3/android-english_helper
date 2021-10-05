package com.alexsp.englishhelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class TranslationProviderDeepL extends TranslationProvider
{

    public TranslationProviderDeepL(OnCompleted onCompleted)
    {
        super(onCompleted);
    }

    protected String composeUrl(String textToTranslate)
    {
        try
        {
            String baseUrl = "https://api-free.deepl.com/v2/translate";
            String auth_key = GlobalSettings.getString("DeepLAuthKey", "");
            String encoded_text = URLEncoder.encode(textToTranslate, "utf-8");
            String from_lang;
            String to_lang;
            if (direction == TranslationDirection.fromEnglish)
            {
                from_lang = "EN";
                to_lang = GlobalSettings.getString("MyLanguage", "ES");
            }
            else
            {
                from_lang = GlobalSettings.getString("MyLanguage", "ES");
                to_lang = "EN";
            }
            String url = baseUrl + "?auth_key=" + auth_key + "&text=" + encoded_text + "&source_lang=" + from_lang + "&target_lang=" + to_lang;
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
            String translatedText = response.optJSONArray("translations").getJSONObject(0).getString("text");
            ret.add(translatedText);
        }
        catch (JSONException e)
        {
            ErrorDisplay.Show(e);
        }
        return ret;
    }


}
