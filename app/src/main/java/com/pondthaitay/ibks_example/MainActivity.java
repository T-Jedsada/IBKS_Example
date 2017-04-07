package com.pondthaitay.ibks_example;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.accent_systems.ibks_sdk.scanner.ASBleScanner;
import com.accent_systems.ibks_sdk.scanner.ASResultParser;
import com.accent_systems.ibks_sdk.scanner.ASScannerCallback;
import com.accent_systems.ibks_sdk.utils.ASUtils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements ASScannerCallback {

    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_BLUE_TOOTH = 1001;
    private ASBleScanner bleScanner;

    @Override
    protected void onStart() {
        super.onStart();
        MainActivityPermissionsDispatcher.showLocationWithCheck(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bleScanner != null) ASBleScanner.stopScan();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showLocation() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This device supported bluetooth.", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_BLUE_TOOTH);
            } else {
                startScan();
            }
        }
    }

    private void startScan() {
        int err;
        bleScanner = new ASBleScanner(this, this);
        bleScanner.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        err = ASBleScanner.startScan();
        if (err != ASUtils.TASK_OK) {
            Log.i(TAG, "startScan - Error (" + Integer.toString(err) + ")");
            if (err == ASUtils.ERROR_LOCATION_PERMISSION_NOT_GRANTED) {
                MainActivityPermissionsDispatcher.showLocationWithCheck(this);
            }
        }
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_location_rationale)
                .setPositiveButton(R.string.button_allow, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.button_deny, (dialog, button) -> request.cancel())
                .show();
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.permission_location_never_ask_again, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUE_TOOTH) {
            if (resultCode == RESULT_OK) {
                startScan();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "User canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void scannedBleDevices(ScanResult scanResult) {
        String advertisingString = ASResultParser.byteArrayToHex(scanResult.getScanRecord().getBytes());
        String logstr = scanResult.getDevice().getAddress() + " / RSSI: " + scanResult.getRssi() + " / Adv packet: " + advertisingString;
        switch (ASResultParser.getAdvertisingType(scanResult)) {
            case ASUtils.TYPE_IBEACON:
                /**** Example to get data from advertising ***
                 advData = ASResultParser.getDataFromAdvertising(result);
                 try {
                 Log.i(TAG, "FrameType = " +advData.getString("FrameType")+" AdvTxPower = "+advData.getString("AdvTxPower")+" UUID = "+advData.getString("UUID")+" Major = "+advData.getString("Major")+" Minor = "+advData.getString("Minor"));
                 }catch (Exception ex){
                 Log.i(TAG,"Error parsing JSON");
                 }
                 /*******************************************/
                Log.i(TAG, scanResult.getDevice().getName() + " - iBEACON - " + logstr);
                break;
            case ASUtils.TYPE_EDDYSTONE_UID:
                /**** Example to get data from advertising ***
                 advData = ASResultParser.getDataFromAdvertising(result);
                 try {
                 Log.i(TAG, "FrameType = " +advData.getString("FrameType")+" AdvTxPower = "+advData.getString("AdvTxPower")+" Namespace = "+advData.getString("Namespace")+" Instance = "+advData.getString("Instance"));
                 }catch (Exception ex){
                 Log.i(TAG,"Error parsing JSON");
                 }
                 /*******************************************/
                Log.i(TAG, scanResult.getDevice().getName() + " - UID - " + logstr);
                break;
            case ASUtils.TYPE_EDDYSTONE_URL:
                /**** Example to get data from advertising ***
                 advData = ASResultParser.getDataFromAdvertising(result);
                 try {
                 Log.i(TAG, "FrameType = " +advData.getString("FrameType")+"  AdvTxPower = "+advData.getString("AdvTxPower")+" Url = "+advData.getString("Url"));
                 }catch (Exception ex){
                 Log.i(TAG,"Error parsing JSON");
                 }
                 /*******************************************/
                Log.i(TAG, scanResult.getDevice().getName() + " - URL - " + logstr);

                break;
            case ASUtils.TYPE_EDDYSTONE_TLM:
                /**** Example to get data from advertising ***
                 advData = ASResultParser.getDataFromAdvertising(result);
                 try {
                 if(advData.getString("Version").equals("0")){
                 Log.i(TAG, "FrameType = " +advData.getString("FrameType")+" Version = "+advData.getString("Version")+" Vbatt = "+advData.getString("Vbatt")+" Temp = "+advData.getString("Temp")+" AdvCount = "+advData.getString("AdvCount")+" TimeUp = "+advData.getString("TimeUp"));
                 }
                 else{
                 Log.i(TAG, "FrameType = " +advData.getString("FrameType")+" Version = "+advData.getString("Version")+" EncryptedTLMData = "+advData.getString("EncryptedTLMData")+" Salt = "+advData.getString("Salt")+" IntegrityCheck = "+advData.getString("IntegrityCheck"));
                 }
                 }catch (Exception ex){
                 Log.i(TAG,"Error parsing JSON");
                 }
                 /*******************************************/
                Log.i(TAG, scanResult.getDevice().getName() + " - TLM - " + logstr);
                break;
            case ASUtils.TYPE_EDDYSTONE_EID:
                /**** Example to get EID in Clear by the air ***
                 if(!readingEID) {
                 readingEID = true;
                 new ASEDSTService(null,this,10);
                 ASEDSTService.setClient_ProjectId(client, getPrefs.getString("projectId", null));
                 ASEDSTService.getEIDInClearByTheAir(result);
                 }
                 /**************************************************/
                /**** Example to get data from advertising ***
                 advData = ASResultParser.getDataFromAdvertising(result);
                 try {
                 Log.i(TAG, "FrameType = " +advData.getString("FrameType")+" AdvTxPower = "+advData.getString("AdvTxPower")+" EID = "+advData.getString("EID"));
                 }catch (Exception ex){
                 Log.i(TAG,"Error parsing JSON");
                 }
                 /*******************************************/
                Log.i(TAG, scanResult.getDevice().getName() + " - EID - " + logstr);
                break;
            case ASUtils.TYPE_DEVICE_CONNECTABLE:
                Log.i(TAG, scanResult.getDevice().getName() + " - CONNECTABLE - " + logstr);
                break;
            case ASUtils.TYPE_UNKNOWN:
                Log.i(TAG, scanResult.getDevice().getName() + " - UNKNOWN - " + logstr);
                break;
            default:
                Log.i(TAG, "ADVERTISING TYPE: " + "ERROR PARSING");
                break;
        }
    }
}

