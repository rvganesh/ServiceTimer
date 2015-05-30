package com.fourtime.adapter;

import java.util.List;
import com.fourtime.R;
import com.fourtime.bean.Item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SettingsAdapter extends ArrayAdapter<Item>{

	private LayoutInflater mInflater;

	public SettingsAdapter(Context context, int textViewResourceId,
			List<Item> objects) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.tabata_list_item_new, null);
			holder.txtItem = (TextView)convertView.findViewById(R.id.txtItemName);
			holder.txtValue = (TextView)convertView.findViewById(R.id.txtItemTime);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		if(getCount() == 1){
			convertView.setBackgroundResource(R.drawable.list_item_single);
		}else{
			if(position == 0){
				convertView.setBackgroundResource(R.drawable.list_item_top);
			}else if(position == getCount()-1){
				convertView.setBackgroundResource(R.drawable.list_item_bottom);
			}else{
				convertView.setBackgroundResource(R.drawable.list_item_center);
			}
		}

		final Item item = getItem(position);		
		holder.txtItem.setText(item.item);
		holder.txtValue.setText(item.value);

		return convertView;
	}

	private static class ViewHolder{
		TextView txtItem, txtValue;
	}

}
