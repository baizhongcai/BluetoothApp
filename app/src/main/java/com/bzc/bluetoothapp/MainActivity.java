package com.bzc.bluetoothapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bzc.bluetoothapp.adapter.MyBluetoothAdapter;
import com.bzc.bluetoothapp.utils.Params;
import com.bzc.bluetoothapp.utils.ToastUtils;

import org.w3c.dom.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mOpenBtn;
    private Button mCloseBtn;
    private Button mSearchBtn;
    private Button mBondedBtn;
    private List<BluetoothDevice> mBluetoothDeviceList;

    private ListView mBluetoothList;
    private BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
    private int mOpenBlueRequestCode = 400;
    private MyBluetoothAdapter myBluetoothAdapter;
    private LinearLayout mBottomLayout;
    private TextView mInfoMsgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        mOpenBtn = findViewById(R.id.openBtn);
        mCloseBtn = findViewById(R.id.closeBtn);
        mSearchBtn = findViewById(R.id.searchBtn);
        mBluetoothList = findViewById(R.id.bluetoothList);
        mBondedBtn = findViewById(R.id.bondedBtn);
        mBottomLayout = findViewById(R.id.bottomLayout);
        mInfoMsgView = findViewById(R.id.infoMessage);
        //获得蓝牙设备
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //绑定事件
        //打开蓝牙
        mOpenBtn.setOnClickListener(this);
        //关闭蓝牙
        mCloseBtn.setOnClickListener(this);
        //搜索蓝牙
        mSearchBtn.setOnClickListener(this);
        //获取已经配对的蓝牙设备
        mBondedBtn.setOnClickListener(this);
        //注册广播
        registerDiscoveryReceiver();
    }

    private void initData() {
        //创建adapter
        mBluetoothDeviceList = new ArrayList<>();
        myBluetoothAdapter = new MyBluetoothAdapter(new ArrayList<BluetoothDevice>(), this);
        mBluetoothList.setAdapter(myBluetoothAdapter);

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case Params.MY_PERMISSION_REQUEST_CONSTANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ToastUtils.showToast(this, "已经授权完成，正在搜索周围蓝牙设备...");
                }
                return;
            }
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.openBtn) {
            //打开蓝牙
            //openBlueAsyn();
            openBlueSync(this, mOpenBlueRequestCode);
        } else if (viewId == R.id.closeBtn) {
            //关闭蓝牙
            closeBlue();
        } else if (viewId == R.id.searchBtn) {
            //搜索蓝牙设备
            searchBlueList();
        } else if (viewId == R.id.bondedBtn) {
            //获取已经配对的蓝牙设备
            getBondedDevices();
        }
    }

    /**
     * 判断设备支持蓝牙模块不？
     * true : 蓝牙支持
     * false : 设备不支持
     */
    private boolean isSupportBlue() {
        return mBluetoothAdapter != null;
    }

    /**
     * 判断蓝牙设备是否开启
     * true : 蓝牙设备打开
     * false ： 蓝牙设备未打开
     */
    private boolean isOpenBlue() {
        return isSupportBlue() && mBluetoothAdapter.isEnabled();
    }

    /**
     * 自动打开蓝牙（异步：蓝牙不会立刻就处于开启状态）
     * 这个方法打开蓝牙不会弹出提示
     */
    private void openBlueAsyn() {
        if (!isOpenBlue()) {
            mBluetoothAdapter.enable();
        } else {
            ToastUtils.showToast(this, "蓝牙已经打开");
        }
    }

    /**
     * 关闭蓝牙设备
     */
    private void closeBlue() {
        if (isOpenBlue()) {
            mBluetoothDeviceList.clear();
            myBluetoothAdapter.notifyDataSetChanged();
            mBluetoothAdapter.disable();
        } else {
            ToastUtils.showToast(this, "蓝牙已经关闭");
        }
    }

    /**
     * 自动打开蓝牙（同步）
     * 这个方法打开蓝牙会弹出提示
     * 需要在onActivityResult 方法中判断resultCode == RESULT_OK  true为成功
     */
    public void openBlueSync(Activity activity, int requestCode) {
        if (!isOpenBlue()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, requestCode);
        } else {
            ToastUtils.showToast(this, "蓝牙已经打开");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mOpenBlueRequestCode) {
            if (resultCode == RESULT_OK) {
                ToastUtils.showToast(this, "蓝牙打开成功");
            } else {
                ToastUtils.showToast(this, "蓝牙打开失败");
            }
        }
    }

    /**
     * 进行设备的搜索
     */
    private void searchBlueList() {
        if (isOpenBlue()){
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }

            mBluetoothDeviceList.clear();
            myBluetoothAdapter.notifyDataSetChanged();

            /*
            下面代码一定要进行运行时的授权，不然系统广播没有办法进行处理
             */
            if (Build.VERSION.SDK_INT >= 6.0) {
                ActivityCompat.requestPermissions(MainActivity.this
                        , new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Params.MY_PERMISSION_REQUEST_CONSTANT);
            }
            mBluetoothAdapter.startDiscovery();
        }else{
            ToastUtils.showToast(this, "请先打开蓝牙设备");
        }
    }

    /**
     * 获取已经配对的蓝设备
     */
    private void getBondedDevices() {
        if (isOpenBlue()) {

            mBluetoothDeviceList.clear();
            myBluetoothAdapter.notifyDataSetChanged();

            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            if (bondedDevices.size() > 0) {

                mBluetoothDeviceList.clear();
                myBluetoothAdapter.notifyDataSetChanged();

                for (BluetoothDevice device:  bondedDevices) {
                    //远程蓝牙设备的物理地址和用户名
                    mBluetoothDeviceList.add(device);
                }

                myBluetoothAdapter.setData(mBluetoothDeviceList);
            } else {
                ToastUtils.showToast(this, "本机还没有任何配对的蓝牙设备");
            }
        } else {
            ToastUtils.showToast(this, "请先打开蓝牙设备");
        }
    }

    /**
     * 注册蓝牙广播
     */
    private void registerDiscoveryReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(discoveryReceiver, intentFilter);
    }

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();;
        private BluetoothDevice bluetoothDevice;
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    showInfoDialog("正在搜索附近的蓝牙设备...");
                    ToastUtils.showToast(MainActivity.this,"正在搜索附近的蓝牙设备...");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    hideInfoDialog();
                    ToastUtils.showToast(MainActivity.this, "搜索结束");
                    break;
                case BluetoothDevice.ACTION_FOUND:

                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (isNewDevice(bluetoothDevice)) {
                        mBluetoothDeviceList.add(bluetoothDevice);
                        myBluetoothAdapter.setData(mBluetoothDeviceList);
                        myBluetoothAdapter.notifyDataSetChanged();
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    switch(bluetoothDevice.getBondState()) {
                        case BluetoothDevice.BOND_BONDING://正在配对
                            Log.e("xxxx", "正在配对");
                            showInfoDialog("正在配对...");
                            break;
                        case BluetoothDevice.BOND_BONDED://配对结束
                            hideInfoDialog();
                            ToastUtils.showToast(MainActivity.this, "完成配对");
                            //startConnect(device);
                            break;
                        case BluetoothDevice.BOND_NONE://取消配对/未配对
                            Log.e("xxxx", "取消配对");
                            hideInfoDialog();
                            ToastUtils.showToast(MainActivity.this, "取消配对");
                            break;
                    }
            }
        }
    };

    /**
     * 显示加载信息
     */
    private void showInfoDialog(String title){
        if (mBottomLayout.getVisibility() == View.GONE) {
            mInfoMsgView.setText(title);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏加载信息
     */
    private void hideInfoDialog(){
        if (mBottomLayout.getVisibility() == View.VISIBLE){
            mBottomLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 判断搜索的设备是新蓝牙设备，且不重复
     *
     * @param device
     * @return
     */
    private boolean isNewDevice(BluetoothDevice device) {
        boolean repeatFlag = false;
        for (BluetoothDevice d :
                mBluetoothDeviceList) {
            if (d.getAddress().equals(device.getAddress())) {
                repeatFlag = true;
            }
        }
        //不是已绑定状态，且列表中不重复
        return device.getBondState() != BluetoothDevice.BOND_BONDED && !repeatFlag;
    }

    private void initEvent(){
        mBluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Method method = null;
                try {
                    if(mBluetoothDeviceList.get(i).getBondState() != BluetoothDevice.BOND_BONDED){
                        mBluetoothAdapter.cancelDiscovery();
                        method = BluetoothDevice.class.getMethod("createBond");
                        method.invoke(mBluetoothDeviceList.get(i));
                    }else{
                        ToastUtils.showToast(MainActivity.this, "该设备已经配对");
                    }

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(discoveryReceiver);
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

}