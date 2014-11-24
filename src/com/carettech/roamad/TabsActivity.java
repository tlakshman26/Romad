package com.carettech.roamad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class TabsActivity extends Activity{

public static BasicPhone myphone;
private static Context context;
static String number;
static View settingview;
SharedPreferences sharedpreferences;

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    context = this;
    //myphone =  new MyPhoneActivity(getApplicationContext());
    myphone = BasicPhone.getInstance(getApplicationContext());
    //
    myphone.login();
    /*Intent startServiceIntent = new Intent(context, MyPhoneActivity.class);
    context.startService(startServiceIntent);
    if (MyPhoneActivity.isInstanceCreated()){
    	myphone = MyPhoneActivity.instance;
    }*/
     sharedpreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    number = sharedpreferences.getString(context.getString(R.string.incoming_phone_number),"");
    // startService(myphone);
    ActionBar bar = getActionBar();
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    ActionBar.Tab home = bar.newTab().setText("Home");
    ActionBar.Tab activity = bar.newTab().setText("Activity");
    ActionBar.Tab voicemail = bar.newTab().setText("Voicemail");
    ActionBar.Tab settings = bar.newTab().setText("settings");

    Fragment fragmentA = new LaunchpadSectionFragment(); //FragmentTab1.newInstance(0, "Page # 1");
    Fragment fragmentB = new FragmentTab1();
    Fragment fragmentC = new FragmentTab2();
    Fragment fragmentD = new DummySectionFragment();
    
    
    home.setTabListener(new MyTabsListener(fragmentA));
    activity.setTabListener(new MyTabsListener(fragmentB));
    voicemail.setTabListener(new MyTabsListener(fragmentC));
    settings.setTabListener(new MyTabsListener(fragmentD));
    
    bar.addTab(home);
    bar.addTab(activity);
    bar.addTab(voicemail);
    bar.addTab(settings);
    
   // checkVoiceMailStaus();

}


	
protected class MyTabsListener implements ActionBar.TabListener {

    private Fragment fragment;

    public MyTabsListener(Fragment fragment) {
        this.fragment = fragment;
    }
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        ft.add(R.id.fragment_container, fragment, null);
    }
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // some people needed this line as well to make it work: 
        ft.remove(fragment);
    }
	
	
}

public static class LaunchpadSectionFragment extends Fragment {
	
	 // storing string resources into Array
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
       View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);

       return rootView;
   }
}

public static class DummySectionFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";
    ToggleButton toggle;
    SharedPreferences sharedpreferences;
    Editor editor;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	
    	
        settingview = inflater.inflate(R.layout.fragment_section_dummy, container, false);
        sharedpreferences = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
     //   Bundle args = getArguments();
        ((TextView) settingview.findViewById(R.id.textView1)).setText(number);
                //getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
        
        toggle = (ToggleButton) settingview.findViewById(R.id.togglebutton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	new loginTask().execute();
                editor.putBoolean("voice_status", isChecked);
		 		editor.commit();
            }
        });
        boolean s = sharedpreferences.getBoolean("voice_status", false);
        toggle.setChecked(s);
        return settingview;
    }

    
/*
 * AsyncTask for Fetch data from database or online
 */
public class loginTask extends AsyncTask<Void, Void, Void> {
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
		 boolean s = sharedpreferences.getBoolean("voice_status", false);
		 try{
			 HttpClient client = new DefaultHttpClient();
			 String ACCOUNT_SID, AUTH_TOKEN, CLIENT_NAME,PHONE_SID;
			 ACCOUNT_SID = sharedpreferences.getString(getString(R.string.sid),"");
			 AUTH_TOKEN = sharedpreferences.getString(getString(R.string.auth_token),"");
			 PHONE_SID = sharedpreferences.getString(getString(R.string.phone_sid),"");
			 String basicAuth = "Basic " + Base64.encodeToString(String.format("%s:%s", ACCOUNT_SID, AUTH_TOKEN).getBytes(), Base64.NO_WRAP);
			 Log.d("lakshman",getString(R.string.url_postincomingphonenumbers,ACCOUNT_SID,PHONE_SID));
			 HttpPost request3 = new HttpPost(getString(R.string.url_postincomingphonenumbers,ACCOUNT_SID,PHONE_SID));
			 List<NameValuePair> value = new ArrayList<NameValuePair>();
			 //value.add(new BasicNameValuePair("PhoneNumber", number));
			 if (s){
				 value.add(new BasicNameValuePair("VoiceUrl", getString(R.string.url_voicefallback,ACCOUNT_SID)));
				 value.add(new BasicNameValuePair("VoiceFallbackUrl", getString(R.string.url_voicefallback,ACCOUNT_SID)));
			 }else{
				 value.add(new BasicNameValuePair("VoiceUrl", getString(R.string.url_voice,ACCOUNT_SID)));
				 value.add(new BasicNameValuePair("VoiceFallbackUrl", getString(R.string.url_voicefallback,ACCOUNT_SID)));
			 }
			 	
			 UrlEncodedFormEntity entity = new UrlEncodedFormEntity(value);
			request3.setEntity(entity);
			request3.setHeader("Authorization", basicAuth);
			HttpResponse res = client.execute(request3);
			int statusCode = res.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK | statusCode == HttpStatus.SC_CREATED) {
				String bufstring = EntityUtils.toString(res.getEntity(),"UTF-8");
				JSONObject IncomingData = new JSONObject(bufstring);
				Log.d("TAG", bufstring);
			}	
		}catch (Exception e) {
            Log.e("TAG", "Failed to set call list : " + e.getLocalizedMessage()+ e);
        }
		
		/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/return null;
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
         
            	//ToggleButton lv = (ToggleButton) settingview.findViewById(R.id.togglebutton);
         		//lv.setChecked(false);
            }
        });
	}
	
}

}
}

