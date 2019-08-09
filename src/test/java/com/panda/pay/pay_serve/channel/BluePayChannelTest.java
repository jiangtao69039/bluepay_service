package com.panda.pay.pay_serve.channel;

import com.alibaba.fastjson.JSONObject;
import com.panda.pay.pay_serve.SpringBaseTest;
import com.panda.pay.service.channel.bluepay.BluePayChannelImpl;
import com.panda.pay.util.MD5Util;
import com.panda.pay.util.UUIDUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.web.client.RestTemplate;

/** Created by jiangtao on 19-1-27 下午12:33 */
public class BluePayChannelTest extends SpringBaseTest {

  @Autowired BluePayChannelImpl bluePayChannel;

  @Autowired RestTemplate restTemplate;

  /**
   * 增加账户储备金,然后用来测试放款
   *
   * @throws Exception
   */
  @Test
  @Rollback(false)
  public void chubei() throws Exception {
    JSONObject paramJson = new JSONObject();
    paramJson.put("price", 10000000);
    paramJson.put("currency", "IDR");
    paramJson.put("notifyUrl", "http://www.baidu.com");
    paramJson.put("notifyType", "http");
    paramJson.put("nation", "ID");
    paramJson.put("mobile", "082197662283");
    paramJson.put("outTradeNo", UUIDUtils.getUUID32());
    paramJson.put("channelCode", "bluepay");
    paramJson.put("channelBizCode", "gather_dynamic_VA_bluepay");

    JSONObject channelSpecialParamsJson = new JSONObject();
    channelSpecialParamsJson.put("payType", "atm");
    channelSpecialParamsJson.put("bankType", "permata");
    channelSpecialParamsJson.put("productId", "2257");

    paramJson.put("channelSpecialParams", channelSpecialParamsJson);

    HttpHeaders headers = new HttpHeaders();
    MediaType type = MediaType.APPLICATION_JSON_UTF8;
    headers.setContentType(type);
    HttpEntity<JSONObject> entity = new HttpEntity<>(paramJson, headers);

    String res =
        restTemplate
            .postForEntity("http://127.0.0.1:8670/bluepay-service/pay/gather", entity, String.class)
            .getBody();

    System.out.println(res);

    String paymentCode =
        JSONObject.parseObject(res)
            .getJSONObject("data")
            .getJSONObject("bizParams")
            .getString("paymentCode");

    String shoukuanUrl =
        "http://120.76.101.146:21921/indonesia/express/gather/dr?price=1000000&paymentCode="
            + paymentCode
            + "&bankType=permata&cardNo=56994147426768475421&provider=atm";

    String res2 = restTemplate.getForEntity(shoukuanUrl, String.class).getBody();
    System.out.println(res2);
  }

  /**
   * 测试统一收款接口
   *
   * @throws Exception
   */
  @Test
  @Rollback(false)
  public void testUnifyGather() throws Exception {

    JSONObject paramJson = new JSONObject();
    paramJson.put("price", 20000);
    paramJson.put("currency", "IDR");
    paramJson.put("notifyUrl", "http://47.104.109.123:84/bluepay-service/callback/testNotify");
    paramJson.put("notifyType", "http");
    paramJson.put("nation", "ID");
    paramJson.put("mobile", "082197662283");
    paramJson.put("outTradeNo", UUIDUtils.getUUID32());
    paramJson.put("channelCode", "bluepay");
    paramJson.put("channelBizCode", "gather_dynamic_VA_bluepay");

    JSONObject channelSpecialParamsJson = new JSONObject();
    channelSpecialParamsJson.put("payType", "atm");
    channelSpecialParamsJson.put("bankType", "permata");
    channelSpecialParamsJson.put("productId", "2257");

    paramJson.put("channelSpecialParams", channelSpecialParamsJson);

    System.out.println(paramJson.toJSONString());
    HttpHeaders headers = new HttpHeaders();
    MediaType type = MediaType.APPLICATION_JSON_UTF8;
    headers.setContentType(type);
    HttpEntity<JSONObject> entity = new HttpEntity<>(paramJson, headers);

    /*String res =
        restTemplate
            .postForEntity("http://127.0.0.1:8670/bluepay-service/pay/gather", entity, String.class)
            .getBody();

    System.out.println(res);*/
  }

