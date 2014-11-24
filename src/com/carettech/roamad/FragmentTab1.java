package com.carettech.roamad;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.carettech.roamad.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

public class FragmentTab1 extends Fragment {
	
	
    private ArrayList<HashMap<String, String>> callList= null;
    LazyAdapter adapter;
    View rootView;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	// storing string resources into Array
    	rootView = (LinearLayout) inflater.inflate(R.layout.fragmenttab1, container, false);
    	
    	new ActivityList().execute();
    	
        return rootView;
      }
	
	
	
	/*
	 * AsyncTask for Fetch data from database or online
	 */
	public class ActivityList extends AsyncTask<Void, Void, Void> {
		public String flag;

		public ProgressDialog progressDialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			progressDialog.setMessage("Please wait ...");
			progressDialog.setIndeterminate(false);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
	    	SharedPreferences sharedpreferences = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
	        String ACCOUNT_SID, AUTH_TOKEN, CLIENT_NAME; 
	        ACCOUNT_SID = sharedpreferences.getString(getString(R.string.sid),"");
	        AUTH_TOKEN = sharedpreferences.getString(getString(R.string.auth_token),"");
	        CLIENT_NAME = sharedpreferences.getString(getString(R.string.clientname),"");
	    	HttpGet request = new HttpGet(getString(R.string.url_calls,ACCOUNT_SID));
			String basicAuth = "Basic " + Base64.encodeToString(String.format("%s:%s", ACCOUNT_SID, AUTH_TOKEN).getBytes(), Base64.NO_WRAP);
			request.setHeader("Authorization", basicAuth);
			request.setHeader("Content-Type",  "application/json");
			//String basicAuth = "Basic " + Base64.encodeToString("user:password".getBytes(), Base64.NO_WRAP);
			//request.setHeader("Authorization", basicAuth);
			//String[] adobe_products = new String[]{};
			callList = new ArrayList<HashMap<String, String>>();
			
			try{
				HttpResponse res = client.execute(request);
				if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String bufstring = EntityUtils.toString(res.getEntity(),"UTF-8");
					JSONObject IncomingData = new JSONObject(bufstring);
					JSONArray inArray = new JSONArray(IncomingData.getString("calls"));
					
					for (int i = 0; i < inArray.length(); i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						JSONObject SingleData = (JSONObject) inArray.get(i);
						map.put("from", SingleData.getString("from"));
						map.put("start_time", SingleData.getString("start_time"));
						map.put("duration", SingleData.getString("duration"));
						
						callList.add(map);
						//ar.add(((JSONObject)inArray.get(i)).getString("from"));
					}
					Log.d("TAG", bufstring);
					
					
				}	
			}catch (Exception e) {
	            Log.e("TAG", "Failed to obtain call list : " + e.getLocalizedMessage()+ e);
	        }
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
			

	        // updating UI from Background Thread
	        ((Activity) getActivity()).runOnUiThread(new Runnable() {
	            public void run() {
	         
	            	ListView lv = (ListView) rootView.findViewById(R.id.frament_list1);
	         		adapter=new LazyAdapter(getActivity(), callList);  
	                lv.setAdapter(adapter);
	            	
	            }
	        });
		}
		
	}

 
}