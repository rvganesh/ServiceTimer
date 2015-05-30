package com.fourtime.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.LinearLayout;

import com.fourtime.R;
import com.fourtime.constants.Constants;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class Utils {

	public static void loadAd(Activity activity)
	{
		 AdView adView;
		// Create the adView
//		adView = new AdView(activity, AdSize.BANNER, "a15082bc5732b54");
		 adView = new AdView(activity, AdSize.BANNER, Constants.ADMOB_ID);
		// Lookup your LinearLayout assuming it’s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) activity.findViewById(R.id.adLayout);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		adView.loadAd(new AdRequest());
	}
	public static String format(long timeInMillis){
		int seconds = (int) (timeInMillis / 1000);
		int minutes = seconds / 60;

		seconds = seconds % 60;
		String time = "";
		
		if(minutes > 0){
			time += minutes + ":";
		}else{
			time += "0:"; 
		}
				
		time += seconds;				
		return time;
	}
	
	/*public static String getFormattedValue(long timeInMillis){
		long mSecs = timeInMillis / 100;
		int seconds = (int) (timeInMillis / 1000);
		int minutes = seconds / 60;

		seconds = seconds % 60;
		mSecs = mSecs % 10;

		String time = "";

		time += minutes + ":";	
		time += seconds + ":" + mSecs;		
		return time;
	}*/
	
	
	public static String getFormattedValue(long timeInMillis){
		long mSecs = timeInMillis / 100;
		int seconds = (int) (timeInMillis / 1000);
		int minutes = seconds / 60;

		seconds = seconds % 60;
		mSecs = mSecs % 10;

		String time = "";
		
		if(minutes == 0){
			time += "0000";
		}else if(minutes > 999){
			time += minutes;
		}else if(minutes > 99){
			time += "0" + minutes;
		}else if(minutes > 9){
			time += "00" + minutes;
		}else{
			time += "000" + minutes;
		}
		
		time += ":";

		if(seconds == 0){
			time += "00";
		}else if(seconds > 9){
			time += seconds;
		}else{
			time += "0" + seconds;
		}
		time += "." + mSecs;		
		return time;
	}
	
	
	
	public static String milliSecondsToTimer(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";
		
		// Convert total duration into time
		   int hours = (int)( milliseconds / (1000*60*60));
		   int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		   int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		   // Add hours if there
		   if(hours > 0){
			   finalTimerString = hours + ":";
		   }
		   
		   // Prepending 0 to seconds if it is one digit
		   if(seconds < 10){ 
			   secondsString = "0" + seconds;
		   }else{
			   secondsString = "" + seconds;}
		   
		   finalTimerString = finalTimerString + minutes + ":" + secondsString;
		
		// return timer string
		return finalTimerString;
	}
	
	public static void showAlert(Context context, String title, String msg){
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		if(title != null && !title.trim().equals("")){
			alert.setTitle(title);
		}
		
		if(msg != null && !msg.trim().equals("")){
			alert.setMessage(msg);
		}
		
		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alert.show();
	}

}
