package com.fourtime.result;

import java.util.ArrayList;

import com.fourtime.R;
import com.fourtime.adapter.TabataResultAdapter;
import com.fourtime.constants.Constants;
import com.fourtime.service.TabataService;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class ResultsActivity extends Activity{
	public static final String RESULT_TYPE = "result_type";
	public static final int TABATA_RESULT = 0;
	public static final int INTERVAL_RESULT = 1;	
	private ListView listResults;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestPortraitOrientation();
		setContentView(R.layout.tabata_results);
		
		init();
		setupDefaults();
	}
	
	private void requestPortraitOrientation(){
		if(!Constants.isTablet){
			setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
		}
	}
	
	private void init(){
		listResults = (ListView)findViewById(R.id.listTabataResults);
		listResults.setEmptyView(findViewById(R.id.list_empty));
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);
	}
	
	private void setupDefaults(){
		ArrayList<Result> results = getIntent().getParcelableArrayListExtra(TabataService.VALUE);
		int resultType = getIntent().getIntExtra(RESULT_TYPE, 0);
		listResults.setAdapter(new TabataResultAdapter(this, results, resultType));	
	}
	
}
