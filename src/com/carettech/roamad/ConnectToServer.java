package com.carettech.roamad;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.carettech.roamad.R;

import android.content.Context;
import android.util.Log;

/*
 * class to connect with server
 */
public class ConnectToServer {
	
	
	private Context context;
	

	public ConnectToServer(Context ctx) {
		context = ctx;
	}

	public  String serverPostConnect(String data, String url) {
		String bufstring = "";
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost(url);
			
			request.setHeader("Authorization", context.getString(R.string.authorization));
			request.setHeader("Content-Type", "application/json");
			StringEntity se = new StringEntity(data); 
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(se);
			HttpResponse res = client.execute(request);
			if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK || res.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				bufstring = EntityUtils.toString(res.getEntity(),"UTF-8");
			}else{
				bufstring = String.valueOf(res.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			Log.i("localhost", "Exp=" + e);
			bufstring = "error";
		}
		return bufstring;
	}

	public  String serverGetConnect(String url, HttpParams params) {
		String bufstring = "";
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			//HttpParams params = new BasicHttpParams();
			//params.setParameter("Authorization", context.getString(R.string.authorization));
			if(params != null){
			request.setParams(params);
			}
			request.setHeader("Authorization",  context.getString(R.string.authorization));
			request.setHeader("Content-Type",  "application/json");
			//String basicAuth = "Basic " + Base64.encodeToString("user:password".getBytes(), Base64.NO_WRAP);
			//request.setHeader("Authorization", basicAuth);
			
			HttpResponse res = client.execute(request);
			if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				bufstring = EntityUtils.toString(res.getEntity(),"UTF-8");
			}else{
				bufstring = String.valueOf(res.getStatusLine().getStatusCode());
			}
        
		} catch (Exception e) {
			Log.i("localhost", "Exp=" + e);
			bufstring = "error";
		}
		Log.d("bufstring", "Exp=" + bufstring);
		return bufstring;
	}


}
