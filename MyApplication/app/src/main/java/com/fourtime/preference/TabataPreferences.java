package com.fourtime.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TabataPreferences {

	private static final String PREFS_NAME = "tabata_preferences";
	private static final String WORK = "work", REST = "rest", ROUNDS = "rounds", TABATAS = "tabatas";
	private SharedPreferences mPrefrences;
	private Editor mEditor;
	
	public TabataPreferences (Context context){
		mPrefrences = (SharedPreferences)context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mEditor = mPrefrences.edit();
	}
	
	public void setWork(long value){
		mEditor.putLong(WORK, value);
		mEditor.commit();
	}
	
	public void setRest(long value){
		mEditor.putLong(REST, value);
		mEditor.commit();
	}
	
	public void setRounds(int rounds){
		mEditor.putInt(ROUNDS, rounds);
		mEditor.commit();
	}
	
	public void setTabatas(int tabatas){
		mEditor.putInt(TABATAS, tabatas);
		mEditor.commit();
	}
	
	public long getWork(){
		return mPrefrences.getLong(WORK, 20000l);
	}
	
	public long getRest(){
		return mPrefrences.getLong(REST, 10000l);
	}
	
	public int getRounds(){
		return mPrefrences.getInt(ROUNDS, 8);
	}
	
	public int getTabatas(){
		return mPrefrences.getInt(TABATAS, 1);
	}
}
