package com.pinplanet.pintact.utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.login.LoginActivity;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

// HTTPS

@SuppressWarnings("deprecation")
public class HttpConnection {


    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network
    // connection.
    public void access(Activity act, String path, String json, String op) {
        //test(act, json);
        if (customDialogMessage == -1)
            customDialogMessage = R.string.DIALOG_MESSAGE_PLEASE_WAIT;
        // Gets the URL from the UI's text field.
        String stringUrl = act.getResources().getString(R.string.pintact_server_url) + path;
//        String stringUrl = "https://staging-2046413205.us-east-1.elb.amazonaws.com" + path;
        //String stringUrl = Resources.getSystem().getString(R.string.pintact_server_url);
//        String stringUrl = "https://pintact.com";
        System.out.println("URLLLL:::" + stringUrl + json);
        ConnectivityManager connMgr = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl, json, op);
        } else {
            ((MyActivity) SingletonNetworkStatus.getInstance().getActivity()).myDialog(R.string.app_name, R.string.DIALOG_MESSAGE_NO_NETWORK);
            System.out.println("No network connection available.");
        }
    }

    public int customDialogMessage = R.string.DIALOG_MESSAGE_PLEASE_WAIT;

    public void access(Activity act, String path, String json, String op, int customDialogMessage) {
        this.customDialogMessage = customDialogMessage;
        access(act, path, json, op);

    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        private ProgressDialog dialog = null;

        @Override
        protected String doInBackground(String... urls) {

            // parameters comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0], urls[1], urls[2]);
            } catch (IOException e) {
                return e.getMessage();
                //return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPreExecute() {
            try {
                SingletonNetworkStatus singletonNetworkStatus = SingletonNetworkStatus.getInstance();
                Context context = singletonNetworkStatus.getActivity();
                if (context != null &&
                        !singletonNetworkStatus.getDoNotShowStatus() &&
                        singletonNetworkStatus.getWaitDialog() == null) {
                    dialog = new ProgressDialog(context);
                    dialog.setMessage(AppController.getInstance().getString(customDialogMessage));
                    dialog.show();


                    if (singletonNetworkStatus.getDoNotDismissDialog())
                        singletonNetworkStatus.setWaitDialog(dialog);
                }

                customDialogMessage = -1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                SingletonNetworkStatus singletonNetworkStatus = SingletonNetworkStatus.getInstance();
                Context context = singletonNetworkStatus.getActivity();

                if (dialog != null && dialog.isShowing() &&
                        !singletonNetworkStatus.getDoNotDismissDialog() && ((MyActivity) context).isActive()) {
                    dialog.dismiss();
                }

                if (!singletonNetworkStatus.getDoNotDismissDialog() &&
                        singletonNetworkStatus.getWaitDialog() != null) {
                    singletonNetworkStatus.getWaitDialog().dismiss();
                    singletonNetworkStatus.setWaitDialog(null);
                }
                if (context != null) {
                    if (singletonNetworkStatus.getCode() == 401) {
                        Intent it = new Intent(context, LoginActivity.class);
                        context.startActivity(it);
                    } else {//MyFragment
                        if (singletonNetworkStatus.getFragment() != null) {
                            ((MyFragment) singletonNetworkStatus.getFragment()).onPostNetwork();
                        } else {
                            if (((MyActivity) context).isActive()) {
                                ((MyActivity) context).onPostNetwork();
                            }
                        }
                    }
                    SingletonNetworkStatus.getInstance().setFragment(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String url, String json, String op) throws IOException {
            InputStream inputStream = null;
            String result = "";
            HttpResponse httpResponse = null;
            try {

                HttpClient httpclient = AppConnectionManager.getHttpClient();
                // 1. create HttpClient
                // HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                HttpGet httpGet = new HttpGet(url);

                // 3. build jsonObject

                // 4. convert JSONObject to JSON to String

                // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                // ObjectMapper mapper = new ObjectMapper();
                // json = mapper.writeValueAsString(person);

                // 5. set json to StringEntity
                ByteArrayEntity bae = new ByteArrayEntity(json.getBytes("UTF-8"));

                // 6. set httpPost Entity
                httpPost.setEntity(bae);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                httpPost.setHeader("Accept-Encoding", "gzip");
                httpPost.setHeader("XAPP_VERSION", UiControllerUtil.getVersionName());
                httpPost.setHeader("User-Agent", "Android " + Build.VERSION.CODENAME + " " + Build.VERSION.SDK_INT);

                httpGet.setHeader("Accept", "application/json");
                httpGet.setHeader("Content-type", "application/json");
                httpGet.setHeader("Accept-Encoding", "gzip");
                httpGet.setHeader("XAPP_VERSION", UiControllerUtil.getVersionName());
                httpGet.setHeader("User-Agent", "Android " + Build.VERSION.CODENAME + " " + Build.VERSION.SDK_INT);

                // 8. Execute POST request to the given URL

                httpResponse = op.equals("POST") ? httpclient.execute(httpPost) : httpclient.execute(httpGet);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();
                Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                System.out.println("Response Code:" + httpResponse.getStatusLine().getStatusCode());

                // 10. convert inputstream to string
                if (inputStream != null)
                    result = readIt(inputStream, 10240);
                else
                    result = "Did not work!";

                // 11. return result
                SingletonNetworkStatus.getInstance().setReady(true);
                SingletonNetworkStatus.getInstance().setJson(result);
                SingletonNetworkStatus.getInstance().setCode(httpResponse.getStatusLine().getStatusCode());
                SingletonNetworkStatus.getInstance().setMsg(httpResponse.getStatusLine().getReasonPhrase());

            } catch (Exception e) {
                System.out.println("InputStream:::" + e.getLocalizedMessage());
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

            }

            return result;
        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException,
                UnsupportedEncodingException {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(stream, "UTF-8"), 10240);
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    //System.out.println(line); // print out response message
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return sb.toString();
        }
    }
}
