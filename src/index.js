import { NativeModules } from 'react-native';
const { EspManager } = NativeModules;

const resolveOrReject = (resolve, reject, error, value) => {
  if (!error) {
    resolve(value);
  } else {
    reject(error);
  }
};

export const createDevice = (name, password, pop) => {
  return new Promise((resolve, reject) => {
    EspManager.createDevice(name, password, pop, (error, value) => {
      resolveOrReject(resolve, reject, error, value);
    });
  });
};

export const connectDevice = () => {
  return new Promise((resolve, reject) => {
    EspManager.connectDevice((error, value) => {
      console.log({ error, value });
      resolveOrReject(resolve, reject, error, value);
    });
  });
};

export const scanWifiList = () => {
  return new Promise((resolve, reject) => {
    EspManager.scanWifiList((error, value) => {
      resolveOrReject(resolve, reject, error, value);
    });
  });
};

export const provision = (ssid, password) => {
  return new Promise((resolve, reject) => {
    EspManager.provision(ssid, password, (error, value) => {
      resolveOrReject(resolve, reject, error, value);
    });
  });
};
