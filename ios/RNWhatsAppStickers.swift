//
//  RNWhatsAppStickers.swift
//  RNWhatsAppStickers
//
//  Created by Johannes Sorg on 04.11.18.
//  Copyright © 2018 Facebook. All rights reserved.
//

import Foundation

@objc(RNWhatsAppStickers)
class RNWhatsAppStickers: NSObject {
    var stickerPack: StickerPack!;
    var imageData: ImageData!;
   
    @objc
    func createStickerPack(_ config: NSDictionary,
                           resolver resolve: RCTPromiseResolveBlock,
                           rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            let dataDecoded:NSData = NSData(base64Encoded: RCTConvert.nsString(config["trayImagePNGData"]), options: NSData.Base64DecodingOptions(rawValue: 0))!
            
            stickerPack = try StickerPack(identifier: RCTConvert.nsString(config["identifier"]),
                            name: RCTConvert.nsString(config["name"]),
                            publisher: RCTConvert.nsString(config["publisher"]),
                            trayImagePNGData: dataDecoded as Data,
                            publisherWebsite: RCTConvert.nsString(config["publisherWebsite"]),
                            privacyPolicyWebsite: RCTConvert.nsString(config["privacyPolicyWebsite"]),
                            licenseAgreementWebsite: RCTConvert.nsString(config["licenseAgreementWebsite"]))
            resolve("success")

        } catch {
            NSLog("\(error)")
            reject("RNWhatsAppStickers", "an unknown error occured for whats app stickers", error)
        }
    }
    
    @objc
    func addSticker(_ fileName: String,
                    emojis: Array<String>,
                    resolver resolve: RCTPromiseResolveBlock,
                    rejecter reject: RCTPromiseRejectBlock){
        do {
            try stickerPack.addSticker(contentsOfFile: fileName,
                                   emojis: emojis)
            resolve("success")
        } catch {
            NSLog("\(error)")
            reject("RNWhatsAppStickers", "an unknown error occured for whats app stickers", error)
        }
    }

    @objc
    func addStickerData(_ imageData: String,
                    type: String,
                    emojis: Array<String>,
                    resolver resolve: RCTPromiseResolveBlock,
                    rejecter reject: RCTPromiseRejectBlock){
        do {
            let dataDecoded:NSData = NSData(base64Encoded: imageData, options: NSData.Base64DecodingOptions(rawValue: 0))! 
            let imageType:ImageDataExtension
            if(type == "png"){
                imageType = .png
            }else{
                imageType = .webp
            }
            try stickerPack.addSticker(imageData: dataDecoded as Data,
                                           type: imageType,
                                    emojis: emojis)
            resolve("success")
        } catch {
            NSLog("\(error)")
            reject("RNWhatsAppStickers", "an unknown error occured for whats app stickers", error)
        }
    }
    
    @objc
    func send(_ resolve: @escaping RCTPromiseResolveBlock,
              rejecter reject: @escaping RCTPromiseRejectBlock) {
        stickerPack.sendToWhatsApp { completed in
            if(completed){
                resolve("success")
            }else{
                let error = NSError(domain: "", code: 200, userInfo: nil)
                reject("RNWhatsAppStickers", "an unknown error occured for whats app stickers", error)
            }
        }
    }
    
}
