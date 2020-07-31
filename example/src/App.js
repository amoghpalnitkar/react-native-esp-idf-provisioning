import * as React from 'react';
import { StyleSheet, View, Button } from 'react-native';
import EspIdfProvisioning from 'react-native-esp-idf-provisioning';

export default function App() {
  const handleConnect = () => {
    EspIdfProvisioning.connectDevice((error, value) => {
      console.warn({error, value});
    });
  };

  const handleCreate = () => {
    let deviceSSID = 'PROV_TEST_SSID';
    let devicePassword = 'mw_prov_password';
    let deviceProofOfPossession = 'abcd1234';

    EspIdfProvisioning.createDevice(
      deviceSSID,
      devicePassword,
      deviceProofOfPossession,
      (error, value) => {
        console.log({ error, value });
      }
    );
  };

  const handleScan = () => {
    EspIdfProvisioning.scanWifiList((error, value) => {
      console.warn({error, value});
    });
  };

  const handleProvision = () => {
    EspIdfProvisioning.provision(
      'PROV_TEST_LAN_SSID',
      'PROV_TEST_LAN_PASSWORD',
      (error, value) => {
        console.warn({error, value});
      },
    );
  };

  return (
    <View style={styles.container}>
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
