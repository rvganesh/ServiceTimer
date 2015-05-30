package com.fourtime.playlist;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.adapter.PlayListsAdapter;
import com.fourtime.bean.PlayListsBean;
import com.fourtime.constants.Constants;

public class PlayListActivity extends Activity {

	private ListView listView;
	private Button doneButton;

	private PlayListsAdapter playListAdapter;
	private ArrayList<PlayListsBean> playListBeanList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int layoutRes = R.layout.playlists_dialog;
		if(!Constants.isTablet){			
			super.setTheme(android.R.style.Theme_NoTitleBar);	
			layoutRes = R.layout.playlists;
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(layoutRes);
		init();
		setupDefaults();
		setupEvents();
	}

	private void init(){
		listView=(ListView)findViewById(R.id.PhoneMusicList);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		doneButton=(Button)findViewById(R.id.doneBtn);
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);

		playListBeanList = new ArrayList<PlayListsBean>();
	}

	private void setupDefaults(){
		new GetAudioListAsynkTask().execute((Void) null);
	}

	private void setupEvents(){
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doneButtonPressed();
			}
		});
	}

	private class GetAudioListAsynkTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(PlayListActivity.this);
			progressDialog.setMessage("Loading");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				initLayout();
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			playListAdapter = new PlayListsAdapter(PlayListActivity.this,0, playListBeanList);
			listView.setAdapter(playListAdapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View view,int arg2, long arg3) {
					CheckBox checkbox = (CheckBox) view.getTag(R.id.chkSongs);
					checkbox.toggle();
				}
			});
		}

	}

	private void initLayout() {

		final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		final String[] cursor_cols = { 
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.DISPLAY_NAME,
		};

		final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
		final Cursor cursor = getContentResolver().query(uri,
				cursor_cols, where, null, null);

		while (cursor.moveToNext()) {
			String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
			String track = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			Long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

			int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

			Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
			Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
			String displayName=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));	

			//Log.e("URI","AlbumArt"+albumArtUri.toString());

			//Logger.debug(albumArtUri.toString());
			Bitmap bitmap = null;
			try {
				bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), albumArtUri);
				bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
				bitmap =null;
			} catch (IOException e) {
				e.printStackTrace();
			}

			playListBeanList.add(new PlayListsBean(artist, bitmap, album, track, data, albumId, duration, albumArtUri,displayName,false));
		}

	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(contentUri, proj,null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private void doneButtonPressed(){

		boolean exits=false;

		if(playListAdapter.insertList.size()>0){
			for (int insertvalues = 0; insertvalues < playListAdapter.insertList.size(); insertvalues++) {
				int insertvalue = playListAdapter.insertList.get(insertvalues);

				String displayName=playListBeanList.get(insertvalue).getDisplayName();

				if(Constants.playList.size()>0){
					exits=checkIsExist(displayName);
					if(!exits)
						Constants.playList.add(playListBeanList.get(insertvalue));
				}
				else{
					Constants.playList.add(playListBeanList.get(insertvalue));
				}
			}
		}

		setResult(RESULT_OK);
		finish();

	}
	
	private boolean checkIsExist(String displayName){
		
		boolean type=false;
		for(int i=0;i<Constants.playList.size();i++){
			if(Constants.playList.get(i).getDisplayName().equalsIgnoreCase(displayName)){
				type=true;
				break;
			}
			else{
				type=false;
			}			
		}
		return type;
		
	}

	
/*	position = 0;
	if(!symptomsSelection.equalsIgnoreCase("")){

		for(int i=0;i<symptomsList.size();i++){
			if(Migraine_Constants.symptomsSelectedList.contains(symptomsList.get(i).getSymptoms().toString())){
				position=(int) symptomsAdapter.getItemId(i);
				position=i;
				break;
			}
		}
	}else{
		position = symptomsList.size() - 1;
	}*/

}
