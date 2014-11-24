package com.carettech.roamad;

import com.carettech.roamad.BasicPhone;
import com.carettech.roamad.BasicPhone.BasicConnectionListener;
import com.carettech.roamad.BasicPhone.BasicDeviceListener;
import com.carettech.roamad.BasicPhone.LoginListener;

import com.carettech.roamad.R;
import com.twilio.client.Connection;
import com.twilio.client.Device;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.PhoneLookup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.util.Log;
import android.view.View;
import static com.carettech.roamad.TabsActivity.myphone;

public class IncomingCallActivity extends Activity implements LoginListener,
		BasicConnectionListener, BasicDeviceListener, View.OnClickListener,
		CompoundButton.OnCheckedChangeListener {
	private static final Handler handler = new Handler();

	private BasicPhone phone;
	private AlertDialog incomingAlert;

	// private ToggleButton speakerButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		myphone = BasicPhone.getInstance(getApplicationContext());
	    myphone.login();
		myphone.setListeners(this, this, this);
		phone = myphone;
		
		/*Intent myIntent1 = new Intent(IncomingCallActivity.this, TabsActivity.class);
		startActivity(myIntent1);
		finish();*/
		// speakerButton = (ToggleButton)findViewById(R.id.speaker_toggle);
		// speakerButton.setOnCheckedChangeListener(this);

		/*phone = BasicPhone.getInstance(getApplicationContext());
		phone.setListeners(this, this, this);
		phone.login();*/
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (phone.handleIncomingIntent(getIntent())) {
			showIncomingAlert();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (phone != null) {
			phone.setListeners(null, null, null);
			phone = null;
		}
	}
	
	 private void addStatusMessage(final String message)
	    {
	        handler.post(new Runnable()
	        {
	            @Override
	            public void run()
	            {
	                //logTextBox.append('-' + message + '\n');
	            	Log.d("LOGGER", message);
	            }
	        });
	    }

	    private void addStatusMessage(int stringId)
	    {
	        addStatusMessage(getString(stringId));
	    }
	    

	@Override
	public void onClick(View view) {
		/*if (view.getId() == R.id.main_button) {
			if (!phone.isConnected())
				phone.connect();
			else
				phone.disconnect();
		}*/
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		/*if (button.getId() == R.id.speaker_toggle)
			phone.setSpeakerEnabled(isChecked);*/
	}

	
	private void showIncomingAlert() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (incomingAlert == null) {
					incomingAlert = new AlertDialog.Builder(
							IncomingCallActivity.this)
							.setTitle(R.string.incoming_call)
							.setMessage(R.string.incoming_call_message)
							.setPositiveButton(R.string.answer,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											phone.acceptConnection();
										}
									})
							.setNegativeButton(R.string.ignore,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											phone.ignoreIncomingConnection();
										}
									})
							.setOnCancelListener(
									new DialogInterface.OnCancelListener() {
										@Override
										public void onCancel(
												DialogInterface dialog) {
											phone.ignoreIncomingConnection();
										}
									}).create();
					incomingAlert.show();
				}
			}
		});
	}

	private void hideIncomingAlert() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (incomingAlert != null) {
					incomingAlert.dismiss();
					incomingAlert = null;
				}
			}
		});
	}

	@Override
	public void onLoginStarted() {
		//addStatusMessage(R.string.logging_in);
	}

	@Override
	public void onLoginFinished() {
		addStatusMessage(phone.canMakeOutgoing() ? R.string.outgoing_ok
				: R.string.no_outgoing_capability);
		addStatusMessage(phone.canAcceptIncoming() ? R.string.incoming_ok
				: R.string.no_incoming_capability);
	}

	@Override
	public void onLoginError(Exception error) {
		if (error != null)
			addStatusMessage(String.format(getString(R.string.login_error_fmt),
					error.getLocalizedMessage()));
		else
			addStatusMessage(R.string.login_error_unknown);
	}

	@Override
	public void onIncomingConnectionDisconnected() {
		hideIncomingAlert();
		addStatusMessage(R.string.incoming_disconnected);
	}

	@Override
	public void onConnectionConnecting() {
		addStatusMessage(R.string.attempting_to_connect);
	}

	@Override
	public void onConnectionConnected() {
		addStatusMessage(R.string.connected);
	}

	@Override
	public void onConnectionFailedConnecting(Exception error) {
		if (error != null)
			addStatusMessage(String.format(
					getString(R.string.couldnt_establish_outgoing_fmt),
					error.getLocalizedMessage()));
		else
			addStatusMessage(R.string.couldnt_establish_outgoing);
	}

	@Override
	public void onConnectionDisconnecting() {
		addStatusMessage(R.string.disconnect_attempt);
	}

	@Override
	public void onConnectionDisconnected() {
		addStatusMessage(R.string.disconnected);
	}

	@Override
	public void onConnectionFailed(Exception error) {
		if (error != null)
			addStatusMessage(String.format(
					getString(R.string.connection_error_fmt),
					error.getLocalizedMessage()));
		else
			addStatusMessage(R.string.connection_error);
	}

	@Override
	public void onDeviceStartedListening() {
		addStatusMessage(R.string.device_listening);
	}

	@Override
	public void onDeviceStoppedListening(Exception error) {
		if (error != null)
			addStatusMessage(String.format(
					getString(R.string.device_listening_error_fmt),
					error.getLocalizedMessage()));
		else
			addStatusMessage(R.string.device_not_listening);
	}
}

