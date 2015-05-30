package com.fourtime.result;

import android.os.Parcel;
import android.os.Parcelable;

public class Result implements Parcelable{

	public int tabata, round, reps;
		
	/**This is for <b>Tabata resuts</b>
	 * @param tabata
	 * @param round
	 * @param reps
	 */
	public Result(int tabata, int round, int reps) {
		this.tabata = tabata;
		this.round = round;
		this.reps = reps;
	}
		
	/** This result item is for <b>Inverval reuslts</b>
	 * @param round
	 * @param reps
	 */
	public Result(int round, int reps){
		this.round = round;
		this.reps = reps;
	}
	
	public Result(Parcel data){
		this.tabata = data.readInt();
		this.round = data.readInt();
		this.reps = data.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(tabata);
		dest.writeInt(round);
		dest.writeInt(reps);	
	}
	
	public static final Creator<Result> CREATOR = new Creator<Result>() {
		@Override
		public Result[] newArray(int size) {
			return new Result[size];
		}
		
		@Override
		public Result createFromParcel(Parcel source) {
			return new Result(source);
		}
	};
	
}
