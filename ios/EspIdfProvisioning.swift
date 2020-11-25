import Alamofire
import ESPProvision

class EspDevice {
  static let shared = EspDevice()
  var espDevice: ESPDevice?
  func setDevice(device: ESPDevice) {
    self.espDevice = device
  }
}


@objc(EspIdfProvisioning)
class EspIdfProvisioning: NSObject {
    var bleDevices:[ESPDevice]?

    @objc(createDevice:devicePassword:deviceProofOfPossession:successCallback:)
    func createDevice(_ deviceName: String, devicePassword: String, deviceProofOfPossession: String, successCallback: @escaping RCTResponseSenderBlock) -> Void {
      ESPProvisionManager.shared.createESPDevice(
          deviceName: deviceName,
          transport: ESPTransport.softap,
          security: ESPSecurity.secure,
          proofOfPossession: deviceProofOfPossession,
          softAPPassword: devicePassword
      ){ espDevice, _ in
          dump(espDevice)
          EspDevice.shared.setDevice(device: espDevice!)
          successCallback([nil, "success"])
      }

    }

    // Searches for BLE devices with a name starting with the given prefix.
    // The prefix must match the string in '/main/app_main.c'
    // Resolves to an array of BLE devices
    @objc(getBleDevices:withResolver:withRejecter:)
    func getBleDevices(prefix: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
      // Check permissions

      // Search for BLE devices (ESPProvisionManager.searchESPDevices())
      ESPProvisionManager.shared.searchESPDevices(devicePrefix:prefix, transport:.ble) { bleDevices, error in
        DispatchQueue.main.async {
          if bleDevices == nil {
            let error = NSError(domain: "getBleDevices", code: 404, userInfo: [NSLocalizedDescriptionKey : "No devices found"])
            reject("404", "getBleDevices", error)

            return
          }

          // TODO: We only return the name of the devices. Do we want to return more (MAC address, RSSI...)?
          let deviceNames = bleDevices!.map {[
            "name": $0.name,
            "address": $0.name
          ]}
          // Return found BLE device names
          resolve(deviceNames)
        }
      }
    }

    // Connects to a BLE device
    // We need the Service UUID from the config.service_uuid in app_prov.c
    // We need the proof of possestion (pop) specified in '/main/app_main.c'
    // The deviceAddress is the address we got from the "getBleDevices" function
    // Resolves when connected to device
    @objc(connectBleDevice:deviceProofOfPossession:withResolver:withRejecter:)
    func connectBleDevice(deviceAddress: String, deviceProofOfPossession: String, resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
      // TODO: make security level a parameter.
      ESPProvisionManager.shared.createESPDevice(deviceName: deviceAddress, transport: .ble, security: .unsecure, proofOfPossession: deviceProofOfPossession, completionHandler: { device, _ in
          if device == nil {
            let error = NSError(domain: "connectBleDevice", code: 400, userInfo: [NSLocalizedDescriptionKey : "Device not found"])
            reject("400", "Device not found", error)

            return
          }

          let espDevice: ESPDevice = device!
          EspDevice.shared.setDevice(device: espDevice)

          // TODO: Add event when the device disconnect
          espDevice.connect(completionHandler: { status in
            print(status)

            switch status {
              case .connected:
                  resolve(status)
              case let .failedToConnect(error):
                  reject("400", "Failed to connect", error)
              default:
                let error = NSError(domain: "connectBleDevice", code: 400, userInfo: [NSLocalizedDescriptionKey : "Default connection error"])
                reject("400", "Default connection error", error)
            }
          })
      })
    }

    @objc(connectDevice:)
    func connectDevice(successCallback: @escaping RCTResponseSenderBlock) -> Void {
      var completedFlag = false
      EspDevice.shared.espDevice?.connect(completionHandler: { status in
        dump(status)
        if(!completedFlag) {
          completedFlag = true
          switch(status) {
          case .connected:
            successCallback([nil, "connected"])
          case .failedToConnect(_):
            successCallback(["failed_to_connect", nil])
          case .disconnected:
            successCallback(["disconnected", nil])
          @unknown default:
            successCallback([status, nil])
          }
        }
      })
    }

    @objc(scanWifiList:withRejecter:)
    func scanWifiList(resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
      EspDevice.shared.espDevice?.scanWifiList{ wifiList, _ in
        let ssids = wifiList!.map { $0.ssid }
        resolve(ssids)
      }
    }

    @objc(provision:passPhrase:successCallback:)
    func provision(ssid: String, passPhrase: String, successCallback: @escaping RCTResponseSenderBlock) -> Void {
      var completedFlag = false
      EspDevice.shared.espDevice?.provision(ssid: ssid, passPhrase: passPhrase, completionHandler: {
        status in
        dump(status)
        if(!completedFlag) {
          completedFlag = true
          successCallback([nil, status])
        }
      })
    }
}