/*
public class TabsActivity extends FragmentActivity implements ActionBar.TabListener {

    *//**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     *//*
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    *//**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     *//*
    
    ViewPager mViewPager;

	public static MyPhoneActivity myphone;
    static String number;

	private static Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        //getActionBar().hide();
        context = this;
        myphone = new MyPhoneActivity(getApplicationContext());
        setContentView(R.layout.activity_main);
        SharedPreferences sharedpreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        number = sharedpreferences.getString(context.getString(R.string.incoming_phone_number),"");

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        //actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
        	String[] tabNames = new String[]{"Home","Activity","Voicemail","settings"};
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(tabNames[i])
                            .setTabListener(this));
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    *//**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     *//*
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return FragmentTab1.newInstance(0, "Page # 1");
                case 1:
                	//Toast.makeText(ctx, "5455", Toast.LENGTH_LONG).show();
                	//return new ListpadSectionFragment();
                	return FragmentTab1.newInstance(0, "Page # 2");
                case 2:
                	return FragmentTab1.newInstance(0, "Page # 3");
                	//return new ListpadSectionFragment();
                
                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment fragment = new DummySectionFragment();
                    Bundle args = new Bundle();
                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);
                    return fragment;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + (position + 1);
        }
    }

    *//**
     * A fragment that launches other parts of the demo application.
     *//*
    public static class LaunchpadSectionFragment extends Fragment {
    	
    	 // storing string resources into Array
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);

            // Demonstration of a collection-browsing activity.
            rootView.findViewById(R.id.demo_collection_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        	
                        	String phoneNumber = "+919700540500";//connection.getParameters().get(connection.IncomingParameterFromKey);
                            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                            //resolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME};
                            ContentResolver contentResolver = context.getContentResolver();

                            //Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

                            String[] projection = new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID};

                            Cursor cursor =  
                               contentResolver.query(
                                    uri, 
                                    projection, 
                                    null, 
                                    null, 
                                    null);

                            if(cursor!=null) {
                              while(cursor.moveToNext()){
                                String contactName = cursor.getString(cursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
                                String contactId = cursor.getString(cursor.getColumnIndexOrThrow(PhoneLookup._ID));
                                Log.d("contactMatch name:", "contactMatch name: " + contactName);
                                Log.d("contactMatch id:", "contactMatch id: " + contactId);
                              }
                              cursor.close();
                            }
                            Intent intent = new Intent(getActivity(), CollectionDemoActivity.class);
                            startActivity(intent);
                        }
                    });

            // Demonstration of navigating to external activities.
            rootView.findViewById(R.id.demo_external_activity)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Create an intent that asks the user to pick a photo, but using
                            // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, ensures that relaunching
                            // the application from the device home screen does not return
                            // to the external activity.
                            Intent externalActivityIntent = new Intent(Intent.ACTION_PICK);
                            externalActivityIntent.setType("image/*");
                            externalActivityIntent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(externalActivityIntent);
                        }
                    });

            return rootView;
        }
    }

    
    *//**
     * A fragment that launches other parts of the demo application.
     *//*
    public static class ListpadSectionFragment extends Fragment {
    	
    	 // storing string resources into Array
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	// storing string resources into Array
            String[] adobe_products = getResources().getStringArray(R.array.adobe_products);
           // String[] items= getResources().getStringArray(R.array.adobe_products);;
			ListView rootView = (ListView) inflater.inflate(R.layout.activity_list, container, false);
            rootView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item, R.id.label, adobe_products));
            
          
            // Demonstration of a collection-browsing activity.
            rootView.findViewById(R.id.demo_collection_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), CollectionDemoActivity.class);
                            startActivity(intent);
                        }
                    });

            // Demonstration of navigating to external activities.
            rootView.findViewById(R.id.demo_external_activity)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Create an intent that asks the user to pick a photo, but using
                            // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, ensures that relaunching
                            // the application from the device home screen does not return
                            // to the external activity.
                            Intent externalActivityIntent = new Intent(Intent.ACTION_PICK);
                            externalActivityIntent.setType("image/*");
                            externalActivityIntent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(externalActivityIntent);
                        }
                    });
           
            return rootView;
        }
    }

    
    *//**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     *//*
    public static class DummySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
         //   Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText("your roamd number \n \n " + number);
                    //getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
*/