package com.fourtime.settings;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.constants.Constants;

public class AboutUsActivity extends Activity{
	
	private TextView versionNoText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int layoutRes = R.layout.about_dialog;
		if(!Constants.isTablet){			
			super.setTheme(android.R.style.Theme_NoTitleBar);	
			layoutRes = R.layout.about;
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	
		requestPortraitOrientation();
		setContentView(layoutRes);
		init();
		setupDefaults();		
	}
	
	private void requestPortraitOrientation(){
		if(!Constants.isTablet){
			setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
		}
	}
	
	private void init(){		
		versionNoText=(TextView)findViewById(R.id.versionNoText);
	}
	
	private void setupDefaults(){	
		versionNoText.setText(getVersionNo());		
	}
	
	
	private String getVersionNo(){	
		String version="";
		
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;			
			Log.e("VersionCode","Code"+versionCode);
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
				
		version = pInfo.versionName;								
		return version;
	}

}
