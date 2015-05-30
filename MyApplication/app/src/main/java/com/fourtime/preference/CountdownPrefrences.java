package com.fourtime.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CountdownPrefrences {

	private static final String PREFS_NAME = "countdown_preferences";
	private static final String DURATION = "interval";
	private SharedPreferences mPrefrences;
	private Editor mEditor;
	
	public CountdownPrefrences (Context context){
		mPrefrences = (SharedPreferences)context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mEditor = mPrefrences.edit();
	}
	
	public void setInterval(long value){
		mEditor.putLong(DURATION, value);
		mEditor.commit();
	}
	
	public long getInterval(){
		return mPrefrences.getLong(DURATION, 60000);
	}
	
	
}
