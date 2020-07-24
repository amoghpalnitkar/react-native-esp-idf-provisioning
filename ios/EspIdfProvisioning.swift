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

    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve(a*b)
    }
    
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

    @objc(scanWifiList:)
    func scanWifiList(successCallback: @escaping RCTResponseSenderBlock) -> Void {
      EspDevice.shared.espDevice?.scanWifiList{ wifiList, _ in
        dump(wifiList)
        successCallback([nil, wifiList])
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
