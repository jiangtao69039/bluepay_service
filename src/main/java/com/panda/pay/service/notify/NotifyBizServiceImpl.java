package com.panda.pay.service.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.panda.pay.constant.enums.StatusEnum;
import com.panda.pay.ds.primary.entity.BizNotify;
import com.panda.pay.ds.primary.entity.GatherTrade;
import com.panda.pay.ds.primary.entity.LoanTrade;
import com.panda.pay.ds.primary.repository.BizNotifyRepository;
import com.panda.pay.framework.CallbackResult;
import com.panda.pay.util.TimeUtil;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jiangtao on 19-1-28 下午4:35
 *
 * <p>回调业务系统的处理类
 */
@Service("notifyBizService")
public class NotifyBizServiceImpl {

  private Logger logger = LoggerFactory.getLogger(NotifyBizServiceImpl.class);

  @Autowired BizNotifyRepository bizNotifyRepository;
  @Autowired RestTemplate restTemplate;
  // @Autowired SqsServiceImpl sqsService;

  /**
   * 收款业务回调通知业务系统
   *
   * @param gatherTrade
   * @param params
   * @return
   */
  @Async
  public CallbackResult<?> notifyBizGather(GatherTrade gatherTrade, NotifyBizRequestParams params) {

    logger.info("支付服务通知业务系统,通知内容:{}", JSONObject.toJSONString(params));

    if ("http".equals(gatherTrade.getBizNotifyType())) {
      // 1.通过http通知业务系统
      CallbackResult<JSONObject> notifyBizCall = this.nofifyBizGatherByHttp(gatherTrade, params);

      // 2.更新通知记录表
      updateBizNotifyTable(
          gatherTrade, params, notifyBizCall.getBusinessObject(), notifyBizCall.isSuccess());
      if (notifyBizCall.isSuccess()) {
        return CallbackResult.success();
      }
      if (!notifyBizCall.isSuccess()) {
        // JSONObject j = notifyBizCall.getBusinessObject();
        return CallbackResult.failure(
            "通知业务系统失败",
            "msg:" + notifyBizCall.getBizMsg(),
            StatusEnum.NOTIFY_BIZ_ERROR.getCode(),
            notifyBizCall.getBusinessObject(),
            notifyBizCall.getThrowable());
      }
    }
    if ("sqs".equals(gatherTrade.getBizNotifyType())) {
      CallbackResult<JSONObject> notifyBizCall = this.nofifyBizGatherBySqs(gatherTrade, params);

      // 2.更新通知记录表
      updateBizNotifyTable(
          gatherTrade, params, notifyBizCall.getBusinessObject(), notifyBizCall.isSuccess());
      if (notifyBizCall.isSuccess()) {
        return CallbackResult.success();
      }
      if (!notifyBizCall.isSuccess()) {
        JSONObject j = notifyBizCall.getBusinessObject();
        return CallbackResult.failure(
            "通知业务系统失败",
            "msg:" + notifyBizCall.getBizMsg(),
            StatusEnum.NOTIFY_BIZ_ERROR.getCode(),
            notifyBizCall.getBusinessObject(),
            notifyBizCall.getThrowable());
      }
    }

    return CallbackResult.failure(
        "不支持的通知类型", "请使用http或sqs方式回调通知", StatusEnum.NOT_SUPPORT_NOTIFY_TYPE.getCode(), null, null);
  }

