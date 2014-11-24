package com.carettech.roamad;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.carettech.roamad.R;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Type;
import android.text.InputType;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class SignupActivity extends Activity {
	private static final String TAG = "SignupActivity";
	private Spinner mySpinner;
	private String[] countries_list = new  String[]{"+91","+44","+43"};
	private EditText phone;
	ConnectToServer connectToServer;
	String countryCode, pNumber, verifyCode;
	SharedPreferences sharedpreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		mySpinner = (Spinner)findViewById(R.id.countries_list);
		phone = (EditText)findViewById(R.id.phone_number);
		connectToServer = new ConnectToServer(this);
		sharedpreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		
	}
	
	public void signUp(View vs){
		int position = mySpinner.getSelectedItemPosition();
		countryCode = countries_list[position];
		pNumber = phone.getText().toString();
		if(pNumber.equals("")){
			Toast.makeText(this, "Please enter phone number", Toast.LENGTH_LONG).show();
		}else{
			vs.setEnabled(false);
			if (isOnline()) {
				new RegisterTask().execute();
			}else{
				alertboxNeutral("Warning", "Please check your internet connection or try again later", "ok","");
			}
		}
	}

	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	/*
	 * AsyncTask for Fetch data from database or online
	 */
	public class RegisterTask extends AsyncTask<Void, Void, Void> {
		public Boolean flag;

		public ProgressDialog progressDialog = new ProgressDialog(
				SignupActivity.this);

		@Override
		protected void onPreExecute() {
			progressDialog.setMessage("Please wait ...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			flag = sendMessage();
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void unused) {
			progressDialog.dismiss();
			if (flag) {
				verifyAlert();
			}else {
				alertboxNeutral("Warning", "Sending SMS Failed", "Ok","Try again");
				
			}

		}
	}
	
	/*
	 * AsyncTask for Fetch data from database or online
	 */
	public class RegisterUpdateTask extends AsyncTask<Void, Void, Void> {
		public Boolean flag;

		public ProgressDialog loginDialog = new ProgressDialog(
				SignupActivity.this);

		@Override
		protected void onPreExecute() {
			loginDialog.setMessage("Please wait ...");
			loginDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			flag = getDetails();
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void unused) {
			loginDialog.dismiss();
			if (flag) {
				Intent myIntent1 = new Intent(SignupActivity.this, TabsActivity.class);
        		startActivity(myIntent1);
        		finish();
			}else {
				alertboxNeutral("Warning", "Try again! ", "ok","");
				
			}

		}
	}
	
	AlertDialog alertDialogMain	= null;
	private void verifyAlert(){
		// Creating alert Dialog with one Button
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignupActivity.this);
       
       

        // Setting Dialog Title
        alertDialog.setTitle("VERIFY ACCOUNT");

        // Setting Dialog Message
        alertDialog.setMessage("Enter code");
        final LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(SignupActivity.this);
        TextView noteView = new TextView(SignupActivity.this);
        noteView.setText("Resend code");
        noteView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (alertDialogMain != null){
					alertDialogMain.dismiss();
				}
				new RegisterTask().execute();
				
			}
		});
        
        
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
          input.setLayoutParams(lp);
          input.setInputType(InputType.TYPE_CLASS_PHONE);
          noteView.setLayoutParams(lp);
          //noteView.setClickable(true);
          noteView.setAutoLinkMask(Linkify.WEB_URLS);
          
          ll.addView(input);
          ll.addView(noteView);
          alertDialog.setView(ll);
          


        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.abc_ic_go);

        // Setting Positive "Yes" Button
        alertDialog.setNegativeButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        // Write your code here to execute after dialog

                        String password = input.getText().toString();
                       	if(verifyCode.equals(password))
                        	{
                        		Toast.makeText(getApplicationContext(),"Code Matched", Toast.LENGTH_SHORT).show();
                        		new RegisterUpdateTask().execute();
                        		
                        	}else
                        	{
                        		dialog.cancel();
                        		verifyAlert();
                        		Toast.makeText(getApplicationContext(),"Wrong code!", Toast.LENGTH_SHORT).show();
                        	}
                     }
                });
		        // Setting Negative "NO" Button
		        alertDialog.setPositiveButton("Cancel",
		                new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {
		                        // Write your code here to execute after dialog
		                        dialog.cancel();
		                        finish();
		                    }
		                });

        // closed

        // Showing Alert Message
       alertDialogMain = alertDialog.show();


	}
	
	/*
	 * alert for if there any network connection error or if given username and password not mapped
	 */
	private void alertboxNeutral(String title, String message, String positive,
            String neutral) {
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(
                SignupActivity.this);
        alertbox.setTitle(title);
        alertbox.setMessage(message);
        alertbox.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    alertbox.setCancelable(true);
                    }
                });
        alertbox.setNeutralButton(neutral,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new RegisterTask().execute();
                    }
                });

        alertbox.show();
    }

	/*
	 * check whether the user info is present in databse or in server
	 */
	public boolean sendMessage() {
		Boolean isJsonDataExist= false;
		
		try {
			Random rnd = new Random();
			int n = 1000 + rnd.nextInt(8999);
			verifyCode = ""+n;
			JSONObject json = new JSONObject();
			json.put(getString(R.string.country), countryCode);
			json.put(getString(R.string.user_number),pNumber);
			json.put("code", verifyCode);
			Log.d("Tag",json.toString());
			isJsonDataExist = true;
			String data = connectToServer.serverPostConnect(json.toString(), getString(R.string.url_verify));
			JSONObject jData = new JSONObject(data);
			if (data != null) {
			 	if(jData.getString("status").equals("OK"))
			 	{
			 		isJsonDataExist = true;
			 	}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isJsonDataExist;

	}
	
	/*
	 * check whether the user info is present in databse or in server
	 */
	public boolean getDetails() {
		Boolean isJsonDataExist= false;
		try {
			JSONObject json = new JSONObject();
			json.put(getString(R.string.country), countryCode);
			json.put(getString(R.string.user_number),pNumber);
			Log.d("Tag",json.toString());
			isJsonDataExist = true;
			String data = connectToServer.serverPostConnect(json.toString(), getString(R.string.url_register));
			JSONObject jData = new JSONObject(data);
			if (data != null) {
			 	if(jData.getString("status").equals("active"))
			 	{
			 		String ACCOUNT_SID = jData.getString("sid");
					String AUTH_TOKEN = jData.getString("auth_token");
			 		Editor editor = sharedpreferences.edit();
			 		editor.putString(getString(R.string.country), countryCode);
			 		editor.putString(getString(R.string.user_number), pNumber);
			 		editor.putString(getString(R.string.clientname), countryCode+pNumber);
			 		editor.putString(getString(R.string.sid), ACCOUNT_SID);
			 		editor.putString(getString(R.string.auth_token), AUTH_TOKEN);
			 		editor.commit();
			 		isJsonDataExist = true;
			 		String bufstring;
			 		Log.d("localhost", "Exp=0");
					try {
						HttpClient client = new DefaultHttpClient();
						HttpGet request = new HttpGet(getString(R.string.url_incomingphonenumbers,ACCOUNT_SID));
						String basicAuth = "Basic " + Base64.encodeToString(String.format("%s:%s", ACCOUNT_SID, AUTH_TOKEN).getBytes(), Base64.NO_WRAP);
						request.setHeader("Authorization", basicAuth);
						HttpResponse res = client.execute(request);
						bufstring = EntityUtils.toString(res.getEntity(),"UTF-8");
						JSONObject IncomingData = new JSONObject(bufstring);
						Log.d("IncomingData", "Exp=" + IncomingData);	
						if(IncomingData.has("incoming_phone_numbers") && IncomingData.getInt("total") > 0) {
						    //it has it, do appropriate processing
							JSONArray inArray = new JSONArray(IncomingData.getString("incoming_phone_numbers"));
							JSONObject singleInfo = (JSONObject) inArray.get(0);
							editor.putString(getString(R.string.incoming_phone_number), singleInfo.getString("phone_number"));
							editor.putString(getString(R.string.phone_sid), singleInfo.getString("sid"));
							boolean s = false;
							if (singleInfo.getString("voice_url") == singleInfo.getString("voice_fallback_url")){
								s = true;
							}
							
							editor.putBoolean("voice_status", s);
					 		editor.commit();
						}else{
							Log.d(TAG,"No incomingphonenumbers");
							HttpGet request2 = new HttpGet(getString(R.string.url_availablephonenumbers,ACCOUNT_SID,"GB"));
							request2.setHeader("Authorization", basicAuth);
							HttpResponse res2 = client.execute(request2);
							bufstring = EntityUtils.toString(res2.getEntity(),"UTF-8");
							Log.d(TAG,"available incomingphonenumbers");
							Log.d(TAG,bufstring);
							JSONObject PhoneData = new JSONObject(bufstring);
							JSONArray inArray = new JSONArray(PhoneData.getString("available_phone_numbers"));
							Log.d(TAG, "selected available_phone_numbers="+ inArray.get(0)); //+ inArray.get(0));	
							HttpPost request3 = new HttpPost(getString(R.string.url_incomingphonenumbers,ACCOUNT_SID));
							List<NameValuePair> value = new ArrayList<NameValuePair>();
							value.add(new BasicNameValuePair("PhoneNumber", ((JSONObject)inArray.get(0)).getString("phone_number")));
							value.add(new BasicNameValuePair("VoiceUrl", getString(R.string.url_voice,ACCOUNT_SID)));
							value.add(new BasicNameValuePair("VoiceMethod", "GET"));
							value.add(new BasicNameValuePair("VoiceFallbackUrl", getString(R.string.url_voicefallback,ACCOUNT_SID)));
							value.add(new BasicNameValuePair("VoiceFallbackMethod", "GET"));
							Log.d(TAG, "posted data="+ value.toString());
							UrlEncodedFormEntity entity = new UrlEncodedFormEntity(value,"UTF-8");
							request3.setEntity(entity);
							request3.setHeader("Authorization", basicAuth);
							//request3.addHeader("Content-Type", "application/x-www-form-urlencoded");
							request3.setHeader("Content-Type",
				                    "application/x-www-form-urlencoded;charset=UTF-8");
							HttpResponse res3 = client.execute(request3);
							bufstring = EntityUtils.toString(res3.getEntity(),"UTF-8");
							JSONObject singleInfo = new JSONObject(bufstring);
							Log.d(TAG,"Post Result");
							Log.d(TAG,bufstring);
							if (singleInfo.has("sid")){
							
							//JSONArray singleinArray = new JSONArray(IncomingDataPOST.getString("incoming_phone_numbers"));
							//JSONObject singleInfo = (JSONObject) singleinArray.get(0);
							editor.putString(getString(R.string.incoming_phone_number), singleInfo.getString("phone_number"));
							editor.putString(getString(R.string.phone_sid), singleInfo.getString("sid"));
							boolean s = false;
							if (singleInfo.getString("voice_url") == singleInfo.getString("voice_fallback_url")){
								s = true;
							}
							
							editor.putBoolean("voice_status", s);
					 		editor.commit();
							editor.putString(getString(R.string.incoming_phone_number), ((JSONObject)inArray.get(0)).getString("phone_number"));
							
					 		editor.commit();
							}else{
								isJsonDataExist= false;
							}
							//available_phone_numbers
						}
						/*
						if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK||res.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
							bufstring = EntityUtils.toString(res.getEntity(),"UTF-8");
							JSONObject IncomingData = new JSONObject(bufstring);
							Log.d("localhost", "Exp=" + IncomingData);
							Log.d("localhost", "Exp=1.5.1");
						}else{
							Log.d("localhost", "Exp=1.5.2");
							bufstring = EntityUtils.toString(res.getEntity(),"UTF-8");
							//bufstring = String.valueOf(res.getStatusLine().getStatusCode());
							Log.d("localhost", "Exp=" + bufstring);
						}*/
						Log.d("localhost", "Exp=1.6");
					} catch (Exception e) {
						Log.i("localhost", "Exp=" + e);
						bufstring = "error";
					}
					Log.d("localhost", "Exp=1");
			 	}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isJsonDataExist;

	}
	
}
