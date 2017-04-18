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
import android.widget.TextView;
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

    @SuppressWarnings("AccessStaticViaInstance")
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

    private double getDistance(int rssi, int txPower) {
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void scannedBleDevices(ScanResult scanResult) {
        if(scanResult == null) return;
        int txPower = scanResult.getScanRecord().getTxPowerLevel();
        ((TextView) findViewById(R.id.tv_distance)).setText(String.valueOf(getDistance(scanResult.getRssi(), txPower)));
        switch (ASResultParser.getAdvertisingType(scanResult)) {
            case ASUtils.TYPE_IBEACON:
                Log.d(TAG, "RSSI: " + scanResult.getRssi() + " / tx power: " + scanResult.getScanRecord().getTxPowerLevel());
                break;
            default:
                break;
        }
    }
}