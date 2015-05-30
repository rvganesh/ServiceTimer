package com.fourtime.playlist;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.adapter.MusicListAdapter;
import com.fourtime.bean.PlayListsBean;
import com.fourtime.constants.Constants;
import com.fourtime.utils.Utils;

public class MusicActivity extends Activity {

	private ListView listView;
	private TextView albumText,titleText,artistText,tapToaddSongs,durationText;
	private Button taptoAddSongButton;
	private ImageView albumArtImage;
	private ImageView previousImage, playImage,nextImage;
	private RelativeLayout titlesLayout;
	private MusicListAdapter adapter;
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100) {
			if(resultCode == Activity.RESULT_OK){
				refreshListings();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int layoutRes = R.layout.music_dialog;
		if(!Constants.isTablet){			
			super.setTheme(android.R.style.Theme_NoTitleBar);	
			layoutRes = R.layout.music;
		}
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(layoutRes);
		init();
		setupDefaults();
		setupEvents();
	}

	private void init(){

		//doneBtn =(Button)findViewById(R.id.doneBtn);
		taptoAddSongButton=(Button)findViewById(R.id.taptoaddSong);
		titlesLayout=(RelativeLayout)findViewById(R.id.titlesLayout);
		listView=(ListView)findViewById(R.id.musicplaylists);
		durationText=(TextView)findViewById(R.id.durationText);
		tapToaddSongs=(TextView)findViewById(R.id.tapToadd);
		albumText=(TextView)findViewById(R.id.albumText);
		titleText=(TextView)findViewById(R.id.titleText);
		artistText=(TextView)findViewById(R.id.artistText);
		albumArtImage=(ImageView)findViewById(R.id.albumartimg);
		previousImage=(ImageView)findViewById(R.id.previous);
		playImage=(ImageView)findViewById(R.id.play);
		nextImage=(ImageView)findViewById(R.id.next);
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);

	}

	private void setupDefaults(){

		if(Constants.playList.size()>0){
			listView.setVisibility(View.VISIBLE);
			titlesLayout.setVisibility(View.VISIBLE);
			tapToaddSongs.setVisibility(View.GONE);
			playImage.setEnabled(true);
			adapter=new MusicListAdapter(this, Constants.playList);
			listView.setAdapter(adapter);
			setTitles();
		}
		else{
			titlesLayout.setVisibility(View.GONE);
			tapToaddSongs.setVisibility(View.VISIBLE);
			playImage.setEnabled(false);
			listView.setVisibility(View.GONE);
			previousImage.setImageResource(R.drawable.previous_disable);
			nextImage.setImageResource(R.drawable.next_disable);
		}

		taptoAddSongButton.setOnClickListener(onActionClick);
	}

	private void setupEvents(){

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int pos,long id) {
				System.gc();
				Constants.listPosition=pos;
				playSong(Constants.listPosition);
				adapter.setSelectedPosition(Constants.listPosition);	
			}});

		previousImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(Constants.playList.size() > 0){

					if(Constants.listPosition > 0){
						playSong(Constants.listPosition - 1);
						Constants.listPosition = Constants.listPosition - 1;
					}else{
						// play last song
						playSong(Constants.playList.size() - 1);
						Constants.listPosition = Constants.playList.size() - 1;
					}

					adapter.setSelectedPosition(Constants.listPosition);
				}

			}});

		playImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Constants.playList.size()> 0){
					if(Constants.mediaPlayer.isPlaying()){
						Constants.mediaPlayer.pause();
						playImage.setImageResource(R.drawable.musicplay);
					}
					else{
						Constants.mediaPlayer.start();
						playImage.setImageResource(R.drawable.music_stop);
					}
					adapter.setSelectedPosition(Constants.listPosition);
				}

			}});

		nextImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Constants.playList.size()>0){
					if(Constants.listPosition < (Constants.playList.size() - 1)){
						playSong(Constants.listPosition + 1);
						Constants.listPosition = Constants.listPosition + 1;
					}else{
						// play first song
						playSong(0);
						Constants.listPosition = 0;
					}
					adapter.setSelectedPosition(Constants.listPosition);
				}
			}});

		Constants.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(Constants.playList.size()>0){
					if(Constants.listPosition < (Constants.playList.size() - 1)){
						playSong(Constants.listPosition + 1);
						Constants.listPosition = Constants.listPosition + 1;
					}else{
						// play first song
						playSong(0);
						Constants.listPosition = 0;
					}
					adapter.setSelectedPosition(Constants.listPosition);
				}
			}});

	}

	
	private OnClickListener onActionClick = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.taptoaddSong){
				openPlayLists();
			}
		}
	};

	private void openPlayLists(){

		if(Constants.playList.size()>0){
			Constants.isListEmpty=false;

		}

		Intent intent = new Intent(MusicActivity.this,PlayListActivity.class);
		startActivityForResult(intent, 100);
	}

	private void refreshListings(){	

		if(adapter!=null){
			adapter.notifyDataSetChanged();
		}
		else{
			adapter=new MusicListAdapter(this, Constants.playList);
			listView.setAdapter(adapter);
		}

		if(Constants.isListEmpty){

			if(Constants.playList.size()>0){
				Constants.listPosition=0;
				tapToaddSongs.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				playImage.setEnabled(true);
				//playSong(Constants.listPosition);
				setTitles();
				previousImage.setImageResource(R.drawable.previous);
				nextImage.setImageResource(R.drawable.next);
				adapter.setSelectedPosition(Constants.listPosition);
				titlesLayout.setVisibility(View.VISIBLE);				
			}
			else{
				tapToaddSongs.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
				titlesLayout.setVisibility(View.GONE);
				playImage.setEnabled(false);
				previousImage.setImageResource(R.drawable.previous_disable);
				nextImage.setImageResource(R.drawable.next_disable);
			}			
		}

	}

	public void  playSong(int songIndex){

		try {

			System.gc();
			PlayListsBean bean=Constants.playList.get(songIndex);

			Constants.mediaPlayer.reset();
			Constants.mediaPlayer.setDataSource(bean.getData());
			Constants.mediaPlayer.prepare();
			Constants.mediaPlayer.start();

			setTitles();
			previousImage.setImageResource(R.drawable.previous);
			nextImage.setImageResource(R.drawable.next);
			// Changing Button Image to pause image
			playImage.setImageResource(R.drawable.music_stop);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setTitles(){
		
		PlayListsBean bean=Constants.playList.get(Constants.listPosition);
		if(bean.getBitmap()==null){
			albumArtImage.setImageResource(R.drawable.music);
		}
		else{
			albumArtImage.setImageBitmap(bean.getBitmap());
		}
		
		albumText.setText("Album: " + bean.getAlbum());
		titleText.setText("Title: " +bean.getDisplayName());
		artistText.setText("Artist: " + bean.getArtist());
		durationText.setText("Duration :" + Utils.milliSecondsToTimer(bean.getDuration()));
		
	}

}