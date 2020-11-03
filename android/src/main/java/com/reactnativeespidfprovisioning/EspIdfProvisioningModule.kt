package com.reactnativeespidfprovisioning

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import android.util.Log;
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.core.app.ActivityCompat

import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPDevice
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.WiFiAccessPoint
import com.espressif.provisioning.listeners.ProvisionListener
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.listeners.WiFiScanListener
import com.espressif.provisioning.listeners.BleScanListener
import com.espressif.provisioning.transport.BLETransport

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.facebook.react.bridge.*
import java.lang.Exception
import java.util.ArrayList
import com.google.protobuf.InvalidProtocolBufferException;


class EspIdfProvisioningModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "EspIdfProvisioning"
    }

    // Example method
    // See https://facebook.github.io/react-native/docs/native-modules-android
    @ReactMethod
    fun multiply(a: Int, b: Int, promise: Promise) {

      promise.resolve(a * b)
    }

    @ReactMethod
    fun getBleDevices(prefix: String, callback: Callback) {
      Log.e("Jochem", "===== getBleDevices =====");
      val deviceList: ArrayList<BluetoothDevice> = arrayListOf<BluetoothDevice>();

      if (ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        ESPProvisionManager.getInstance(reactApplicationContext).searchBleEspDevices(prefix, object: BleScanListener {
            override fun scanStartFailed() {
              callback.invoke("Scan start failed")
            }

            override fun onPeripheralFound(device: BluetoothDevice, scanResult: ScanResult) {
              Log.e("Jochem", "onPeripheralFound");
              val deviceExists = false;
              var serviceUuid = "";

              if (scanResult.getScanRecord().getServiceUuids() != null && scanResult.getScanRecord().getServiceUuids().size > 0) {
                  serviceUuid = scanResult.getScanRecord().getServiceUuids().get(0).toString();
              }

              // if (bluetoothDevices.containsKey(device)) {
              //     deviceExists = true;
              // }


              if (!deviceExists) {
                // bluetoothDevices.put(device, serviceUuid);
                Log.e("Jochem", "DeviceFound");
                deviceList.add(device);
                connectDevice(device, serviceUuid);
              }
            }

            override fun scanCompleted() {
              Log.e("Jochem", "BLEscanCompleted");

              val listData = deviceList.joinToString()
              callback.invoke(listData);
            }

            override fun onFailure(p0: Exception?) {
              callback.invoke("Scan failure")
            }
        });
      } else {
        Toast.makeText(reactApplicationContext, "Location permission denied", Toast.LENGTH_SHORT).show()
      }
    }

    @ReactMethod
    fun connectDevice(device: BluetoothDevice, uuid: String) {
      val esp : ESPDevice = ESPProvisionManager.getInstance(reactApplicationContext).createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1);
      esp.proofOfPossession = "abcd1234";

      Log.e("Jochem", "connectDevice");

      if (ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        esp.connectBLEDevice(device, uuid);

        Log.e("Jochem", "scanNetworks");
        Log.e("Jochem", device.toString());
        Log.e("Jochem", esp.toString());

      //  val listener: WiFiScanListener = WiFiScanListener()
        esp.scanNetworks(object: WiFiScanListener {
          override fun onWifiListReceived(wifiList: java.util.ArrayList<WiFiAccessPoint>?) {
            Log.e("Jochem", "onWifiListReceived");
          }

          override fun onWiFiScanFailed(p0: java.lang.Exception?) {
            Log.e("Jochem", p0.toString());
          }
        })
      }
    }

    @ReactMethod
    fun createDevice(ssid: String, password: String, devicePop: String,
                     callback: Callback ) {
      val device : ESPDevice = ESPProvisionManager.getInstance(reactApplicationContext).createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_0)
      device.proofOfPossession = devicePop
      if (ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        device.connectWiFiDevice(ssid, password)
        callback.invoke("success")
      } else {
        Toast.makeText(reactApplicationContext, "Location permission denied", Toast.LENGTH_SHORT).show()
      }
    }

    @ReactMethod
    fun scanWifiList(callback: Callback) {
      val device = ESPProvisionManager.getInstance(reactApplicationContext).espDevice
      if(device != null) {
//        val listener: WiFiScanListener = WiFiScanListener()
        device.scanNetworks(object: WiFiScanListener {
          override fun onWifiListReceived(wifiList: java.util.ArrayList<WiFiAccessPoint>?) {
            callback.invoke(wifiList)
          }

          override fun onWiFiScanFailed(p0: java.lang.Exception?) {
            Toast.makeText(reactApplicationContext, "Failed to get Wi-Fi scan list", Toast.LENGTH_LONG).show();
          }
        })
      } else {
        Toast.makeText(reactApplicationContext, "No device found", Toast.LENGTH_LONG).show();
      }
    }

    @ReactMethod
    fun provision(ssid: String, password: String, callback: Callback) {
      val device = ESPProvisionManager.getInstance(reactApplicationContext).espDevice
      if(device != null) {
        device.provision(ssid, password, object: ProvisionListener {
          override fun wifiConfigApplyFailed(p0: Exception?) {
            TODO("Not yet implemented")
          }

          override fun wifiConfigApplied() {
            TODO("Not yet implemented")
          }

          override fun onProvisioningFailed(p0: Exception?) {
            Toast.makeText(reactApplicationContext, p0?.message, Toast.LENGTH_LONG).show();
            callback.invoke("error")
          }

          override fun deviceProvisioningSuccess() {
            callback.invoke("success")
          }

          override fun createSessionFailed(p0: Exception?) {
            Toast.makeText(reactApplicationContext, p0?.message, Toast.LENGTH_LONG).show();
          }

          override fun wifiConfigFailed(p0: Exception?) {
            Toast.makeText(reactApplicationContext, p0?.message, Toast.LENGTH_LONG).show();
          }

          override fun provisioningFailedFromDevice(p0: ESPConstants.ProvisionFailureReason?) {
            Toast.makeText(reactApplicationContext, p0?.name, Toast.LENGTH_LONG).show();
          }

          override fun wifiConfigSent() {
            //todo implement
          }
        })
      }
    }
}
