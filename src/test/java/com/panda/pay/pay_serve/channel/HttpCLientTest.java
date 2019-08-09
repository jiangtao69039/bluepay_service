package com.panda.pay.pay_serve.channel;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

/** Created by jiangtao on 19-2-14 下午5:59 */
public class HttpCLientTest {

  @Test
  public void test() throws Exception {

    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet get =
        new HttpGet("http://47.104.109.123:84/bluepay-service/callback/testproxy?name=%3D");
    httpClient.execute(get);
  }
}
