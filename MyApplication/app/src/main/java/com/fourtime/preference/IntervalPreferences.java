package com.fourtime.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class IntervalPreferences {

	private static final String PREFS_NAME = "interval_preferences";
	private static final String INTERVAL = "interval";
	private SharedPreferences mPrefrences;
	private Editor mEditor;
	
	public IntervalPreferences (Context context){
		mPrefrences = (SharedPreferences)context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mEditor = mPrefrences.edit();
	}
	
	public void setInterval(long value){
		mEditor.putLong(INTERVAL, value);
		mEditor.commit();
	}
	
	public long getInterval(){
		return mPrefrences.getLong(INTERVAL, 10000);
	}	
	
}
