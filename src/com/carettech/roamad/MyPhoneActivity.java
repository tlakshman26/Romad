package com.carettech.roamad;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.carettech.roamad.R;
import com.twilio.client.Connection;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;
import com.twilio.client.Twilio;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyPhoneActivity implements Twilio.InitListener, DeviceListener 
{
    private static final String TAG = "MonkeyPhone";
    public static MyPhoneActivity instance = null;
    private Device device;
    
    private Connection connection;
    private Context context;

    public MyPhoneActivity(Context context)
    {
    	this.context = context;
        Twilio.initialize(context, this /* Twilio.InitListener */);
    }

        
    /* Twilio.InitListener method */
    @Override
    public void onInitialized()
    {
        Log.d(TAG, "Twilio SDK is ready");

        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(context.getString(R.string.url_capability));
            SharedPreferences sharedpreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String SID, AUTH_TOKEN, CLIENT_NAME; 
            SID = sharedpreferences.getString(context.getString(R.string.sid),"");
            AUTH_TOKEN = sharedpreferences.getString(context.getString(R.string.auth_token),"");
            CLIENT_NAME = sharedpreferences.getString(context.getString(R.string.clientname),"");
            request.setHeader("Authorization", context.getString(R.string.authorization));
            request.setHeader("Content-Type", "application/json");
            JSONObject json = new JSONObject();
			json.put("clientName", CLIENT_NAME);
			json.put("auth_token",AUTH_TOKEN);
			json.put("sid", SID);
			Log.d("Tag",json.toString());
			StringEntity se = new StringEntity(json.toString()); 
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(se);
			HttpResponse res3 = client.execute(request);
			String bufstring = EntityUtils.toString(res3.getEntity(),"UTF-8");
			JSONObject IncomingDataPOST = new JSONObject(bufstring);
			String capabilityToken = IncomingDataPOST.getString("token");
			Log.d(TAG, capabilityToken);
			
            //String capabilityToken = HttpHelper.httpGet("http://companyfoo.com/auth.php");
            device = Twilio.createDevice(capabilityToken, this);
            Intent intent = new Intent(context, IncomingCallActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            device.setIncomingIntent(pendingIntent);
            device.setIncomingSoundEnabled(true);
           // device.set
        } catch (Exception e) {
            Log.e(TAG, "Failed to obtain capability token: " + e.getLocalizedMessage()+ e);
        }
    }

    
    public void handleIncomingConnection(Device inDevice, Connection inConnection)
    {
        Log.i(TAG, "Device received incoming connection");
        if (connection != null){
            connection.disconnect();
        }
        connection = inConnection;
        connection.accept();
        //connection.setConnectionListener(arg0)
    }
    
    
    public void handleDeclieConnection()
    {
        Log.i(TAG, "Device received incoming connection");
        if (connection != null)
            connection.disconnect();
        connection = null;
        //connection.accept();
    }
    
    /* Twilio.InitListener method */
    public void onError(Exception e)
    {
        Log.e(TAG, "Twilio SDK couldn't start: " + e.getLocalizedMessage());
    }

    protected void finalize()
    {
        if (device != null)
            device.release();
    }
    
    /* ... other methods ... */
    
    public void onStartListening(Device inDevice)
    {
        Log.i(TAG, "Device is now listening for incoming connections");
        Log.i(TAG, "Device is now listening for incoming connections");
        Toast.makeText(context, "text", Toast.LENGTH_LONG).show();
    }
 
    public void onStopListening(Device inDevice)
    {
        Log.i(TAG, "Device is no longer listening for incoming connections");
    }
 
    public void onStopListening(Device inDevice, int inErrorCode, String inErrorMessage)
    {
        Log.i(TAG, "Device is no longer listening for incoming connections due to error " +
                   inErrorCode + ": " + inErrorMessage);
    }
 
    public boolean receivePresenceEvents(Device inDevice)
    {
        return false;  // indicate we don't care about presence events
    }
 
    public void onPresenceChanged(Device inDevice, PresenceEvent inPresenceEvent) { }

/*	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}*/
 
    /* ... other methods ... */
}
