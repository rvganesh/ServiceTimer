package com.fourtime.utils;

public class TimerData {

	public int flag, count;
	public long duration;
	
	public TimerData (int flag, int count, long duraion){
		this.flag = flag;
		this.count = count;
		this.duration = duraion;
	}
	
	public TimerData (int count, long duraion){
		this.count = count;
		this.duration = duraion;
	}
}
