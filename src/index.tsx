import { NativeModules } from 'react-native';

type EspIdfProvisioningType = {
  multiply(a: number, b: number): Promise<number>;
};

const { EspIdfProvisioning } = NativeModules;

export default EspIdfProvisioning as EspIdfProvisioningType;
