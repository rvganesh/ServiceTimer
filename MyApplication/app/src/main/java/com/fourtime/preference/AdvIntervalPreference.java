package com.fourtime.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AdvIntervalPreference {

	private static final String PREFS_NAME = "advinterval_preferences";
	private static final String ROUNDS = "rounds";
	private static final String VALUE = "values";
	private static final String LOOP_INTERVAL = "is_loop_interval";
	
	private SharedPreferences mPrefrences;
	private Editor mEditor;
	
	public AdvIntervalPreference (Context context){
		mPrefrences = (SharedPreferences)context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mEditor = mPrefrences.edit();
	}
		
	public void setRounds(int rounds){
		mEditor.putInt(ROUNDS, rounds);
		mEditor.commit();
	}
				
	public int getRounds(){
		return mPrefrences.getInt(ROUNDS, 1);
	}
		
	public void setRoundIntervals(String value){
		mEditor.putString(VALUE, value);
		mEditor.commit();
	}
	
	public String getRoundIntervals(){
		return mPrefrences.getString(VALUE, "60000");
	}
	
	public boolean isLoopInterval(){
		return mPrefrences.getBoolean(LOOP_INTERVAL, false);
	}
	
	public void setLoopInterval(boolean value){
		mEditor.putBoolean(LOOP_INTERVAL, value);
		mEditor.commit();
	}
		
}
