package org.kohaerenzstiftung.andtroin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public class SchedulesAdapter extends BaseAdapter {


	private Database mDataBase;
	private Context mContext;

	public SchedulesAdapter(Context context, Database database) {
		super();
		this.mContext = context;
		this.mDataBase = database;
	}


	public int getCount() {
		return this.mDataBase.getScheduleCount();
	}


	public Object getItem(int position) {
		Schedule result = this.mDataBase.getScheduleByPosition(position);
		return result;
	}


	public long getItemId(int position) {
		Schedule schedule = (Schedule) this.getItem(position);
		return (long) schedule.getmId();
	}


	public View getView(int position, View convertView, ViewGroup parent) {
		ItemView itemView;
		if (convertView == null) {
			itemView = new ItemView(this.mContext, 1, LinearLayout.HORIZONTAL);
		} else {
			itemView = (ItemView) convertView;
		}
		Schedule schedule = (Schedule) this.getItem(position);
		String listname = this.mDataBase.getListNameById(schedule.getmListId());
		itemView.setTextValue(0, listname);
		return itemView;
	}

}
