package com.alexsp.englishhelper;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class WvClient extends WebViewClient
{
    ProgressBarInterface progressBarInterface = null;

    public WvClient()
    {
    }

    public WvClient(ProgressBarInterface progressBarInterface)
    {
        this.progressBarInterface = progressBarInterface;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError er)
    {
        handler.proceed();
        // Ignore SSL certificate errors
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon)
    {
        if (progressBarInterface != null)
            progressBarInterface.Start();
    }

    @Override
    public void onPageFinished(WebView view, String url)
    {
        if (progressBarInterface != null)
            progressBarInterface.Stop();
    }

    public void setProgressBarInterface(ProgressBarInterface progressBarInterface)
    {
        this.progressBarInterface = progressBarInterface;
    }


}
