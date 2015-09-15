package com.pinplanet.pintact.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

public class RestServiceAsync extends AsyncTask<String, Void, Void>{

  private PostServiceExecuteTask postExecuteTask;
  private boolean showLoading;
  private ProgressDialog dialog;
  private Context context;
  private int statusCode;

  public RestServiceAsync(PostServiceExecuteTask postExecuteTask, Context context, boolean showLoading){
    this.postExecuteTask = postExecuteTask;
    this.showLoading = showLoading;
    this.context = context;
  }

  public RestServiceAsync(PostServiceExecuteTask postExecuteTask){
    this(postExecuteTask, null, false);
  }

  @Override
  protected void onPreExecute() {
      try {
          if (showLoading) {
              dialog = new ProgressDialog(context);
              dialog.setMessage(AppController.getInstance().getString(R.string.DIALOG_MESSAGE_PLEASE_WAIT));
              dialog.show();
          }
      }catch (Exception e)
      {
          e.printStackTrace();
      }
  }

  @Override
  protected void onPostExecute(Void result) {
      try {
          if (showLoading) {
              if (dialog != null && dialog.isShowing()) {
                  dialog.dismiss();
              }
          }
          if (statusCode == 401 && context != null) {
              Intent it = new Intent(context, LoginActivity.class);
              context.startActivity(it);
          }
      }catch (Exception e)
      {
          e.printStackTrace();
      }
  }

  @Override
  protected Void doInBackground(String... urls) {
    try {
      String url = AppController.getInstance().getResources().getString(R.string.pintact_server_url) + urls[0];
      String json = urls[1];
      String op = urls[2];
      InputStream inputStream = null;
      String result = "";
      HttpResponse httpResponse = null;
      try {
        System.out.println("Request  Url:" + url);

        HttpClient httpclient = AppConnectionManager.getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        HttpGet httpGet = new HttpGet(url);

        ByteArrayEntity bae = new ByteArrayEntity(json.getBytes("UTF-8"));

        httpPost.setEntity(bae);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Accept-Encoding", "gzip");
        httpPost.setHeader("XAPP_VERSION", UiControllerUtil.getVersionName());
          httpPost.setHeader("User-Agent", "Android "+Build.VERSION.CODENAME+" "+Build.VERSION.SDK_INT);

        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/json");
        httpGet.setHeader("Accept-Encoding", "gzip");
        httpGet.setHeader("XAPP_VERSION", UiControllerUtil.getVersionName());
          httpGet.setHeader("User-Agent", "Android "+ Build.VERSION.CODENAME+" "+Build.VERSION.SDK_INT);

        httpResponse = op.equals("POST") ? httpclient.execute(httpPost) : httpclient.execute(httpGet);
        inputStream = httpResponse.getEntity().getContent();
        Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
          inputStream = new GZIPInputStream(inputStream);
        }
        //System.out.println("Response Code:" + httpResponse.getStatusLine().getStatusCode());

        // 10. convert inputstream to string
        if (inputStream != null)
          result = readIt(inputStream, 10240);
        else
          result = "Did not work!";
        statusCode = httpResponse.getStatusLine().getStatusCode();
        if(postExecuteTask != null) {

          postExecuteTask.run(httpResponse.getStatusLine().getStatusCode(), result);
        }

      } catch (Exception e) {
        System.out.println("InputStream:::" + e.getLocalizedMessage());
      } finally {
        if(inputStream != null)
          inputStream.close();
      }
    }catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
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
    }
    catch (IOException e) { e.printStackTrace(); }
    catch (Exception e) { e.printStackTrace(); }



    return sb.toString();
  }
}
