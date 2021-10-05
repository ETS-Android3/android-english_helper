package com.alexsp.englishhelper;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLContext;

import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;

public class TranslationProvider
{

    OnCompleted onCompleted;
    TranslationDirection direction;

    enum RequestType { post, get }
    RequestType requestType = RequestType.get;

    public TranslationProvider(OnCompleted onCompleted)
    {
        this.onCompleted = onCompleted;
        FixSSl();
    }

    public void translate(String text, TranslationDirection direction)
    {
        TaskRunner tr = new TaskRunner();
        this.direction = direction;
        tr.executeAsync(new GetHttpTask(composeUrl(text)), new GetHttpComplete(direction));
    }




    protected String composeUrl(String textToTranslate)
    {
        return ""; // stub
    }

    protected ArrayList<String> parseResponse(JSONObject response)
    {
        return new ArrayList<String>(); // stub
    }

    public interface OnCompleted
    {
        void completed(ArrayList<String> translations, TranslationDirection direction);
    }

    class GetHttpTask implements Callable<String>
    {
        private final String url;

        public GetHttpTask(String url)
        {
            this.url = url;
            FixSSl();
        }

        @Override
        public String call()
        {
            return doExec(url);
        }
    }


    class GetHttpComplete implements TaskRunner.Callback<String>
    {
        TranslationDirection direction;
        public GetHttpComplete(TranslationDirection direction)
        {
            this.direction = direction;
        }
        @Override
        public void onComplete(String result)
        {
            try
            {
                if (result.isEmpty())
                {
                    ErrorDisplay.Show("An error was produced when translating. Check engine translation settings.");
                }
                else
                {
                    JSONObject response = new JSONObject(result);
                    onCompleted.completed(parseResponse(response), direction);
                }
            }
            catch (Exception e)
            {
                ErrorDisplay.Show(e);
            }
        }
    }


    private String doExec(String url)
    {
        try
        {
            if (url.isEmpty())
                throw new Exception("Translation provider did not work properly when composing the request. Maybe the text has rare characters?");
            CloseableHttpResponse response;
            CloseableHttpClient client = HttpClients.createDefault();
            HttpRequestBase request;
            if (requestType == RequestType.get)
                request = new HttpGet();
            else
                request = new HttpPost();
            request.setURI(new URI(url));
            response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();
                return responseString;
            }
            else
            {
                response.getEntity().getContent().close();
                throw new IOException(response.getStatusLine().getReasonPhrase());
            }
        }
        catch (Exception e)
        {
            ErrorDisplay.Show(e);
        }
        return "";
    }

    // Avoid TLS version problem with kitkat
    private void FixSSl()
    {
        SSLContext sslContext;
        try
        {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
        }
        catch (NoSuchAlgorithmException | KeyManagementException e)
        {
            ErrorDisplay.Show(e);
        }
    }

}
