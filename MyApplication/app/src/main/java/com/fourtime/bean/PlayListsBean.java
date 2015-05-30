package com.fourtime.bean;

import android.graphics.Bitmap;
import android.net.Uri;

public class PlayListsBean {
	
	private String artist;
	private Bitmap bitmap;
	private String album;
	private String track;
	private String data;
	private long albumId;
	private int duration;
	private Uri albumArtURI;
	private String displayName;
	public boolean isChecked;
	
	public PlayListsBean(String artist, Bitmap bitmap, String album,
			String track, String data, long albumId, int duration,
			Uri albumArtURI, String displayName, boolean isChecked) {
		super();
		this.artist = artist;
		this.bitmap = bitmap;
		this.album = album;
		this.track = track;
		this.data = data;
		this.albumId = albumId;
		this.duration = duration;
		this.albumArtURI = albumArtURI;
		this.displayName = displayName;
		this.isChecked = isChecked;
	}
	
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getTrack() {
		return track;
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public long getAlbumId() {
		return albumId;
	}
	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public Uri getAlbumArtURI() {
		return albumArtURI;
	}
	public void setAlbumArtURI(Uri albumArtURI) {
		this.albumArtURI = albumArtURI;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
	
	
	


}
