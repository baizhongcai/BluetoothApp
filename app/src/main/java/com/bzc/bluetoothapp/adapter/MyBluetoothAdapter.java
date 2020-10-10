package com.bzc.bluetoothapp.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bzc.bluetoothapp.R;
import com.bzc.bluetoothapp.bean.BluetoothBean;

import org.w3c.dom.Text;

import java.util.List;

public class MyBluetoothAdapter extends BaseAdapter {

    private List<BluetoothDevice> mBluetoothList;
    private Context mContext;

    public MyBluetoothAdapter(List<BluetoothDevice> data, Context context){
        mBluetoothList = data;
        mContext = context;
    }
    public void setData(List<BluetoothDevice> data){
        mBluetoothList = data;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mBluetoothList.size();
    }

    @Override
    public Object getItem(int i) {
        return mBluetoothList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.activity_list_item, null);
            viewHolder.nameTextView = view.findViewById(R.id.name);
            viewHolder.addressTextView = view.findViewById(R.id.address);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.nameTextView.setText(mBluetoothList.get(i).getName());
        viewHolder.addressTextView.setText(mBluetoothList.get(i).getAddress());
        return view;
    }

    static class ViewHolder{
        TextView nameTextView;
        TextView addressTextView;
    }
}
