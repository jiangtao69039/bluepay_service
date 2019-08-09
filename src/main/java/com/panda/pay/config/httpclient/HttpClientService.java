/*
package com.panda.pay.config.httpclient;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

*/
/**
 * @Title:HttpClientService @Copyright: Copyright (c) 2016 @Description: <br>
 * @Company: panda-fintech @Created on 2018/6/5下午8:12
 *
 * @miaoxuehui@panda-fintech.com
 *//*

   @Component
   public class HttpClientService {
     @Autowired private CloseableHttpClient httpClient;
     @Autowired private RequestConfig config;

     public String doGet(String url) throws Exception {
       HttpGet httpGet = new HttpGet(url);
       try {
         // 装载配置信息
         httpGet.setConfig(config);
         // 发起请求
         CloseableHttpResponse response = this.httpClient.execute(httpGet);
         return EntityUtils.toString(response.getEntity(), "UTF-8");
       } catch (Exception e) {
         throw e;
       } finally {
         httpGet.reset();
       }
     }
   }
   */
