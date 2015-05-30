package com.fourtime.splash;

import com.fourtime.R;
import com.fourtime.constants.Constants;
import com.fourtime.home.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity{

	private static final int DELAY = 2500;	
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash_screen);
		Constants.isTablet = isTablet();
		requestPortraitOrientation();
		mHandler = new Handler();
		mHandler.postDelayed(onFinish, DELAY);
	}
	
	private void requestPortraitOrientation(){
		if(!Constants.isTablet){
			setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mHandler.removeCallbacks(onFinish);
	}

	private Runnable onFinish = new Runnable() {		
		@Override
		public void run() {
			Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	};

	private boolean isTablet() {
		boolean isTablet = false;
		
		if((getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK)
				>= Configuration.SCREENLAYOUT_SIZE_LARGE){
			
			if(Build.VERSION.SDK_INT > 9 && (getResources().getDisplayMetrics().densityDpi < 320)){
				isTablet = true;
			}			
		}	
		return isTablet;
	}

}