  /**
   * 更新通知表
   *
   * @param o
   * @param params
   * @param bizRep
   * @param notifyBizSuccess
   */
  public void updateBizNotifyTable(
      Object o, NotifyBizRequestParams params, JSONObject bizRep, boolean notifyBizSuccess) {
    if (o instanceof GatherTrade) {
      GatherTrade gatherTrade = (GatherTrade) o;

      BizNotify bizNotify = bizNotifyRepository.findByTransactionCode(params.getTransactionCode());
      if (bizNotify == null) {
        bizNotify = new BizNotify();
      }

      bizNotify.setTransactionCode(gatherTrade.getTransactionCode());
      bizNotify.setOutTradeNo(gatherTrade.getOutTradeNo());
      // bizNotify.setBizResponse(bizRep.toJSONString());
      JSONArray bizResponseArray =
          Optional.ofNullable(bizNotify.getBizResponse())
              .map(JSONArray::parseArray)
              .orElseGet(JSONArray::new);
      if (bizRep != null) {
        bizResponseArray.add(bizRep);
      }

      bizNotify.setBizResponse(bizResponseArray.isEmpty() ? null : bizResponseArray.toJSONString());

      Integer count = Optional.ofNullable(bizNotify.getNotifyCount()).orElse(0);
      count++;
      bizNotify.setNotifyCount(count);
      JSONArray bizNotifyParamsArray =
          Optional.ofNullable(bizNotify.getNotifyParams())
              .map(JSONArray::parseArray)
              .orElseGet(JSONArray::new);
      JSONObject notifyJson = JSONObject.parseObject(JSON.toJSONString(params));
      notifyJson.put("notifyTime", TimeUtil.DateToRFC3339String(new Date()));

      bizNotifyParamsArray.add(notifyJson);
      bizNotify.setNotifyParams(bizNotifyParamsArray.toJSONString());

      if (notifyBizSuccess) {
        bizNotify.setNotifyStatus("success");
      } else {
        bizNotify.setNotifyStatus("fail");
      }
      bizNotify.setNotifyType(gatherTrade.getBizNotifyType());
      bizNotify.setNotifyUrl(gatherTrade.getBizNotifyUrl());
      bizNotify.setUpdatedAt(new Date());
      bizNotifyRepository.save(bizNotify);
    }

    if (o instanceof LoanTrade) {
      LoanTrade gatherTrade = (LoanTrade) o;

      BizNotify bizNotify = bizNotifyRepository.findByTransactionCode(params.getTransactionCode());
      if (bizNotify == null) {
        bizNotify = new BizNotify();
      }

      bizNotify.setTransactionCode(gatherTrade.getTransactionCode());
      bizNotify.setOutTradeNo(gatherTrade.getOutTradeNo());
      // bizNotify.setBizResponse(bizRep.toJSONString());
      JSONArray bizResponseArray =
          Optional.ofNullable(bizNotify.getBizResponse())
              .map(JSONArray::parseArray)
              .orElseGet(JSONArray::new);
      if (bizRep != null) {
        bizResponseArray.add(bizRep);
      }

      bizNotify.setBizResponse(bizResponseArray.isEmpty() ? null : bizResponseArray.toJSONString());

      Integer count = Optional.ofNullable(bizNotify.getNotifyCount()).orElse(0);
      count++;
      bizNotify.setNotifyCount(count);
      JSONArray bizNotifyParamsArray =
          Optional.ofNullable(bizNotify.getNotifyParams())
              .map(JSONArray::parseArray)
              .orElseGet(JSONArray::new);
      JSONObject notifyJson = JSONObject.parseObject(JSON.toJSONString(params));
      notifyJson.put("notifyTime", TimeUtil.DateToRFC3339String(new Date()));

      bizNotifyParamsArray.add(notifyJson);
      bizNotify.setNotifyParams(bizNotifyParamsArray.toJSONString());

      if (notifyBizSuccess) {
        bizNotify.setNotifyStatus("success");
      } else {
        bizNotify.setNotifyStatus("fail");
      }
      bizNotify.setNotifyType(gatherTrade.getBizNotifyType());
      bizNotify.setNotifyUrl(gatherTrade.getBizNotifyUrl());
      bizNotify.setUpdatedAt(new Date());
      bizNotifyRepository.save(bizNotify);
    }
  }

