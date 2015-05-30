package com.fourtime.adapter;

import java.util.ArrayList;
import com.fourtime.R;
import com.fourtime.result.Result;
import com.fourtime.result.ResultsActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TabataResultAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private ArrayList<Result> items;
	private int RESULT_TYPE = 0;

	public TabataResultAdapter(Context context, ArrayList<Result> items, int resultType) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		this.RESULT_TYPE = resultType;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.tabata_result_item, null);
			holder.txtItem = (TextView)convertView.findViewById(R.id.txtItemName);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		/*if(getCount() == 1){
			convertView.setBackgroundResource(R.drawable.list_item_single);
		}else{
			if(position == 0){
				convertView.setBackgroundResource(R.drawable.list_item_top);
			}else if(position == getCount()-1){
				convertView.setBackgroundResource(R.drawable.list_item_bottom);
			}else{
				convertView.setBackgroundResource(R.drawable.list_item_center);
			}
		}*/

		final Result item = (Result) getItem(position);

		String value = "empty";

		if(RESULT_TYPE == ResultsActivity.TABATA_RESULT){
			value = "Tabata: " + item.tabata + "   Round: " + item.round + "  Reps: " + item.reps;
			
		}else if(RESULT_TYPE == ResultsActivity.INTERVAL_RESULT){
			value = "Round: " + item.round + "  Reps: " + item.reps;
		}

		holder.txtItem.setText(value);

		return convertView;
	}

	private static class ViewHolder{
		TextView txtItem;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
