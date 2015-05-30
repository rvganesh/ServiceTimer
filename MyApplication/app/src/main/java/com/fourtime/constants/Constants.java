package com.fourtime.constants;

import java.util.ArrayList;

import com.fourtime.bean.PlayListsBean;
import com.fourtime.utils.SoundEffects;

import android.graphics.Typeface;
import android.media.MediaPlayer;

public class Constants {
	public static Typeface tf_DIGITAL = null;
	public static Typeface tf_HELVETICA_BOLD = null;
	public static Typeface tf_HELVETICA_MEDIUM = null;
	
	public static SoundEffects soundFX = null;
	public static ArrayList<PlayListsBean> playList = new ArrayList<PlayListsBean>();
	public static boolean isListEmpty = true;
	public static int listPosition = 0;
	public static MediaPlayer mediaPlayer = new MediaPlayer();
	public static int COUNTDOWN_DURATION = 0;	
	public static String ADMOB_ID=" a151650cb86dd20";
	public static boolean isTablet = false;
}
