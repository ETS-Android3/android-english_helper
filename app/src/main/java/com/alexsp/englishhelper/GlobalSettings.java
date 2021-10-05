package com.alexsp.englishhelper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class GlobalSettings
{

    static SharedPreferences prefs = null;

    public static void setContext(Context context)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getString(String setting, String def)
    {
        if (prefs == null)
            return def;
        return prefs.getString(setting, def);
    }

    public static void setString(String setting, String value)
    {
        if (prefs == null)
            return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(setting, value);
        editor.commit();
    }

    public static int getInt(String setting, int def)
    {
        if (prefs == null)
            return def;
        return prefs.getInt(setting, def);
    }

    public static void setInt(String setting, int value)
    {
        if (prefs == null)
            return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(setting, value);
        editor.commit();
    }

    public static long getLong(String setting, long def)
    {
        if (prefs == null)
            return def;
        return prefs.getLong(setting, def);
    }

    public static void setLong(String setting, long value)
    {
        if (prefs == null)
            return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(setting, value);
        editor.commit();
    }

    public static float getFloat(String setting, float def)
    {
        if (prefs == null)
            return def;
        return prefs.getFloat(setting, def);
    }

    public static void setFloat(String setting, float value)
    {
        if (prefs == null)
            return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(setting, value);
        editor.commit();
    }

    public static boolean getBoolean(String setting, boolean def)
    {
        if (prefs == null)
            return def;
        return prefs.getBoolean(setting, def);
    }

    public static void setBoolean(String setting, boolean value)
    {
        if (prefs == null)
            return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(setting, value);
        editor.commit();
    }

    public static boolean contains(String setting)
    {
        if (prefs == null)
            return false;
        return prefs.contains(setting);
    }


}
