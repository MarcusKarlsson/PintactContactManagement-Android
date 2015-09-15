package com.pinplanet.pintact.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.pinplanet.pintact.R;

public class HttpConnectionForImage  {
	
	// When user clicks button, calls AsyncTask.
	// Before attempting to fetch the URL, makes sure that there is a network
	// connection.
	public void access(Activity act, String path, String json, String op, String file) {
		
		//test(act, json);
		
		// Gets the URL from the UI's text field.
		String stringUrl = act.getResources().getString(R.string.pintact_server_url) + path;
		System.out.println("URLLLL:::" + stringUrl + json);
		ConnectivityManager connMgr = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new DownloadWebpageTask().execute(stringUrl, json, op, file);
		} else {
			((MyActivity)SingletonNetworkStatus.getInstance().getActivity()).myDialog("Pintact", "No network connection available");
			System.out.println("No network connection available.");
		}
	}

	// Uses AsyncTask to create a task away from the main UI thread. This task
	// takes a
	// URL string and uses it to create an HttpUrlConnection. Once the
	// connection
	// has been established, the AsyncTask downloads the contents of the webpage
	// as
	// an InputStream. Finally, the InputStream is converted into a string,
	// which is
	// displayed in the UI by the AsyncTask's onPostExecute method.
	private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
		private ProgressDialog dialog = null;
		
		@Override
		protected String doInBackground(String... urls) {

			// parameters comes from the execute() call: params[0] is the url.
			try {
				return downloadUrl(urls[0], urls[1], urls[2], urls[3]);
			} catch (IOException e) {
				return e.getMessage();
				//return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		@Override
		protected void onPreExecute() {
			
			if ( SingletonNetworkStatus.getInstance().getActivity() != null &&
				!SingletonNetworkStatus.getInstance().getDoNotShowStatus() &&
				SingletonNetworkStatus.getInstance().getWaitDialog() == null) 
			{
				dialog = new ProgressDialog(SingletonNetworkStatus.getInstance().getActivity());
				dialog.setMessage("Please wait...");
				dialog.show();
				
				if (SingletonNetworkStatus.getInstance().getDoNotDismissDialog())
					SingletonNetworkStatus.getInstance().setWaitDialog(dialog);
			}
		}
		
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
	        if (dialog != null && dialog.isShowing() &&
	        	!SingletonNetworkStatus.getInstance().getDoNotDismissDialog()) 
	        {
	            dialog.dismiss();
	        } 
	        
	        if (!SingletonNetworkStatus.getInstance().getDoNotDismissDialog() &&
	        	SingletonNetworkStatus.getInstance().getWaitDialog() != null) 
	        {
	        	SingletonNetworkStatus.getInstance().getWaitDialog().dismiss();
	        	SingletonNetworkStatus.getInstance().setWaitDialog(null);
	        }
	        
	        if (SingletonNetworkStatus.getInstance().getActivity() != null) {
		        ((MyActivity)SingletonNetworkStatus.getInstance().getActivity()).onPostNetwork();
	        }
		}
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private String downloadUrl(String url, String json, String op, String filePath) throws IOException {
        InputStream inputStream = null;
        String result = "";
        try {

          HttpClient httpclient = AppConnectionManager.getHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);
            HttpGet httpGet = new HttpGet(url);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        	final File file = new File(filePath);
          builder.addBinaryBody("file", file, ContentType.create("image/jpeg"), file.getName());

            // 6. set httpPost Entity
            httpPost.setEntity(builder.build());
            
            // 7. Set some headers to inform server about the type of the content   
            //httpPost.setHeader("Accept", "application/json");
            //httpPost.setHeader("Content-type", "image/jpeg");
            //httpPost.setHeader("accept-encoding", "gzip, deflate");
            
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("XAPP_VERSION", UiControllerUtil.getVersionName());
 
            // 8. Execute POST request to the given URL
            
            HttpResponse httpResponse = op.equals("POST") ? httpclient.execute(httpPost) : httpclient.execute(httpGet);
 
            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            System.out.println("Response Code:" + httpResponse.getStatusLine().getStatusCode());
            
            System.out.println("deleting file : " + filePath);
           // boolean ok = file.delete();
            //System.out.println("deleting file " + (ok ? " OK! " : " Failed!"));
            
            // 10. convert inputstream to string
            if(inputStream != null)
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
        }finally {
            if(inputStream != null) {
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
		           new BufferedReader(new InputStreamReader(stream, "UTF-8"), 65728);
		    String line = null;

		    while ((line = reader.readLine()) != null) {
		        sb.append(line);
		        System.out.println(line);
		    }
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }


		
		return sb.toString();
	}
}
