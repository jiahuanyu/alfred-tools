//
//  main.m
//  alfred-cmc
//
//  Created by vendor on 2018/5/11.
//  Copyright © 2018年 vendor. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Cocoa/Cocoa.h>

int main(int argc, const char * argv[]) {
    @autoreleasepool {
        NSPasteboard *pasteboard = [NSPasteboard generalPasteboard];
        NSArray *types = [pasteboard types];
        if ([types containsObject:NSPasteboardTypePNG]) {
            NSData *pndData = [pasteboard dataForType:NSPasteboardTypePNG];
    
            NSString *endString = @"\r\n";
            NSString *twoHyphen = @"--";
            NSString *boundary = @"----WebKitFormBoundary123456789";
            
            NSMutableData *body = [NSMutableData data];
            
            NSString *urlString = @"https://sm.ms/api/upload";
            // 创建URL
            NSURL *url = [NSURL URLWithString:urlString];
            // 创建请求
            NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
            // 设置请求方法（默认就是GET请求）
            [request setHTTPMethod:@"POST"];
            
            [request setValue:[NSString stringWithFormat:@"multipart/form-data; boundary=%@", boundary]  forHTTPHeaderField:@"Content-Type"];
            
            
            [body appendData:[ [NSString stringWithFormat:@"%@%@%@", twoHyphen, boundary,endString] dataUsingEncoding:NSUTF8StringEncoding] ];
            [body appendData:[ [NSString stringWithFormat:@"Content-Disposition:form-data; name=\"smfile\"; filename=\"upload.png\"%@",endString] dataUsingEncoding:NSUTF8StringEncoding] ];
            [body appendData:[ [NSString stringWithFormat:@"%@",endString] dataUsingEncoding:NSUTF8StringEncoding] ];
            
            [body appendData:pndData];
            [body appendData:[ [NSString stringWithFormat:@"%@",endString] dataUsingEncoding:NSUTF8StringEncoding] ];
            
            [body appendData:[ [NSString stringWithFormat:@"%@%@%@%@",twoHyphen, boundary,twoHyphen,endString] dataUsingEncoding:NSUTF8StringEncoding] ];
            
            
            [request setHTTPBody:body];
            
            NSData *resultData =  [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:nil];
            NSLog(@"resultData = %@",[[NSString alloc] initWithData:resultData encoding:NSUTF8StringEncoding]);
            NSDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:resultData options:kNilOptions error:nil];
            NSDictionary *dataDict = [jsonDict objectForKey:@"data"];
            NSLog(@"resultData = %@", [dataDict objectForKey:@"url"]);
            NSString *pngURL = [NSString stringWithFormat:@"![](%@)", [dataDict objectForKey:@"url"]];
  
            [pasteboard clearContents];
            [pasteboard writeObjects:@[pngURL]];
        }
        
    }
    return 0;
}
