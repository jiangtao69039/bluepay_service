package com.panda.pay.controller.callback;

import com.alibaba.fastjson.JSONObject;
import com.panda.pay.constant.ChannelConstant;
import com.panda.pay.controller.params.BluePayCallBackParams;
import com.panda.pay.framework.CallbackResult;
import com.panda.pay.service.callback.BluePayCallBackHandle;
import com.panda.pay.util.MD5Util;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Copyright [2019] [jiangtao of copyright owner]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
/** Created by jiangtao on 19-1-28 下午3:36 */
@Controller
@RequestMapping("/callback")
public class CallBackController {

  private Logger logger = LoggerFactory.getLogger(CallBackController.class);

  @Value("${thirdparty.bluepay.productIdKey}")
  private String productIdKey;

  @Autowired private RedisTemplate<String, String> stringRedisTemplate;
  @Autowired private BluePayCallBackHandle bluePayCallBackHandle;

  /**
   * 目前未使用 接收bluepay的回调通知,立即将通知存入sqs并返回响应
   *
   * @param params
   * @param request
   * @return
   */
  @ResponseBody
  @RequestMapping(value = "/bluepay/tosqs", method = RequestMethod.GET)
  public String bluepayCallback2Sqs(BluePayCallBackParams params, HttpServletRequest request) {
    // 目前没有实现
    String queryString = request.getQueryString();
    // 将queryString放入sqs
    JSONObject j = new JSONObject();
    j.put("bluePayCallBackParams", JSONObject.parseObject(JSONObject.toJSONString(params)));
    j.put("queryString", queryString);
    // 将sqsmsg放入sqs中进行延时处理,目前没有实现
    return j.toJSONString();
  }
  /**
   * bluepay回调信息处理
   *
   * @param params
   * @param request
   * @return
   */
  @ResponseBody
  @RequestMapping(value = "/bluepay/handle", method = RequestMethod.GET)
  public String bluepayCallbackHandle(BluePayCallBackParams params, HttpServletRequest request) {

    String queryString = request.getQueryString();
    String transactionCode = this.getTransactionCodeFromQUeryString(queryString);
    int count = 0;
    // bluepay会立即回调一次,防止回调更新在 下单更新之前(下单更新时会添加key,更新结束删除key)
    while (stringRedisTemplate.hasKey(transactionCode)) {
      try {
        Thread.sleep(ChannelConstant.SLEEP_TIME);
      } catch (InterruptedException e) {
        logger.error("InterruptedException", e);
        e.printStackTrace();
      }
      count++;
      if (count > ChannelConstant.REDIS_RETRY_COUNT) {
        break;
      }
    }
    try {
      logger.info(
          "产品回调(bluepay):{}",
          Base64.getEncoder().encode(queryString.getBytes(StandardCharsets.UTF_8.name())));
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("base64加密出错", e);
    }
    boolean vaild = this.vaildEncrypt(queryString, params.getEncrypt());
    if (!vaild) {
      return ChannelConstant.VAILD_FAIL;
    }
    CallbackResult callbackResult = bluePayCallBackHandle.handle(params);
    if (!callbackResult.isSuccess()) {
      logger.error("回调出错 msg={},code={}", callbackResult.getBizMsg(), callbackResult.getErrCode());
    }
    if (callbackResult.isSuccess()) {
      return ChannelConstant.SUCCESS;
    }
    return ChannelConstant.SUCCESS; // 告诉bluepay已收到这次回调;
  }

  public boolean vaildEncrypt(String queryString, String encrypt) {
    queryString = urldecodeOperator(queryString);

    queryString = queryString.substring(0, queryString.indexOf("encrypt"));
    queryString = queryString.substring(0, queryString.length() - 1); // 去掉最后的"&"
    queryString = queryString + productIdKey;
    String md5String = MD5Util.getMD5String(queryString);
    return md5String.equals(encrypt);
  }

  public String getTransactionCodeFromQUeryString(String queryString) {
    String s1 = queryString.substring(queryString.indexOf("&t_id") + 1);
    String opNameValue = "";
    if (s1.contains("&")) {
      opNameValue = s1.substring(0, s1.indexOf('&'));
    } else {
      opNameValue = s1;
    }
    return opNameValue.substring(opNameValue.indexOf('=') + 1);
  }
  /**
   * operator参数有空格,先decode再验证 将queryString中的operator decode
   *
   * @param queryString
   * @return operator参数URLdecode之后的queryString
   */
  public String urldecodeOperator(String queryString) {
    String s1 = queryString.substring(queryString.indexOf("operator"));
    String opNameValue = "";
    if (s1.contains("&")) {
      opNameValue = s1.substring(0, s1.indexOf('&'));
    } else {
      opNameValue = s1;
    }
    // opNameValue   operator=BAC%25UIO
    String opValue = opNameValue.substring(opNameValue.indexOf('=') + 1);
    String opValueDecode = "";
    try {
      opValueDecode = URLDecoder.decode(opValue, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      logger.error("不支持的编码类型", e);
      e.printStackTrace();
    }
    String opNameValueDecode = "operator=" + opValueDecode;
    return queryString.replace(opNameValue, opNameValueDecode);
  }

  /**
   * 测试产品回调地址
   *
   * @param params
   * @param request
   * @return
   * @throws Exception
   */
  @RequestMapping(value = "/test/bluepay", method = RequestMethod.GET)
  @ResponseBody
  public String bluepayTestProductCallback(BluePayCallBackParams params, HttpServletRequest request)
      throws Exception {
    // System.out.println("进入testcallback方法");
    String queryString = request.getQueryString();

    String transactionCode = this.getTransactionCodeFromQUeryString(queryString);
    System.out.println(
        "rediskey:" + transactionCode + "   haskey?" + stringRedisTemplate.hasKey(transactionCode));
    int count = 0;
    while (stringRedisTemplate.hasKey(transactionCode)) {
      System.out.println("callback:redis存在key");
      Thread.sleep(ChannelConstant.SLEEP_TIME);
      count++;
      if (count > ChannelConstant.REDIS_RETRY_COUNT) {
        break;
      }
    }
    logger.info(
        "测试产品回调(/test/bluepay):{}",
        Base64.getEncoder().encode(queryString.getBytes(StandardCharsets.UTF_8.name())));
    Path p = Paths.get("/root", "bluepaytestcallback.txt");
    Files.write(p, Arrays.asList(queryString), StandardOpenOption.APPEND);
    boolean vaild = this.vaildEncrypt(queryString, params.getEncrypt());
    if (!vaild) {
      System.out.println("验证失败");
      return ChannelConstant.VAILD_FAIL;
    }
    CallbackResult callbackResult = bluePayCallBackHandle.handle(params);
    if (!callbackResult.isSuccess()) {
      logger.error("回调出错 msg={},code={}", callbackResult.getBizMsg(), callbackResult.getErrCode());
    }
    if (callbackResult.isSuccess()) {
      return ChannelConstant.SUCCESS;
    }
    return ChannelConstant.SUCCESS; // 告诉bluepay已收到这次回调;
  }
}