/*
 * //private MyPhoneActivity phone; //private ImageButton answer, decline,
 * answer_and_hold, speakerON, speakerOFF; Device device; Connection connection;
 * Intent resumeintent; AudioManager audioManager; private TextView
 * incomingName; private Button voice_mail;
 * 
 * @Override public void onCreate(Bundle bundle) { super.onCreate(bundle);
 * setContentView(R.layout.main);
 * 
 * //phone = new MyPhoneActivity(getApplicationContext());
 * 
 * answer = (ImageButton)findViewById(R.id.ic_jog_dial_answer); decline =
 * (ImageButton)findViewById(R.id.ic_jog_dial_decline); answer_and_hold =
 * (ImageButton)findViewById(R.id.ic_jog_dial_answer_and_hold);
 * 
 * incomingName = (TextView)findViewById(R.id.text_incomingName); voice_mail =
 * (Button)findViewById(R.id.btn_voice_mail); // speakerON =
 * (ImageButton)findViewById(R.id.Speaker_on); // speakerOFF =
 * (ImageButton)findViewById(R.id.Speaker_off);
 * 
 * 
 * 
 * ImageButton dialButton = (ImageButton)findViewById(R.id.dialButton);
 * //dialButton.setBackground(android.R.drawable.ic_btn_speak_now);
 * dialButton.setOnClickListener(this);
 * 
 * ImageButton hangupButton = (ImageButton)findViewById(R.id.hangupButton);
 * hangupButton.setOnClickListener(this); // SharedPreferences sharedpreferences
 * = getSharedPreferences(getString(R.string.preference_file_key),
 * Context.MODE_PRIVATE);
 * 
 * if (!(sharedpreferences.contains(getString(R.string.sid)))) { Intent
 * myIntent1 = new Intent(IncomingCallActivity.this, TabsActivity.class);
 * startActivity(myIntent1); finish(); }
 * 
 * //numberField = (EditText)findViewById(R.id.numberField); }
 * 
 * 
 * 
 * public void answerCall(View view) { view.setVisibility(View.GONE);
 * voice_mail.setVisibility(View.GONE);
 * resumeintent.removeExtra(Device.EXTRA_DEVICE);
 * resumeintent.removeExtra(Device.EXTRA_CONNECTION);
 * myphone.handleIncomingConnection(device, connection);
 * 
 * audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); //
 * speakerON.setVisibility(View.VISIBLE); //sanswer.setVisibility(View.GONE); //
 * answer_and_hold.setVisibility(View.VISIBLE); //
 * decline.setVisibility(View.VISIBLE);
 * 
 * }
 * 
 * public void declineCall(View view) { //
 * phone.handleIncomingConnection(device, connection); if(connection != null){
 * connection.disconnect(); myphone.handleDeclieConnection(); //
 * answer.setVisibility(View.GONE); // answer_and_hold.setVisibility(View.GONE);
 * // decline.setVisibility(View.GONE); // speakerON.setVisibility(View.GONE);
 * // speakerOFF.setVisibility(View.GONE); }
 * 
 * finish();
 * 
 * }
 * 
 * public void Speaker_on(View view) {
 * 
 * //AudioManager audioManager = (AudioManager)
 * getSystemService(Context.AUDIO_SERVICE);
 * audioManager.setMode(AudioManager.MODE_IN_CALL);
 * audioManager.setSpeakerphoneOn(true); //speakerON.setVisibility(View.GONE);
 * // speakerOFF.setVisibility(View.VISIBLE);
 * 
 * 
 * }
 * 
 * public void Speaker_off(View view) { audioManager.setSpeakerphoneOn(false);
 * //speakerOFF.setVisibility(View.GONE); //
 * speakerON.setVisibility(View.VISIBLE); }
 * 
 * 
 * @Override public void onNewIntent(Intent intent) { super.onNewIntent(intent);
 * setIntent(intent); }
 * 
 * @Override public void onResume() { super.onResume(); resumeintent =
 * getIntent(); device = resumeintent.getParcelableExtra(Device.EXTRA_DEVICE);
 * connection = resumeintent.getParcelableExtra(Device.EXTRA_CONNECTION); if
 * (device != null && connection != null) { //
 * answer.setVisibility(View.VISIBLE); String phoneNumber =
 * connection.getParameters().get(connection.IncomingParameterFromKey); String
 * contactMatchName = phoneNumber; Uri uri =
 * Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
 * Uri.encode(phoneNumber)); //resolver.query(uri, new
 * String[]{PhoneLookup.DISPLAY_NAME}; ContentResolver contentResolver =
 * getContentResolver();
 * 
 * //Uri uri =
 * Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
 * Uri.encode(phoneNumber));
 * 
 * String[] projection = new String[] {PhoneLookup.DISPLAY_NAME,
 * PhoneLookup._ID};
 * 
 * Cursor cursor = contentResolver.query( uri, projection, null, null, null);
 * 
 * if(cursor!=null) { while(cursor.moveToNext()){ contactMatchName =
 * cursor.getString(cursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
 * String contactId =
 * cursor.getString(cursor.getColumnIndexOrThrow(PhoneLookup._ID));
 * //Log.d("contactMatch name:", "contactMatch name: " + contactName);
 * //Log.d("contactMatch id:", "contactMatch id: " + contactId); }
 * cursor.close(); } incomingName.setText(contactMatchName); } } }
 */