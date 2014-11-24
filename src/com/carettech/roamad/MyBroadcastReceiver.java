package com.carettech.roamad;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import static com.carettech.roamad.TabsActivity.myphone;

public class MyBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//myphone = new MyPhoneActivity(context);
		myphone = BasicPhone.getInstance(context);
	    
	    myphone.login();
        /*Intent startServiceIntent = new Intent(context, MyPhoneActivity.class);
        context.startService(startServiceIntent);*/
    }

}