  /**
   * 放款业务回调通知业务系统
   *
   * @param loanTrade
   * @param params
   * @return
   */
  @Async
  public CallbackResult<?> notifyBizLoan(LoanTrade loanTrade, NotifyBizRequestParams params) {

    logger.info("支付服务通知业务系统,通知内容:{}", JSONObject.toJSONString(params));

    if ("http".equals(loanTrade.getBizNotifyType())) {
      // 1.通过http通知业务系统
      CallbackResult<JSONObject> notifyBizCall = this.nofifyBizLoanByHttp(loanTrade, params);

      // 2.更新通知记录表
      updateBizNotifyTable(
          loanTrade, params, notifyBizCall.getBusinessObject(), notifyBizCall.isSuccess());

      if (notifyBizCall.isSuccess()) {
        return CallbackResult.success();
      }
      if (!notifyBizCall.isSuccess()) {
        JSONObject j = notifyBizCall.getBusinessObject();
        return CallbackResult.failure(
            "通知业务系统失败",
            "msg:" + j.toJSONString(),
            StatusEnum.NOTIFY_BIZ_ERROR.getCode(),
            null,
            null);
      }
    }

    if ("sqs".equals(loanTrade.getBizNotifyType())) {
      CallbackResult<JSONObject> notifyBizCall = this.nofifyBizLoanBySqs(loanTrade, params);

      // 2.更新通知记录表
      updateBizNotifyTable(
          loanTrade, params, notifyBizCall.getBusinessObject(), notifyBizCall.isSuccess());
      if (notifyBizCall.isSuccess()) {
        return CallbackResult.success();
      }
      if (!notifyBizCall.isSuccess()) {
        // JSONObject j = notifyBizCall.getBusinessObject();
        return CallbackResult.failure(
            "通知业务系统失败",
            "msg:" + notifyBizCall.getBizMsg(),
            StatusEnum.NOTIFY_BIZ_ERROR.getCode(),
            notifyBizCall.getBusinessObject(),
            notifyBizCall.getThrowable());
      }
    }
    return CallbackResult.failure(
        "不支持的通知类型", "请使用http或sqs方式回调通知", StatusEnum.NOT_SUPPORT_NOTIFY_TYPE.getCode(), null, null);
  }

  /**
   * 使用sqs回调通知业务系统
   *
   * @param loanTrade
   * @param params
   */
  public CallbackResult<JSONObject> nofifyBizLoanBySqs(
      LoanTrade loanTrade, NotifyBizRequestParams params) {
    /*String sqsQueue = loanTrade.getBizNotifyUrl();
    String sqsMsg = JSONObject.toJSONString(params);
    try {
      sqsService.sendMessage(sqsQueue, sqsMsg);
    } catch (Exception e) {
      e.printStackTrace();
      JSONObject j = new JSONObject();
      j.put("sqsQueue", sqsQueue);
      j.put("exception", e.getMessage());
      return CallbackResult.failure(
          "回调通知业务系统", "使用sqs通知失败" + j.toJSONString(), StatusEnum.NOTIFY_BIZ_ERROR.getCode(), j, e);
    }*/
    return CallbackResult.success();
  }

  /**
   * 放款业务使用http方式回调通知业务系统
   *
   * @param loanTrade
   * @param params
   * @return
   */
  public CallbackResult<JSONObject> nofifyBizLoanByHttp(
      LoanTrade loanTrade, NotifyBizRequestParams params) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<NotifyBizRequestParams> entity = new HttpEntity<>(params, headers);
    ResponseEntity<String> responseEntity;
    try {
      responseEntity =
          restTemplate.postForEntity(loanTrade.getBizNotifyUrl(), entity, String.class);

    } catch (HttpClientErrorException e) {
      logger.error("放款通知业务系统异常,transactionCode:{}", loanTrade.getTransactionCode(), e);

      e.printStackTrace();
      JSONObject j = new JSONObject();
      j.put("time", TimeUtil.DateToRFC3339String(new Date()));
      j.put("HttpResponseCode", "未返回");
      j.put("HttpResponseBody", "未返回");
      j.put("msg", "http连接或响应超时" + e.getMessage());
      return CallbackResult.failure(
          "通知业务系统(fintech)", "失败,http连接错误", StatusEnum.HTTP_REQUEST_ERROR.getCode(), j, null);
    } catch (Exception e) {
      logger.error("放款通知业务系统异常,transactionCode:{}", loanTrade.getTransactionCode(), e);
      e.printStackTrace();
      JSONObject j = new JSONObject();
      j.put("time", TimeUtil.DateToRFC3339String(new Date()));
      j.put("HttpResponseCode", "未返回");
      j.put("HttpResponseBody", "未返回");
      j.put("msg", "http连接或响应超时" + e.getMessage());
      return CallbackResult.failure(
          "通知业务系统(fintech)", "失败,http连接错误", StatusEnum.HTTP_REQUEST_ERROR.getCode(), j, null);
    }

