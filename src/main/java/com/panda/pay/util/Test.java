/*
package com.panda.pay.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

public class Test {
  private static int productId = 2257; // 您的产品id
  private static String productIdKey = "ed7dac86eda5dc5b"; // 在best 平台上注册的全局参数（服务端密钥）
  private static String domainDebug = "http://120.76.101.146:8160"; // 测试环境地址
  private static String domain = "http://in.api.bluepay.tech"; // 正式环境地址
  private static Boolean DEBUG = true;
  private static String transactionId = null;

  public static String getJSONStrFromCommonStr(String common) {
    String[] commons = common.split("&");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < commons.length; i++) {
      String key = commons[i].split("=")[0];
      String value = commons[i].split("=")[1];
      if (i == 0) {
        sb.append("{");
      }
      if (i >= 0) {
        sb.append("\"")
            .append(key)
            .append("\"")
            .append(":")
            .append("\"")
            .append(value)
            .append("\"");
        if (i < commons.length - 1) {
          sb.append(",");
        }
      }
      if (i == commons.length - 1) {
        sb.append("}");
      }
    }
    return sb.toString();
  }

  private static String getAES(String data) throws Exception {
    String dataEncodeFirst = AES.encryptAES(data, productIdKey);
    String dataEncodeSecond = Base64.encode(dataEncodeFirst);
    String dataEncodeLast = URLEncoder.encode(dataEncodeSecond, "UTF-8");
    String dataDecodeLast =
        AES.decryptr(Base64.decode(URLDecoder.decode(dataEncodeLast, "UTF-8")), productIdKey);
    return dataEncodeLast;
  }

  private static String getHandleEncrypt(String dataEncodeLast) {
    String queryString = "productId=" + productId + "&data=" + dataEncodeLast;
    String str = queryString + productIdKey;
    String md5String = MD5Util.getMD5String(str);
    return md5String;
  }

  private static String getQueryEncrypt(String transactionId) {
    String queryString = "productId=" + productId + "&transactionId=" + transactionId;
    String str = queryString + productIdKey;
    String md5String = MD5Util.getMD5String(str);
    return md5String;
  }

  private static String httpGet(String url) {
    System.out.println(url);
    StringBuffer sb = new StringBuffer();
    try {

      URL realUrl = new URL(url);
      URLConnection connection = realUrl.openConnection();
      connection.connect();

      InputStream is = connection.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);

      BufferedReader br = new BufferedReader(isr);

      int len = 0;
      byte[] buffer = new byte[1024];

      String temp = null;
      while ((temp = br.readLine()) != null) {
        sb.append(temp);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      return sb.toString();
    }
  }

  // 取消转账接口尚不可用。
  //    private static void cancelTransfer(){
  //        if (Test.transactionId == null){
  //            System.out.println("TransactionId is null,will exit.");
  //        }
  //        String plainParam = "transactionId="+Test.transactionId+"&productId="+Test.productId;
  //        String params = plainParam+Test.productId+"&encrypt="+MD5Util.getMD5String(plainParam +
  // Test.productIdKey);
  //        String url = (DEBUG ? Test.domainDebug:Test.domain)+
  // "/charge/indonesiaFintechTransfer/cancelTransfer?"+params;
  //        String response = Test.httpGet(url);
  //
  //        System.out.println(response);
  //    }

  public static void main(String[] args) throws Exception {
    Test.transactionId = System.currentTimeMillis() + ""; // 交易id，每笔转账的唯一识别
    String payeeBankName = "BCA"; // 到账用户的银行；
    String payeeName = "Afif Asrif"; // 到账用户的用户名
    String payeeAccount = "8915208823"; // 到账用户的账号
    int transferAmount = 20000; // 转账金额
    String payeeMsisdn = "62123456789"; // 手机号必须是62开头，用户如果输入的是0 开头，需要把0用62替换
    String data =
        "transactionId="
            + transactionId
            + "&promotionId=1000&payeeCountry=ID&payeeBankName="
            + payeeBankName
            + "&payeeName="
            + payeeName
            + "&payeeAccount="
            + payeeAccount
            + "&payeeMsisdn="
            + payeeMsisdn
            + "&payeeType=NORMAL&amount="
            + transferAmount
            + "&currency=IDR";
    System.out.println(data);
    String domain = DEBUG ? Test.domainDebug : Test.domain;
    String url =
        domain
            + "/charge/indonesiaFintechTransfer/transferBalance"
            + "?productId="
            + Test.productId
            + "&data="
            + getAES(data)
            + "&encrypt="
            + getHandleEncrypt(getAES(data));

    String response = Test.httpGet(url);
    System.out.println(response);
    //        System.out.println("please enter 1 confirm transfer request, enter 0 cancel
    // transfer:");
    //        Scanner sn = new Scanner(System.in);
    //        String str=sn.nextLine();
    //        System.out.println(str);
    //        if (str.equals("1")){
    //            System.out.println("the fund will be transfer in 1 day");
    //        }else if(str.equals("0")){
    //            System.out.println("you have cancel the transfer request,Thanks for your
    // support.");
    //            Test.cancelTransfer();
    //        }else{
    //            System.out.println("unkonwn command");
    //        }
  }
}
*/
