package org.techtown.hoxy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.techtown.hoxy.community.PostItem;
import org.techtown.hoxy.waste.WasteInfoItem;

import java.util.ArrayList;

public class NoticeAdapter extends BaseAdapter {

    private ArrayList<NoticeItem> noticeItems = new ArrayList<NoticeItem>();

    @Override
    public int getCount() {
        return noticeItems.size();
    }

    @Override
    public Object getItem(int position) {
        return  noticeItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(NoticeItem item){
        noticeItems.add(item);

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if(convertView == null)
        {
            LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.notice_item_view, parent, false);
        }

        TextView notice = convertView.findViewById(R.id.notice);


        NoticeItem noticeItem = noticeItems.get(position);

        notice.setText(noticeItem.getNotice());


        return convertView;
    }
}
