package com.challenge.quotes.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class RestAgent {
    private final static String DEBUG_TAG = "[RestAgent]";

    protected static final String UTF8 = "UTF-8";
    public static String GET = "GET";
    public static String POST = "POST";
    public static String PUT = "PUT";
    public static String DELETE = "DELETE";
    public String mRelativeUrl;
    public String mMethod;
    public List<Parameter> mParams;

    public RestAgent(String relativeUrl, String method, List<Parameter> params) {
        this.mRelativeUrl = relativeUrl;
        this.mMethod = method;
        this.mParams = params;
    }

    public String send() throws IOException {
        String strUrl = Const.BASE_URL + mRelativeUrl;
        if (GET.equals(mMethod) && mParams != null)
        {
            strUrl += "?" + joinParameters(mParams, true);
        }
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod(mMethod);

        if (mParams != null && (POST.equals(mMethod) || PUT.equals(mMethod)))
        {
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF8));
            writer.write(joinParameters(mParams, false));
            writer.flush();
            writer.close();
            os.close();
        }
        conn.connect();

        String result = null;
        InputStream is = null;
        StringBuffer sb = new StringBuffer();
        try {
            is = new BufferedInputStream(conn.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            int i = 0;
            while ((inputLine = br.readLine()) != null && i < 100) {
                sb.append(inputLine + "\n");
                i++;
            }
            result = sb.toString();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.v(DEBUG_TAG, "Error reading InputStream");
            result = null;
        }
        finally {
            if (is != null) {
                is.close();
            }
        }

        return result;
    }

    public static class Parameter {
        private String name;
        private String value;

        public Parameter(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s=%s", name, value);
        }

        public String toEncodedString() throws UnsupportedEncodingException {
            return String.format("%s=%s", URLEncoder.encode(name, UTF8), URLEncoder.encode(value, UTF8));
        }
    }

    public String joinParameters(List<Parameter> params, boolean encoded) throws UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        for (Parameter param : params)
        {
            sb.append(encoded ? param.toEncodedString() : param.toString());
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}