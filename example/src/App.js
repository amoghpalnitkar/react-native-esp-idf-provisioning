import * as React from 'react';
import {
  StyleSheet,
  View,
  Button,
  Platform,
  NativeEventEmitter,
  NativeModules,
} from 'react-native';
import EspIdfProvisioning from 'react-native-esp-idf-provisioning';
import { request, PERMISSIONS } from 'react-native-permissions';

const EspIdfProvisioningModule = NativeModules.EspIdfProvisioning;
const deviceProofOfPossession = 'abcd1234';

export default function App() {
  let foundBLEDevices = [];

  const handleConnect = () => {
    if (Platform.OS === 'android') {
      //no need to connect since in Android
      //create & connect happen in the same function
    } else {
      EspIdfProvisioning.connectDevice();
    }
  };

  const handleGetBleDevices = async () => {
    console.log('GetBLEDevices: Start');
    if (Platform.OS === 'android') {
      await request(PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION);
    } else {
      await request(PERMISSIONS.IOS.LOCATION_WHEN_IN_USE);
    }

    try {
      const devices = await EspIdfProvisioning.getBleDevices('PROV_');
      if (devices.length > 0) {
        console.log('GetBLEDevices: found devices:', devices);
        foundBLEDevices = devices;
      } else {
        console.log('GetBLEDevices: No devices found');
      }
    } catch (error) {
      console.log('GetBLEDevices: ' + error);
    }
  };

  const handleConnectBleDevice = async () => {
    const espIdfProvisioningEmitter = new NativeEventEmitter(
      EspIdfProvisioningModule
    );
    espIdfProvisioningEmitter.addListener('DeviceConnectionEvent', function (
      event
    ) {
      console.log('DeviceConnectionEvent');
      console.log(event);

      espIdfProvisioningEmitter.removeListener('DeviceConnectionEvent');
    });

    console.log('handleConnectBleDevice: start connection');
    if (foundBLEDevices.length === 0) {
      console.log(
        "handleConnectBleDevice: Can't connect because there are no devices found"
      );
      return;
    }

    try {
      const connectedDevice = await EspIdfProvisioning.connectBleDevice(
        foundBLEDevices[0].address,
        deviceProofOfPossession
      ); // For testing we use the first matching device.
      console.log(
        'handleConnectBleDevice: Connection started to: ' + connectedDevice
      );
    } catch (error) {
      console.log('handleConnectBleDevice: Connection failed: ' + error);
    }
  };

  const handleCreate = async () => {
    let deviceSSID = 'PROV_TEST_SSID';
    let devicePassword = 'mw_prov_password';

    try {
      if (Platform.OS === 'android') {
        await request(PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION);
      } else {
        await request(PERMISSIONS.IOS.LOCATION_WHEN_IN_USE);
      }
      EspIdfProvisioning.createDevice(
        deviceSSID,
        devicePassword,
        deviceProofOfPossession,
        (error, value) => {
          console.log({ error, value });
        }
      );
    } catch (error) {
      alert('Location permisson denied');
    }
  };

  const handleScanWifi = async () => {
    console.log('handleScanWifi: start');
    try {
      const foundNetworks = await EspIdfProvisioning.scanWifiList();
      console.log('handleScanWifi: Found Networks: ' + foundNetworks);
    } catch (error) {
      console.log('handleScanWifi: ' + error);
    }
  };

  const handleProvision = async () => {
    try {
      const value = EspIdfProvisioning.provision(
        'PROV_TEST_LAN_SSID',
        'PROV_TEST_LAN_PASSWORD'
      );
      console.log('handleProvision: ' + value);
    } catch (error) {
      console.log('handleProvision: ' + error);
    }
  };

  return (
    <View style={styles.container}>
      <Button title="Get BLE Devices" onPress={handleGetBleDevices} />
      <Button title="Connect BLE Device" onPress={handleConnectBleDevice} />
      <Button title="Scan Wifi List" onPress={handleScanWifi} />
      <Button title="Create Device" onPress={handleCreate} />
      <Button title="Connect" onPress={handleConnect} />
      <Button title="Provision" onPress={handleProvision} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
