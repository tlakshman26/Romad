package com.carettech.roamad;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    //public ImageLoader imageLoader; 
    
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       // imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);
        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView artist = (TextView)vi.findViewById(R.id.artist); // artist name
        TextView duration = (TextView)vi.findViewById(R.id.duration); // duration
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image);
        thumb_image.setVisibility(View.GONE);
        // thumb image
        /*Log.d("data size", ""+data.size());
        if (data.size() <= 0){
        	title.setText("No List was found");
            artist.setText("");
            duration.setText("");
        	return vi;
        }*/
        
        
        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);
        if (song.get("vm") == "Voicemail"){
        	title.setText(song.get("msg_from"));
            artist.setText(song.get("created"));
            duration.setText("0:00");
        }else{
        // Setting all values in listview
        title.setText(song.get("from"));
        artist.setText(song.get("start_time"));
        duration.setText(song.get("duration"));
        }
        //imageLoader.DisplayImage(song.get(CustomizedListView.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}