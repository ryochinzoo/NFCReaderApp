package com.example.nfcreaderfornpo;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class customadapter extends ArrayAdapter<nfclistdata> 
{
	private LayoutInflater layoutInflater_;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.JAPAN);
	
	public customadapter(Context context, int textViewResourceId, List<nfclistdata> objects){
		super(context, textViewResourceId, objects);
		layoutInflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		nfclistdata item = (nfclistdata)getItem(position);
		
		if(null == convertView){
			convertView = layoutInflater_.inflate(R.layout.custom_list, null);
		}
		TextView textViewId;
		TextView textViewName;
		TextView textViewDate;
		
		textViewId = (TextView)convertView.findViewById(R.id.id);
		textViewName = (TextView)convertView.findViewById(R.id.name);
		//textViewDate = (TextView)convertView.findViewById(R.id.date);
		
		textViewId.setText(item.getId());
		textViewName.setText(item.getName());
		//textViewDate.setText(sdf.format(item.getDate()));
		
		return convertView;
		
		
	}

}
