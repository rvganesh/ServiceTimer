package com.fourtime.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.bean.PlayListsBean;
import com.fourtime.utils.Utils;

public class PlayListsAdapter extends ArrayAdapter<PlayListsBean>{

	private ArrayList<PlayListsBean> playListBeanList;
	private LayoutInflater mInflater;
	public ArrayList<Integer> insertList=new ArrayList<Integer>();
	private Context context;
	

	public PlayListsAdapter(Context context,int resId, ArrayList<PlayListsBean> contactBeanList){
		super(context, 0, contactBeanList);
		this.context=context;
		this.playListBeanList=contactBeanList;
		this.mInflater=(LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	
	/*public WeatherAdapter(Context context,int resId,ArrayList<WeatherWidgetBean> weatherLocationBean){
		super(context, 0, weatherLocationBean);
		mcontext=context;
		this.resId=resId;
		this.weatherWidgetBeanList=weatherLocationBean;
		layoutInflator=(LayoutInflater)mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader=new ImageLoader(mcontext);
		numberFormatter=new DecimalFormat("##0");	
	}*/


	@Override
	public int getCount() {
		return playListBeanList.size();
	}

	@Override
	public PlayListsBean getItem(int position) {
		return playListBeanList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		final PlayListsBean playListsBean = playListBeanList.get(position);

		if(convertView==null){
			convertView=mInflater.inflate(R.layout.playlists_listitem, null);
			holder=new ViewHolder();			
			holder.songName=(TextView)convertView.findViewById(R.id.songName);
			holder.folderName=(TextView)convertView.findViewById(R.id.folderName);
			holder.duration=(TextView)convertView.findViewById(R.id.durationText);
			holder.albumArt=(ImageView)convertView.findViewById(R.id.playlistImage);		
			holder.chkSongs=(CheckBox)convertView.findViewById(R.id.chkSongs);
			holder.baseLayout=(RelativeLayout)convertView.findViewById(R.id.baselayout);
			convertView.setTag(R.id.chkSongs, holder.chkSongs);
			convertView.setTag(holder);
		}
		else {
			holder=(ViewHolder)convertView.getTag();
		}

		holder.chkSongs.setOnCheckedChangeListener(null);
		holder.chkSongs.setTag(position);
		holder.chkSongs.setChecked(playListBeanList.get(position).isChecked());

		holder.songName.setText(playListsBean.getDisplayName());
		holder.folderName.setText(playListsBean.getAlbum());
		holder.duration.setText(Utils.milliSecondsToTimer(playListsBean.getDuration()));
		
		if(playListsBean.getBitmap()==null){
			holder.albumArt.setImageResource(R.drawable.music);
		}
		else{
			holder.albumArt.setImageBitmap(playListsBean.getBitmap());
		}

		holder.chkSongs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					insertList.add(position);
					playListBeanList.get(position).setChecked(true);
				}
				else {	
					int index=0;
					for(int i=0;i<insertList.size();i++){
						if(insertList.get(i)==position){
							index=i;
							break;
						}
					}
					insertList.remove(index);
					playListBeanList.get(position).setChecked(false);
				}
			}
		});		
		return convertView;
	}

	class ViewHolder {	
		TextView songName, folderName, duration;		
		ImageView albumArt;
		CheckBox chkSongs;
		RelativeLayout baseLayout;
	}

	public ArrayList<PlayListsBean> getTrackerBeanList() {
		return playListBeanList;
	}

	public void clearListData(){
		playListBeanList.clear();
		notifyDataSetChanged();
	}	


	public void refreshListings(ArrayList<PlayListsBean> list){		
		this.playListBeanList=list;		
		notifyDataSetChanged();
	}


	public static String roundTwoDecimals(double value) {
		DecimalFormat twoDForm = new DecimalFormat(".00");
		return twoDForm.format(value);
	}

	
	

}