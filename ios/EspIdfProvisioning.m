#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(EspIdfProvisioning, NSObject)

RCT_EXTERN_METHOD(getBleDevices:(NSString *)prefix
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(connectBleDevice:(NSString *)deviceAddress
                 deviceProofOfPossession:(NSString *)deviceProofOfPossession
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(createDevice:(NSString *)deviceName
                  devicePassword:(NSString *)devicePassword
                  deviceProofOfPossession:(NSString *)deviceProofOfPossession
                  successCallback:(RCTResponseSenderBlock *)successCallback)
RCT_EXTERN_METHOD(connectDevice:(RCTResponseSenderBlock *)successCallback)
RCT_EXTERN_METHOD(scanWifiList:
  (RCTPromiseResolveBlock)resolve
  withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(provision:(NSString *)ssid
                passPhrase:(NSString *)passPhrase
                withResolver:(RCTPromiseResolveBlock)resolve
                withRejecter:(RCTPromiseRejectBlock)reject)
)

+ (BOOL) requiresMainQueueSetup {
  return YES;
}

@end
