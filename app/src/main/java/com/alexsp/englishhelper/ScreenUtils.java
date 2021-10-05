package com.alexsp.englishhelper;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

public class ScreenUtils
{
    static int _screenHeightDp = 0;
    static int _screenWidthDp = 0;
    static int _screenHeight = 0;
    static int _screenWidth = 0;

    public static int screenWidth(Context context)
    {
        if (_screenHeightDp == 0)
            calcScreenDimensions(context);
        return _screenWidth;
    }

    public static int screenHeight(Context context)
    {
        if (_screenHeight == 0)
            calcScreenDimensions(context);
        return _screenHeight;
    }

    public static int screenWidthDp(Context context)
    {
        if (_screenHeightDp == 0)
            calcScreenDimensions(context);
        return _screenWidthDp;
    }

    public static int screenHeightDp(Context context)
    {
        if (_screenHeightDp == 0)
            calcScreenDimensions(context);
        return _screenHeightDp;
    }

    private static void calcScreenDimensions(Context context)
    {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenHeight = displayMetrics.heightPixels / displayMetrics.density;
        float screenWidth = displayMetrics.widthPixels / displayMetrics.density;
        _screenHeight = displayMetrics.heightPixels;
        _screenWidth = displayMetrics.widthPixels;
        _screenHeightDp = Math.round(screenHeight);
        _screenWidthDp = Math.round(screenWidth);
    }

    public static int convertDpToPixels(Context context, int dps)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = Math.round(dps * scale + 0.5f);
        return pixels;
    }

    public static void ViewGroupSetHeight(ViewGroup v, int height)
    {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.height = height;
        v.setLayoutParams(params);
    }

    public static void ViewGroupSetWidth(ViewGroup v, int width)
    {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.width = width;
        v.setLayoutParams(params);
    }

}
