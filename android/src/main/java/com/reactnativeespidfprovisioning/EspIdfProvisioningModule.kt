package com.reactnativeespidfprovisioning

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.core.app.ActivityCompat
import android.util.Log

import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPDevice
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.WiFiAccessPoint
import com.espressif.provisioning.listeners.ProvisionListener
import com.espressif.provisioning.listeners.WiFiScanListener
import com.espressif.provisioning.listeners.BleScanListener
import com.espressif.provisioning.DeviceConnectionEvent

import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.bridge.*
import java.lang.Exception
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class EspIdfProvisioningModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    val foundBLEDevices = HashMap<String, BluetoothDevice>();

    init {
      EventBus.getDefault().register(this);
    }

    override fun getName(): String {
        return "EspIdfProvisioning"
    }

    // Searches for BLE devices with a name starting with the given prefix.
    // The prefix must match the string in '/main/app_main.c'
    // Resolves to an array of BLE devices
    @ReactMethod
    fun getBleDevices(prefix: String, promise: Promise) {
      Log.e("ESPProvisioning", "getBleDevices")

      if (ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        promise.reject("Location Permission denied")
        return;
      }

      // Search for BLE devices
      ESPProvisionManager.getInstance(reactApplicationContext).searchBleEspDevices(prefix, object : BleScanListener {
        override fun scanStartFailed() {
          promise.reject("Scan start failed")
        }

        override fun onPeripheralFound(device: BluetoothDevice, scanResult: ScanResult) {
          foundBLEDevices[device.address] = device;
        }

        override fun scanCompleted() {
          val result = WritableNativeArray();

          foundBLEDevices.keys.forEach {
            val device: WritableMap = Arguments.createMap();
            device.putString("address", it);
            device.putString("name", foundBLEDevices[it]?.name);
            result.pushMap(device)
          }

          // Return found BLE devices
          promise.resolve(result);
        }

        override fun onFailure(p0: Exception?) {
          promise.reject(p0.toString())
        }
      });
    }

    // Send event to JS
    fun sendEvent(name: String, value: String) {
      reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(name, value);
    }

    // Subscribe on a DeviceConnectionEvent to get more info about the device connection.
    @Subscribe
    fun onConnectionEvent(event: DeviceConnectionEvent) {
      Log.e("ESPProvisioning", "DeviceConnectionEvent")

      when (event.getEventType()) {
        ESPConstants.EVENT_DEVICE_CONNECTED -> sendEvent("DeviceConnectionEvent", "EVENT_DEVICE_CONNECTED")
        ESPConstants.EVENT_DEVICE_DISCONNECTED -> sendEvent("DeviceConnectionEvent", "EVENT_DEVICE_DISCONNECTED")
        ESPConstants.EVENT_DEVICE_CONNECTION_FAILED -> sendEvent("DeviceConnectionEvent", "EVENT_DEVICE_CONNECTION_FAILED")
      }
    }

    // Connects to a BLE device
    // We need the Service UUID from the config.service_uuid in app_prov.c
    // We need the proof of possestion (pop) specified in '/main/app_main.c'
    // The deviceAddress is the address we got from the "searchBleEspDevices" function
    // Resolves when connected to device
    @ReactMethod
    fun connectBleDevice(deviceAddress: String, deviceProofOfPossession: String, promise: Promise) {
      Log.e("ESPProvisioning", "connectBleDevice")
      if (ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        promise.reject("Location Permission denied");
        return;
      }

      // SECURITY_0 is plain text communication.
      // SECURITY_1 is encrypted.
      // This must match 'wifi_prov_security_t security' in app_main.c
      val esp : ESPDevice = ESPProvisionManager.getInstance(reactApplicationContext).createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_0);
      esp.proofOfPossession = deviceProofOfPossession

      // TODO(wdm) What is the name of this service, and therefore what should this const be called?
      val SERVICE_UUID = "021a9004-0382-4aea-bff4-6b3f1c5adfb4"; // See config.service_uuid in app_prov.c

      val device = foundBLEDevices[deviceAddress]
      if (device == null) {
        promise.reject("Invalid address $deviceAddress")
        return;
      }

      esp.connectBLEDevice(device, SERVICE_UUID);

      promise.resolve(esp.deviceName);
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
    fun scanWifiList(promise: Promise) {
      val device = ESPProvisionManager.getInstance(reactApplicationContext).espDevice

      if(device == null) {
        promise.reject("No device found")
        return;
      }

      device.scanNetworks(object: WiFiScanListener {
        override fun onWifiListReceived(wifiList: java.util.ArrayList<WiFiAccessPoint>?) {
          val result = WritableNativeArray();
          wifiList?.forEach {
            result.pushString(it.wifiName)
          }
          promise.resolve(result)
        }

        override fun onWiFiScanFailed(p0: java.lang.Exception?) {
          promise.reject("Failed to get Wi-Fi scan list")
        }
      })
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
