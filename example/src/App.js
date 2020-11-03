import * as React from 'react';
import { StyleSheet, View, Button, Platform } from 'react-native';
import EspIdfProvisioning from 'react-native-esp-idf-provisioning';
import { request, PERMISSIONS } from 'react-native-permissions';

export default function App() {
  const handleConnect = () => {
    if (Platform.OS === 'android') {
      //no need to connect since in Android
      //create & connect happen in the same function
    } else {
      EspIdfProvisioning.connectDevice(() => {});
    }
  };

  const handleFetchBLEDevices = async () => {
    await request(PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION);
    await request(PERMISSIONS.ANDROID.ACCESS_COARSE_LOCATION);

    EspIdfProvisioning.getBleDevices('PROV_', (devices) => {
      console.log(devices);
      connectBLEDevice();
    });
  };

  const connectBLEDevice = async () => {
    // TODO: Connect device with params
    // EspIdfProvisioning.connectDevice('PROV_D1C5E0', 'BC:DD:C2:D1:C5:E2');
  };

  const handleCreate = async () => {
    let deviceSSID = 'PROV_TEST_SSID';
    let devicePassword = 'mw_prov_password';
    let deviceProofOfPossession = 'abcd1234';

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

  const handleScan = () => {
    EspIdfProvisioning.scanWifiList((error, value) => {
      console.warn({ error, value });
    });
  };

  const handleProvision = () => {
    EspIdfProvisioning.provision(
      'PROV_TEST_LAN_SSID',
      'PROV_TEST_LAN_PASSWORD',
      (error, value) => {
        console.warn({ error, value });
      }
    );
  };

  return (
    <View style={styles.container}>
      <Button title="Fetch BLE Devices" onPress={handleFetchBLEDevices} />
      <Button title="Create Device" onPress={handleCreate} />
      <Button title="Connect" onPress={handleConnect} />
      <Button title="Scan Wifi List" onPress={handleScan} />
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
