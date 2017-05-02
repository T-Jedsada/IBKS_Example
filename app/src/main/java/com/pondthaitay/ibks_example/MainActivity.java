package com.pondthaitay.ibks_example;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final DecimalFormat DOUBLE_TWO_DIGIT_ACCURACY = new DecimalFormat("#.##");
    private static final int REQUEST_BLUE_TOOTH = 1001;
    private int notificationID;
    private boolean flagShow;
    private BeaconManager beaconManager;
    private BeaconRegion beaconRegion;

    @Override
    protected void onStart() {
        super.onStart();
        MainActivityPermissionsDispatcher.showLocationWithCheck(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        beaconManager.stopRanging(beaconRegion);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconRegion = new BeaconRegion("monitored region",
                UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"),
                11497,
                2311);
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
        BluetoothAdapter.LeScanCallback mLeScanCallback = (device, rssi, scanData) -> {
            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanData, System.currentTimeMillis());
            if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
                IBeaconDevice iBeaconDevice = new IBeaconDevice(deviceLe);
                Log.e(TAG, DOUBLE_TWO_DIGIT_ACCURACY.format(iBeaconDevice.getAccuracy()));
                if (iBeaconDevice.getUUID().equals("b9407f30-f5f8-466e-aff9-25556b57fe6d") &&
                        iBeaconDevice.getMajor() == 11497 &&
                        iBeaconDevice.getMinor() == 2311) {
                    ((TextView) findViewById(R.id.tv_distance)).setText(String.format("distance : %s",
                            DOUBLE_TWO_DIGIT_ACCURACY.format(iBeaconDevice.getAccuracy())));
                    if (iBeaconDevice.getAccuracy() < 1 && !flagShow) {
                        flagShow = true;
                        showNotification("Hello");
                    } else if (iBeaconDevice.getAccuracy() > 2 && flagShow) {
                        flagShow = false;
                        showNotification("Bye bye");
                    }
                }
            }
        };
//
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
//        bluetoothAdapter.startLeScan(mLeScanCallback);

        bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                final BluetoothLeDevice deviceLe = new BluetoothLeDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());
                if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
                    IBeaconDevice iBeaconDevice = new IBeaconDevice(deviceLe);
                    Log.e(TAG, DOUBLE_TWO_DIGIT_ACCURACY.format(iBeaconDevice.getAccuracy()));
                    if (iBeaconDevice.getUUID().equals("b9407f30-f5f8-466e-aff9-25556b57fe6d") &&
                            iBeaconDevice.getMajor() == 11497 &&
                            iBeaconDevice.getMinor() == 2311) {
                        ((TextView) findViewById(R.id.tv_distance)).setText(String.format("distance : %s",
                                DOUBLE_TWO_DIGIT_ACCURACY.format(iBeaconDevice.getAccuracy())));
                        if (iBeaconDevice.getAccuracy() < 1 && !flagShow) {
                            flagShow = true;
                            showNotification("Hello");
                        } else if (iBeaconDevice.getAccuracy() > 2 && flagShow) {
                            flagShow = false;
                            showNotification("Bye bye");
                        }
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        });
//        beaconManager = new BeaconManager(getApplicationContext());
//        beaconManager.setForegroundScanPeriod(200, 200);
//        beaconManager.connect(() -> beaconManager.startMonitoring(beaconRegion));
//        beaconManager.setRangingListener(new BeaconManager.MirrorRangingListener() {
//            @Override
//            public void onMirrorsDiscovered(MirrorRegion mirrorRegion, List<Mirror> list) {
//
//            }
//        });
//        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
//            @Override
//            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> list) {
//                Log.d(TAG, "onEnteredRegion: " + beaconRegion.getIdentifier());
//                String message = "Hello";
//                showNotification(message);
//            }
//
//            @Override
//            public void onExitedRegion(BeaconRegion beaconRegion) {
//                Log.d(TAG, "onExitedRegion: " + beaconRegion.getIdentifier());
//                String message = "Bye bye";
//                showNotification(message);
//            }
//        });
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showRationaleForLocation(final PermissionRequest request) {
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

    private void showNotification(String message) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Beacon Notifications")
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID++, builder.build());
    }
}