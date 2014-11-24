/*
 *  Copyright (c) 2011 by Twilio, Inc., all rights reserved.
 *
 *  Use of this software is subject to the terms and conditions of 
 *  the Twilio Terms of Service located at http://www.twilio.com/legal/tos
 */

package com.carettech.roamad;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

import com.twilio.client.Connection;
import com.twilio.client.ConnectionListener;
import com.twilio.client.Device;
import com.twilio.client.Device.Capability;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;
import com.twilio.client.Twilio;

public class BasicPhone implements DeviceListener,
                                   ConnectionListener
{
    private static final String TAG = "BasicPhone";

    public interface LoginListener
    {
        public void onLoginStarted();
        public void onLoginFinished();
        public void onLoginError(Exception error);
    }

    public interface BasicConnectionListener
    {
        public void onIncomingConnectionDisconnected();
        public void onConnectionConnecting();
        public void onConnectionConnected();
        public void onConnectionFailedConnecting(Exception error);
        public void onConnectionDisconnecting();
        public void onConnectionDisconnected();
        public void onConnectionFailed(Exception error);
    }

    public interface BasicDeviceListener
    {
        public void onDeviceStartedListening();
        public void onDeviceStoppedListening(Exception error);
    }

    private static BasicPhone instance;
    public static final BasicPhone getInstance(Context context)
    {
        if (instance == null)
            instance = new BasicPhone(context);
        return instance;
    }

    private final Context context;
    private LoginListener loginListener;
    private BasicConnectionListener basicConnectionListener;
    private BasicDeviceListener basicDeviceListener;

    private static boolean twilioSdkInited;
    private static boolean twilioSdkInitInProgress;
    private boolean queuedConnect;

    private Device device;
    private Connection pendingIncomingConnection;
    private Connection connection;
    private boolean speakerEnabled;

    private BasicPhone(Context context)
    {
        this.context = context;
    }

    public void setListeners(LoginListener loginListener,
                             BasicConnectionListener basicConnectionListener,
                             BasicDeviceListener basicDeviceListener)
    {
        this.loginListener = loginListener;
        this.basicConnectionListener = basicConnectionListener;
        this.basicDeviceListener = basicDeviceListener;
    }

    private String getCapabilityToken() throws Exception
    {
        // This runs synchronously for simplicity!  In a real application you'd
        // want to do network I/O on a separate thread to avoid ANRs.
       // return HttpHelper.httpGet(AUTH_PHP_SCRIPT);
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
		return capabilityToken;
    }

    private boolean isCapabilityTokenValid()
    {
        if (device == null || device.getCapabilities() == null)
            return false;
        long expTime = (Long)device.getCapabilities().get(Capability.EXPIRATION);
        return expTime - System.currentTimeMillis() / 1000 > 0;
    }

    private void updateAudioRoute()
    {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(speakerEnabled);
    }

    public void login()
    {
        if (loginListener != null)
            loginListener.onLoginStarted();

        if (!twilioSdkInited) {
            if (twilioSdkInitInProgress)
                return;

            twilioSdkInitInProgress = true;

            Twilio.initialize(context, new Twilio.InitListener()
            {
                @Override
                public void onInitialized()
                {
                    twilioSdkInited = true;
                    twilioSdkInitInProgress = false;
                    reallyLogin();
                }

                @Override
                public void onError(Exception error)
                {
                    twilioSdkInitInProgress = false;
                    if (loginListener != null)
                        loginListener.onLoginError(error);
                }
            });
        } else
            reallyLogin();
    }

    private void reallyLogin()
    {
        try {
            String capabilityToken = getCapabilityToken();
            if (device == null) {
                device = Twilio.createDevice(capabilityToken, this);
                Intent intent = new Intent(context, IncomingCallActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                device.setIncomingIntent(pendingIntent);
            } else
                device.updateCapabilityToken(capabilityToken);

            if (loginListener != null)
                loginListener.onLoginFinished();

            if (queuedConnect) {
                // If someone called connect() before we finished initializing
                // the SDK, let's take care of that here.
                connect();
                queuedConnect = false;
            }
        } catch (Exception e) {
            if (device != null)
                device.release();
            device = null;

            if (loginListener != null)
                loginListener.onLoginError(e);
        }
    }

    public void setSpeakerEnabled(boolean speakerEnabled)
    {
        if (speakerEnabled != this.speakerEnabled) {
            this.speakerEnabled = speakerEnabled;
            updateAudioRoute();
        }
    }

    public void connect()
    {
        if (twilioSdkInitInProgress) {
            // If someone calls connect() before the SDK is initialized, we'll remember
            // that fact and try to connect later.
            queuedConnect = true;
            return;
        }

        if (!isCapabilityTokenValid())
            login();

        if (device == null)
            return;

        if (canMakeOutgoing()) {
            disconnect();

            connection = device.connect(null, this);
            if (connection == null && basicConnectionListener != null)
                basicConnectionListener.onConnectionFailedConnecting(new Exception("Couldn't create new connection"));
        }
    }

    public void disconnect()
    {
        if (connection != null) {
            connection.disconnect();  // will null out in onDisconnected()
            if (basicConnectionListener != null)
                basicConnectionListener.onConnectionDisconnecting();
        }
    }

    public void acceptConnection()
    {
        if (pendingIncomingConnection != null) {
            if (connection != null)
                disconnect();

            pendingIncomingConnection.accept();
            connection = pendingIncomingConnection;
            pendingIncomingConnection = null;
        }
    }

    public void ignoreIncomingConnection()
    {
        if (pendingIncomingConnection != null) {
            pendingIncomingConnection.ignore();
        }
    }

    public boolean isConnected()
    {
        return connection != null && connection.getState() == Connection.State.CONNECTED;
    }

    public Connection.State getConnectionState()
    {
        return connection != null ? connection.getState() : Connection.State.DISCONNECTED;
    }

    public boolean hasPendingConnection()
    {
        return pendingIncomingConnection != null;
    }

    public boolean handleIncomingIntent(Intent intent)
    {
        Device inDevice = intent.getParcelableExtra(Device.EXTRA_DEVICE);
        Connection inConnection = intent.getParcelableExtra(Device.EXTRA_CONNECTION);
        if (inDevice == null && inConnection == null)
            return false;

        intent.removeExtra(Device.EXTRA_DEVICE);
        intent.removeExtra(Device.EXTRA_CONNECTION);

        if (pendingIncomingConnection != null) {
            Log.i(TAG, "A pending connection already exists");
            inConnection.ignore();
            return false;
        }

        pendingIncomingConnection = inConnection;
        pendingIncomingConnection.setConnectionListener(this);

        return true;
    }

    public boolean canMakeOutgoing()
    {
        if (device == null)
            return false;

        Map<Capability, Object> caps = device.getCapabilities();
        return caps.containsKey(Capability.OUTGOING) && (Boolean)caps.get(Capability.OUTGOING);
    }

    public boolean canAcceptIncoming()
    {
        if (device == null)
            return false;

        Map<Capability, Object> caps = device.getCapabilities();
        return caps.containsKey(Capability.INCOMING) && (Boolean)caps.get(Capability.INCOMING);
    }

    @Override  /* DeviceListener */
    public void onStartListening(Device inDevice)
    {
        if (basicDeviceListener != null)
            basicDeviceListener.onDeviceStartedListening();
    }

    @Override  /* DeviceListener */
    public void onStopListening(Device inDevice)
    {
        if (basicDeviceListener != null)
            basicDeviceListener.onDeviceStoppedListening(null);
    }

    @Override  /* DeviceListener */
    public void onStopListening(Device inDevice, int inErrorCode, String inErrorMessage)
    {
        if (basicDeviceListener != null)
            basicDeviceListener.onDeviceStoppedListening(new Exception(inErrorMessage));
    }

    @Override  /* DeviceListener */
    public boolean receivePresenceEvents(Device inDevice)
    {
        return false;
    }

    @Override  /* DeviceListener */
    public void onPresenceChanged(Device inDevice, PresenceEvent inPresenceEvent) { }

    @Override  /* ConnectionListener */
    public void onConnecting(Connection inConnection)
    {
        if (basicConnectionListener != null)
            basicConnectionListener.onConnectionConnecting();
    }

    @Override  /* ConnectionListener */
    public void onConnected(Connection inConnection)
    {
        updateAudioRoute();
        if (basicConnectionListener != null)
            basicConnectionListener.onConnectionConnected();
    }

    @Override  /* ConnectionListener */
    public void onDisconnected(Connection inConnection)
    {
        if (inConnection == connection) {
            connection = null;
            if (basicConnectionListener != null)
                basicConnectionListener.onConnectionDisconnected();
        } else if (inConnection == pendingIncomingConnection) {
            pendingIncomingConnection = null;
            if (basicConnectionListener != null)
                basicConnectionListener.onIncomingConnectionDisconnected();
        }
    }

    @Override  /* ConnectionListener */
    public void onDisconnected(Connection inConnection, int inErrorCode, String inErrorMessage)
    {
        if (inConnection == connection) {
            connection = null;
            if (basicConnectionListener != null)
                basicConnectionListener.onConnectionFailedConnecting(new Exception(inErrorMessage));
        }
    }
}
