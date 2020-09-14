package org.techtown.hoxy.waste;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.techtown.hoxy.R;

import java.util.ArrayList;

public class MypageListAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<MypageViewItem> listViewItemList = new ArrayList<MypageViewItem>() ;

    // ListViewAdapter의 생성자
    public MypageListAdapter() {
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_mypage, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView codeTextView = (TextView) convertView.findViewById(R.id.codeview) ;
        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameview) ;
        TextView feeTextView= (TextView) convertView.findViewById(R.id.feeview) ;
        TextView addressTextView= (TextView) convertView.findViewById(R.id.addressview) ;
        TextView dateTextView= (TextView) convertView.findViewById(R.id.dateview) ;
        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        MypageViewItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        codeTextView.setText(listViewItem.getApply_code());
        nameTextView.setText(listViewItem.getApply_name());
        feeTextView.setText(listViewItem.getApply_fee());
        addressTextView.setText(listViewItem.getApply_address());
        dateTextView.setText(listViewItem.getApply_date());

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String code, String name, String date,String fee,String address) {
        MypageViewItem item = new MypageViewItem();

        item.setApply_address(address);
        item.setApply_code(code);
        item.setApply_date(date);
        item.setApply_fee(fee);
        item.setApply_name(name);

        listViewItemList.add(item);
    }
}