    if (responseEntity.getStatusCodeValue() != 200) {
      JSONObject j = new JSONObject();
      j.put("time", TimeUtil.DateToRFC3339String(new Date()));
      j.put("HttpResponseCode", responseEntity.getStatusCodeValue() + "");
      j.put(
          "HttpResponseBody",
          StringUtils.isBlank(responseEntity.getBody()) ? "未返回" : responseEntity.getBody());
      j.put("msg", "http响应码不是200");
      return CallbackResult.failure(
          "通知业务系统", "失败,业务系统未返回200响应码", StatusEnum.HTTP_RESPONSE_ERROR.getCode(), j, null);
    }
    JSONObject j = new JSONObject();
    j.put("time", TimeUtil.DateToRFC3339String(new Date()));
    j.put("HttpResponseCode", responseEntity.getStatusCodeValue() + "");
    j.put(
        "HttpResponseBody",
        StringUtils.isBlank(responseEntity.getBody()) ? "未返回" : responseEntity.getBody());
    j.put("msg", "200 成功");
    return CallbackResult.success("通知业务系统", "成功", j);
  }

  /**
   * 使用sqs回调通知业务系统
   *
   * @param gatherTrade
   * @param params
   */
  public CallbackResult<JSONObject> nofifyBizGatherBySqs(
      GatherTrade gatherTrade, NotifyBizRequestParams params) {
    /*String sqsQueue = gatherTrade.getBizNotifyUrl();
    String sqsMsg = JSONObject.toJSONString(params);
    try {
      sqsService.sendMessage(sqsQueue, sqsMsg);
    } catch (Exception e) {
      e.printStackTrace();
      JSONObject j = new JSONObject();
      j.put("sqsQueue", sqsQueue);
      j.put("exception", e.getMessage());
      return CallbackResult.failure(
          "回调通知业务系统", "使用sqs通知失败" + j.toJSONString(), StatusEnum.NOTIFY_BIZ_ERROR.getCode(), j, e);
    }*/
    return CallbackResult.success();
  }

  /**
   * 收款业务使用http方式回调通知业务系统
   *
   * @param gatherTrade
   * @param params
   * @return
   */
  public CallbackResult<JSONObject> nofifyBizGatherByHttp(
      GatherTrade gatherTrade, NotifyBizRequestParams params) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<NotifyBizRequestParams> entity = new HttpEntity<>(params, headers);
    ResponseEntity<String> responseEntity;
    try {
      responseEntity =
          restTemplate.postForEntity(gatherTrade.getBizNotifyUrl(), entity, String.class);

    } catch (HttpClientErrorException e) {
      logger.error("收款通知业务系统异常,transactionCode:{}", gatherTrade.getTransactionCode(), e);

      e.printStackTrace();
      JSONObject j = new JSONObject();
      j.put("time", TimeUtil.DateToRFC3339String(new Date()));
      j.put("HttpResponseCode", "未返回");
      j.put("HttpResponseBody", "未返回");
      j.put("msg", "http连接或响应超时");
      return CallbackResult.failure(
          "通知业务系统(gather)", "失败,http连接错误", StatusEnum.HTTP_REQUEST_ERROR.getCode(), j, null);
    } catch (Exception e) {
      logger.error("收款通知业务系统异常,transactionCode:{}", gatherTrade.getTransactionCode(), e);

      e.printStackTrace();
      JSONObject j = new JSONObject();
      j.put("time", TimeUtil.DateToRFC3339String(new Date()));
      j.put("HttpResponseCode", "未返回");
      j.put("HttpResponseBody", "未返回");
      j.put("msg", "http连接或响应超时");
      return CallbackResult.failure(
          "通知业务系统(gather)", "失败,http连接错误", StatusEnum.HTTP_REQUEST_ERROR.getCode(), j, null);
    }

    if (responseEntity.getStatusCodeValue() != 200) {
      JSONObject j = new JSONObject();
      j.put("time", TimeUtil.DateToRFC3339String(new Date()));
      j.put("HttpResponseCode", responseEntity.getStatusCodeValue() + "");
      j.put(
          "HttpResponseBody",
          StringUtils.isBlank(responseEntity.getBody()) ? "未返回" : responseEntity.getBody());
      j.put("msg", "http响应码不是200");
      return CallbackResult.failure(
          "通知业务系统", "失败,业务系统未返回200响应码", StatusEnum.HTTP_RESPONSE_ERROR.getCode(), j, null);
    }
    JSONObject j = new JSONObject();
    j.put("time", TimeUtil.DateToRFC3339String(new Date()));
    j.put("HttpResponseCode", responseEntity.getStatusCodeValue() + "");
    j.put(
        "HttpResponseBody",
        StringUtils.isBlank(responseEntity.getBody()) ? "未返回" : responseEntity.getBody());
    j.put("msg", "200 成功");
    return CallbackResult.success("通知业务系统", "成功", j);
  }
}
