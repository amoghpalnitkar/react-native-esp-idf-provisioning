#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(EspIdfProvisioning, NSObject)

RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(addEventSwift:(NSString *)name location:(NSString *)location)
RCT_EXTERN_METHOD(createDevice:(NSString *)deviceName
                  devicePassword:(NSString *)devicePassword
                  deviceProofOfPossession:(NSString *)deviceProofOfPossession
                  successCallback:(RCTResponseSenderBlock *)successCallback)
RCT_EXTERN_METHOD(connectDevice:(RCTResponseSenderBlock *)successCallback)
RCT_EXTERN_METHOD(scanWifiList:(RCTResponseSenderBlock *)successCallback)
RCT_EXTERN_METHOD(provision:(NSString *)ssid passPhrase:(NSString *)passPhrase successCallback:(RCTResponseSenderBlock *)successCallback)

+ (BOOL) requiresMainQueueSetup {
  return YES;
}

@end
