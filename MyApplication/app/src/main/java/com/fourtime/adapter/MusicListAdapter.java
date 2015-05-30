package com.fourtime.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.bean.PlayListsBean;
import com.fourtime.constants.Constants;
import com.fourtime.utils.Utils;

public class MusicListAdapter extends BaseAdapter{

	private ArrayList<PlayListsBean> playListBeanList;
	private LayoutInflater mInflater;
	public ArrayList<Integer> insertList=new ArrayList<Integer>();
	private Context context;
	private int selectedPos = -1;

	public MusicListAdapter(Context context, ArrayList<PlayListsBean> contactBeanList){
		this.context=context;
		this.playListBeanList=contactBeanList;
		this.mInflater=(LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return playListBeanList.size();
	}

	@Override
	public Object getItem(int position) {
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
			convertView=mInflater.inflate(R.layout.music_list, null);
			holder=new ViewHolder();			
			holder.songName=(TextView)convertView.findViewById(R.id.songName);
			holder.soundIcon=(ImageView)convertView.findViewById(R.id.soundIcon);
			holder.durationText=(TextView)convertView.findViewById(R.id.durationText);
			holder.baseLayout=(RelativeLayout)convertView.findViewById(R.id.baselayout);
			convertView.setTag(holder);
		}
		else {
			holder=(ViewHolder)convertView.getTag();
		}
		
		/*if(playListBeanList.size()==1){
			convertView.setBackgroundResource(R.drawable.list_bg);
		}
		else{

			if(position == 0){
				convertView.setBackgroundResource(R.drawable.list_item_top);
			}else if(position == getCount()-1){
				convertView.setBackgroundResource(R.drawable.list_item_bottom);
			}else{
				convertView.setBackgroundResource(R.drawable.list_item_center);
			}
		}*/


		if(position ==Constants.listPosition){
			//holder.baseLayout.setBackgroundColor(Color.parseColor("#859E39"));
			holder.soundIcon.setVisibility(View.VISIBLE);
		}else{
			//holder.baseLayout.setBackgroundColor(Color.TRANSPARENT);
			holder.soundIcon.setVisibility(View.GONE);
		}

		holder.songName.setText(playListsBean.getDisplayName());
		holder.durationText.setText(Utils.milliSecondsToTimer(playListsBean.getDuration()));

		return convertView;

	}

	class ViewHolder {	
		TextView songName,durationText;
		ImageView soundIcon;
		RelativeLayout baseLayout;
	}

	public ArrayList<PlayListsBean> getTrackerBeanList() {
		return playListBeanList;
	}

	public void clearListData(){
		playListBeanList.clear();
		notifyDataSetChanged();
	}	

	public void setSelectedPosition(int pos){
		selectedPos = pos;
		// inform the view of this change
		notifyDataSetChanged();
	}

	public int getSelectedPosition(){
		return selectedPos;
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