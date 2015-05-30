package com.fourtime.utils;


import com.fourtime.R;
import com.fourtime.preference.SharedPersistance;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;


public class SoundEffects {
	private SoundPool soundPool;
	private int shortBeep, longBeep;
	private float mSoundVolume = 0f;

	public SoundEffects(Context context){
		soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		shortBeep = soundPool.load(context,R.raw.short_beep, 0);
		longBeep = soundPool.load(context,R.raw.long_beep, 0);
		resetTimerVolume(context);
	}

	public void playShortBeep(){
		if(soundPool!=null)			
			soundPool.play(shortBeep, mSoundVolume, mSoundVolume, 0, 0, 1.0f);
	}

	public void playLongBeep(){
		if(soundPool!=null)			
			soundPool.play(longBeep, mSoundVolume, mSoundVolume, 0, 0, 1.0f);
	}
	
	public void resetTimerVolume(Context context){
		int volume = new SharedPersistance(context).getTimerVolume();
		mSoundVolume = volume / 100f;
		Log.e("", "Current volume is:" + mSoundVolume);
	}
}