  /**
   * 测试统一放款接口
   *
   * @throws Exception
   */
  @Test
  @Rollback(false)
  public void testUnifyLoan() throws Exception {

    JSONObject paramJson = new JSONObject();
    paramJson.put("price", 20000);
    paramJson.put("currency", "IDR");
    paramJson.put("notifyUrl", "http://47.104.109.123:84/bluepay-service/callback/testNotify");
    paramJson.put("notifyType", "http");
    paramJson.put("nation", "ID");
    paramJson.put("mobile", "082197662283");
    paramJson.put("outTradeNo", UUIDUtils.getUUID32());
    paramJson.put("channelCode", "bluepay");
    paramJson.put("channelBizCode", "loan_fintech_bluepay");

    JSONObject channelSpecialParamsJson = new JSONObject();
    channelSpecialParamsJson.put("payeeBankName", "ARTA GRAHA");
    channelSpecialParamsJson.put("payeeName", "Afif Asrif");
    channelSpecialParamsJson.put("payeeAccount", "1077098557");
    channelSpecialParamsJson.put("payeeType", "NORMAL");
    channelSpecialParamsJson.put("productId", "2257");
    // channelSpecialParamsJson.put("payeeCountry", "ID");

    paramJson.put("channelSpecialParams", channelSpecialParamsJson);
    System.out.println(paramJson.toJSONString());
    HttpHeaders headers = new HttpHeaders();
    MediaType type = MediaType.APPLICATION_JSON_UTF8;
    headers.setContentType(type);
    HttpEntity<JSONObject> entity = new HttpEntity<>(paramJson, headers);

    /*String res =
        restTemplate
            .postForEntity("http://127.0.0.1:8670/bluepay-service/pay/loan", entity, String.class)
            .getBody();

    System.out.println(res);*/
  }

  public boolean vaildEncrypt(String queryString, String encrypt) {
    queryString = queryString.substring(0, queryString.indexOf("encrypt"));
    queryString = queryString.substring(0, queryString.length() - 1); // 去掉最后的"&"
    queryString = queryString + "ed7dac86eda5dc5b";
    String md5String = MD5Util.getMD5String(queryString);
    return md5String.equals(encrypt);
  }

  @Test
  public void testEncrypt() {
    String query =
        "productId=2257&data=MUVGNEM5N0NCRjUxM0IyQ0Q2RTBEMDNCMUNDQzBCQzFERTMyNTdENjVDRTIxRTEyNEY3MzMyM0Y5NkNDNTI3MkFCNzQyRkE2NTk4QjExRkE4MjA4M0FFNTM2NDhEN0VENzU5RTQ3NUIyNkRBREYxOENDRTkwRUExNUIxMTQwOTZCNTczODEyODNGOTI3QjAyMjFDM0NGREQyMTQwRDcyQjQyOUJBMjNBMjM3MjRFM0FGMzgxQTg1MkE4RjEwQTUzMDlGMUUxM0Y0ODlBQjU4RkEzMTAzOUQ2QzYyMUQ2NjYxRERGQzMwMzQ0N0VCQjJGM0ZBMEQwMDI2OTAzQkJCRUNGREE5NTgzRkIxQTQ1MkVFRUM5QjNBNDZGMzA2NkM3QjJEODdCQTVEQjM2MkIwOThENTAyRTlBNzc5RjEwN0VBMjNDQzZEQkFFRjExQTc1ODUyNjExOUE1MTg3MkZBMEZGM0M1QTZCOTYwODZFQTA5MkUxODUzRUM0QkQ3RkRCNTMxRThDQzdCQTU5MkQwQUEzMjU5MzQ1RkUwMEI5OUY%3D&encrypt=caeacfdde40dad7bbbdfa9ec7b2c3fbe";

    String en = "caeacfdde40dad7bbbdfa9ec7b2c3fbe";
    boolean f = this.vaildEncrypt(query, en);
    System.out.println(f);
  }

  @Test
  public void testrst() {
    restTemplate.getForEntity(
        "http://47.104.109.123:84/bluepay-service/callback/testproxy?na=me=AAA%3D", String.class);
  }
}
