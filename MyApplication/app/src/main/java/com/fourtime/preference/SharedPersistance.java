package com.fourtime.preference;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPersistance {

	private final String PREPARATIONTIME = "preparationTime";
	private final String TIMER_VOLUME = "timer_volume";
	
	private SharedPreferences sharedPreferences;
	private Editor mEditor;

	public SharedPersistance(Context context){
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		mEditor = sharedPreferences.edit();
	}
	
	public int getPreparationTime(){
		return sharedPreferences.getInt(PREPARATIONTIME, 3);
	}

	public void setPreparationTime(int preparationTime){
		mEditor.putInt(PREPARATIONTIME,preparationTime);
		mEditor.commit();
	}
	
	public int getTimerVolume(){
		return sharedPreferences.getInt(TIMER_VOLUME, 20);
	}
	
	public void setTimerVolume(int volume){
		mEditor.putInt(TIMER_VOLUME, volume);
		mEditor.commit();
	}
	
}