package com.panda.pay.service.channel.bluepay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.panda.pay.constant.ChannelConstant;
import com.panda.pay.constant.NationZoneMap;
import com.panda.pay.constant.enums.StatusEnum;
import com.panda.pay.controller.params.UnifyGatherParams;
import com.panda.pay.controller.params.UnifyLoanParams;
import com.panda.pay.controller.params.UnifyParams;
import com.panda.pay.dao.GatherTradeDao;
import com.panda.pay.dao.LoanTradeDao;
import com.panda.pay.dao.PayTradeDao;
import com.panda.pay.ds.primary.entity.GatherTrade;
import com.panda.pay.ds.primary.entity.LoanTrade;
import com.panda.pay.ds.primary.entity.PayTrade;
import com.panda.pay.ds.primary.repository.GatherTradeRepository;
import com.panda.pay.framework.CallbackResult;
import com.panda.pay.service.channel.AbstractPayChannel;
import com.panda.pay.service.channel.UnifyResponse;
import com.panda.pay.util.*;
import com.panda.pay.util.Base64;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

/** Created by jiangtao on 19-1-24 下午8:28 对接bluepay渠道的具体类 用于bluepay的收款 放款 和 查询 */
@Component("bluePayChannel")
public class BluePayChannelImpl extends AbstractPayChannel {

  private List<String> supportBizCodes;
  @Autowired PayTradeDao payTradeDao;
  @Autowired GatherTradeDao gatherTradeDao;

  @Autowired RedisTemplate<String, String> stringRedisTemplate;
  @Autowired LoanTradeDao loanTradeDao;
  @Autowired GatherTradeRepository gatherTradeRepository;

  /*  @Value("${thirdparty.bluepay.productIdProd}")
  String productIdProd;*/

  @Value("${thirdparty.bluepay.productId}")
  String productId;

  @Value("${thirdparty.bluepay.productIdKey}")
  String productIdKey;

  @Value("${url.bluepay.gatherUrl}")
  String gatherUrl;

  @Value("${url.bluepay.fintechUrl}")
  String fintechUrl;

  @Value("${url.bluepay.fintechQueryUrl}")
  String fintechQueryUrl;

  @Value("${url.bluepay.gatherQueryUrl}")
  String gatherQueryUrl;

  @Value("${thirdparty.bluepay.operatorId}")
  String operatorId;

  @PostConstruct
  /** 初始化一些参数 */
  private void init() {
    if (supportBizCodes == null) {
      supportBizCodes = new ArrayList<>();
      supportBizCodes.add(ChannelConstant.BLUEPAY_BIZ_CODE_GATHER_DYNAMIC_VA);
      supportBizCodes.add(ChannelConstant.BLUEPAY_BIZ_CODE_GATHER_STATIC_VA);
      supportBizCodes.add(ChannelConstant.BLUEPAY_BIZ_CODE_FINTECH_LOAN);
    }
  }

  /**
   * bluepay渠道放款
   *
   * @param params 渠道放款接口需要的请求参数
   * @return 放款结果
   */
  @Override
  public CallbackResult<UnifyResponse> doLoan(UnifyLoanParams params) {
    /** 1.检查参数 */
    CallbackResult vaildP = this.validLoanParams(params);
    if (!vaildP.isSuccess()) {
      return CallbackResult.failure(
          vaildP.getBizName(), vaildP.getBizMsg(), vaildP.getErrCode(), null, null);
    }
    /** 2.保存交易总表记录 */
    CallbackResult<PayTrade> savePayTradeCallback = this.saveNewPayTradeRecord(params);
    if (!savePayTradeCallback.isSuccess()) {
      return CallbackResult.failure(
          savePayTradeCallback.getBizName(),
          savePayTradeCallback.getBizMsg(),
          savePayTradeCallback.getErrCode(),
          null,
          null);
    }

    /** 根据channelBizCode选择合适的产品code放入channelSpecialParams中 */
    /** 目前没有实现,productId由接口调用方手动传入 */

    /** 3.保存放款详细表记录 */
    CallbackResult<LoanTrade> saveLoanCallback =
        this.saveNewLoanTradeRecord(
            params, savePayTradeCallback.getBusinessObject().getTransactionCode());
    if (!saveLoanCallback.isSuccess()) {
      return CallbackResult.failure(
          saveLoanCallback.getBizName(),
          saveLoanCallback.getBizMsg(),
          saveLoanCallback.getErrCode(),
          null,
          null);
    }

    /** 4.发起bluepay 放款请求 */
    redisLockTransactionCode(savePayTradeCallback.getBusinessObject().getTransactionCode());
    CallbackResult<BluePayFintechResponseParams> bluePayLoanResponseParamsCallbackResult =
        this.doLoanHttpRequest(
            params, savePayTradeCallback.getBusinessObject().getTransactionCode());
    if (!bluePayLoanResponseParamsCallbackResult.isSuccess()) {
      return CallbackResult.failure(
          bluePayLoanResponseParamsCallbackResult.getBizName(),
          bluePayLoanResponseParamsCallbackResult.getBizMsg(),
          bluePayLoanResponseParamsCallbackResult.getErrCode(),
          null,
          null);
    }

    /** 5.更新收款详细表记录 */
    updateLoanTradeAfterDoHttpReq(
        saveLoanCallback.getBusinessObject(),
        bluePayLoanResponseParamsCallbackResult.getBusinessObject());
    redisUnLockTransactionCode(savePayTradeCallback.getBusinessObject().getTransactionCode());

    /** 6.返回统一响应 */
    BluePayFintechResponseParams bluePayFintechResponseParams =
        bluePayLoanResponseParamsCallbackResult.getBusinessObject();
    if (!"200".equals(bluePayFintechResponseParams.getHttpStatusCode())) {
      UnifyResponse unifyResponse =
          this.getUnifyResponseFromBluePayFintechResponseParams(
              bluePayFintechResponseParams,
              savePayTradeCallback.getBusinessObject().getTransactionCode());
      return CallbackResult.failure(
          "bluepay放款接口",
          "放款接口响应码不是200",
          StatusEnum.HTTP_RESPONSE_ERROR.getCode(),
          unifyResponse,
          null);
    }
    if (!"200".equals(bluePayFintechResponseParams.getCode())) {
      UnifyResponse unifyResponse =
          this.getUnifyResponseFromBluePayFintechResponseParams(
              bluePayFintechResponseParams,
              savePayTradeCallback.getBusinessObject().getTransactionCode());
      return CallbackResult.failure(
          "bluepay放款接口",
          "放款接口业务错误,描述:"
              + bluePayFintechResponseParams.getCode()
              + ","
              + bluePayFintechResponseParams.getDescription(),
          StatusEnum.BLUEPAY_LOAN_REQ_ERROR.getCode(),
          unifyResponse,
          null);
    }
    if (!"201".equals(bluePayFintechResponseParams.getTransferStatus())) {
      UnifyResponse unifyResponse =
          this.getUnifyResponseFromBluePayFintechResponseParams(
              bluePayFintechResponseParams,
              savePayTradeCallback.getBusinessObject().getTransactionCode());
      return CallbackResult.failure(
          "bluepay放款接口",
          "放款接口业务错误,描述:"
              + bluePayFintechResponseParams.getTransferStatus()
              + ","
              + bluePayFintechResponseParams.getDescription(),
          StatusEnum.BLUEPAY_LOAN_REQ_ERROR.getCode(),
          unifyResponse,
          null);
    }
    UnifyResponse unifyResponse =
        this.getUnifyResponseFromBluePayFintechResponseParams(
            bluePayFintechResponseParams,
            savePayTradeCallback.getBusinessObject().getTransactionCode());
    return CallbackResult.success("bluepay放款接口", "成功", unifyResponse);
  }

  /**
   * bluepay渠道收款
   *
   * @param params 统一收款接口接收的请求参数
   * @return bluepay收款接口的业务状态(包含http返回的响应)
   */
  @Override
  public CallbackResult<UnifyResponse> doGather(@NotNull UnifyGatherParams params) {

    /** 1.检查参数 */
    CallbackResult vaildP = this.validGatherParams(params);
    if (!vaildP.isSuccess()) {
      return CallbackResult.failure(
          vaildP.getBizName(), vaildP.getBizMsg(), vaildP.getErrCode(), null, null);
    }
    /** 2.保存交易总表记录 */
    CallbackResult<PayTrade> savePayTradeCallback = this.saveNewPayTradeRecord(params);
    if (!savePayTradeCallback.isSuccess()) {
      return CallbackResult.failure(
          savePayTradeCallback.getBizName(),
          savePayTradeCallback.getBizMsg(),
          savePayTradeCallback.getErrCode(),
          null,
          null);
    }
    /** 根据channelBizCode选择合适的产品code放入channelSpecialParams中 */
    /** 目前没有实现,productId由接口调用方手动传入 */

    /** 3.保存收款详细表记录 */
    CallbackResult<GatherTrade> saveGatherCallback =
        this.saveNewGatherTradeRecord(
            params, savePayTradeCallback.getBusinessObject().getTransactionCode());
    if (!saveGatherCallback.isSuccess()) {
      return CallbackResult.failure(
          saveGatherCallback.getBizName(),
          saveGatherCallback.getBizMsg(),
          saveGatherCallback.getErrCode(),
          null,
          null);
    }
    /** 4.发起bluepay 收款请求 */

    // redis添加key,防止回调更新太早
    redisLockTransactionCode(savePayTradeCallback.getBusinessObject().getTransactionCode());
    CallbackResult<BluePayGatherResponseParams> bluePayGatherResponseParamsCallbackResult =
        this.doGatherHttpRequest(
            params, savePayTradeCallback.getBusinessObject().getTransactionCode());
    if (!bluePayGatherResponseParamsCallbackResult.isSuccess()) {
      return CallbackResult.failure(
          bluePayGatherResponseParamsCallbackResult.getBizName(),
          bluePayGatherResponseParamsCallbackResult.getBizMsg(),
          bluePayGatherResponseParamsCallbackResult.getErrCode(),
          null,
          null);
    }
    /** 5.更新收款详细表记录 */
    updateGatherTradeAfterDoHttpReq(
        saveGatherCallback.getBusinessObject(),
        bluePayGatherResponseParamsCallbackResult.getBusinessObject());
    redisUnLockTransactionCode(savePayTradeCallback.getBusinessObject().getTransactionCode());

    /** 6.返回统一响应 */
    BluePayGatherResponseParams bluePayGatherResponseParams =
        bluePayGatherResponseParamsCallbackResult.getBusinessObject();
    if (!"200".equals(bluePayGatherResponseParams.getHttpStatusCode())) {
      UnifyResponse unifyResponse =
          this.getUnifyResponseFromBluePayGatherResponseParams(
              bluePayGatherResponseParams,
              savePayTradeCallback.getBusinessObject().getTransactionCode());
      return CallbackResult.failure(
          "bluepay收款接口",
          "收款接口响应码不是200",
          StatusEnum.HTTP_RESPONSE_ERROR.getCode(),
          unifyResponse,
          null);
    }
    if (!"201".equals(bluePayGatherResponseParams.getStatus())) {
      UnifyResponse unifyResponse =
          this.getUnifyResponseFromBluePayGatherResponseParams(
              bluePayGatherResponseParams,
              savePayTradeCallback.getBusinessObject().getTransactionCode());
      return CallbackResult.failure(
          "bluepay收款接口",
          "收款接口业务错误,描述:"
              + bluePayGatherResponseParams.getStatus()
              + ","
              + bluePayGatherResponseParams.getDescription(),
          StatusEnum.BLUEPAY_GATHER_REQ_ERROR.getCode(),
          unifyResponse,
          null);
    }

    UnifyResponse unifyResponse =
        this.getUnifyResponseFromBluePayGatherResponseParams(
            bluePayGatherResponseParams,
            savePayTradeCallback.getBusinessObject().getTransactionCode());
    return CallbackResult.success("bluepay收款接口", "成功", unifyResponse);
  }

  /**
   * 从bluepay收款接口的响应生成支付系统的统一响应结果
   *
   * @param bluePayGatherResponseParams
   * @param transactionCode 支付系统唯一编码
   * @return
   */
  public UnifyResponse getUnifyResponseFromBluePayGatherResponseParams(
      BluePayGatherResponseParams bluePayGatherResponseParams, String transactionCode) {
    UnifyResponse unifyResponse = new UnifyResponse();
    // unifyResponse.setChannelCode(ChannelConstant.BLUEPAY_CHANNEL_CODE);
    // unifyResponse.setChannelBizCode(ChannelConstant.BLUEPAY_BIZ_CODE_GATHER_DYNAMIC_VA);
    unifyResponse.setPayServeBizType("gather");
    unifyResponse.setTransactionCode(transactionCode);
    JSONObject j = (JSONObject) JSON.toJSON(bluePayGatherResponseParams);
    j.put("channelCode", ChannelConstant.BLUEPAY_CHANNEL_CODE);
    j.put("channelBizCode", ChannelConstant.BLUEPAY_BIZ_CODE_GATHER_DYNAMIC_VA);
    j.put("transactionCode", transactionCode);
    unifyResponse.setBizParams(j);
    return unifyResponse;
  }

  /**
   * 从bluepay放款接口的响应生成支付系统的统一响应结果
   *
   * @param params
   * @param transactionCode 支付系统唯一编码
   * @return
   */
  public UnifyResponse getUnifyResponseFromBluePayFintechResponseParams(
      BluePayFintechResponseParams params, String transactionCode) {
    UnifyResponse unifyResponse = new UnifyResponse();
    // unifyResponse.setChannelCode(ChannelConstant.BLUEPAY_CHANNEL_CODE);
    // unifyResponse.setChannelBizCode(ChannelConstant.BLUEPAY_BIZ_CODE_FINTECH_LOAN);
    unifyResponse.setPayServeBizType("loan");
    unifyResponse.setTransactionCode(transactionCode);
    JSONObject j = (JSONObject) JSON.toJSON(params);
    j.put("transactionCode", transactionCode);
    j.put("channelCode", ChannelConstant.BLUEPAY_CHANNEL_CODE);
    j.put("channelBizCode", ChannelConstant.BLUEPAY_BIZ_CODE_FINTECH_LOAN);
    unifyResponse.setBizParams(j);
    return unifyResponse;
  }

  /**
   * 从支付系统统一收款接口参数中提取个人信息
   *
   * @param params
   * @return
   */
  public JSONObject getPersonInfoFromGatherParams(@NotNull UnifyGatherParams params) {
    JSONObject j = new JSONObject();
    j.put("nation", params.getNation());
    j.put("mobile", params.getMobile());
    return StringUtils.isBlank(j.toJSONString()) ? null : j;
  }

  /**
   * 从支付系统统一放款接口参数中提取个人信息
   *
   * @param params
   * @return
   */
  public JSONObject getPersonInfoFromLoanParams(@NotNull UnifyLoanParams params) {
    JSONObject j = new JSONObject();
    j.put("nation", params.getNation());
    j.put("mobile", params.getMobile());
    JSONObject channelSp = params.getChannelSpecialParams();
    j.put(
        "payeeCountry",
        channelSp.getString("payeeCountry") == null
            ? params.getNation()
            : channelSp.getString("payeeCountry"));
    j.put("payeeName", channelSp.getString("payeeName"));
    j.put("payeeBankName", channelSp.getString("payeeBankName"));
    j.put("payeeAccount", channelSp.getString("payeeAccount"));
    String msisdn = NationZoneMap.getZoneByNation(params.getNation()) + params.getMobile();
    j.put("payeeMsisdn", msisdn);
    return StringUtils.isBlank(j.toJSONString()) ? null : j;
  }

  /**
   * 更新收款详细表
   *
   * @param transactionCode 支付系统唯一编号
   * @param params bluepay收款接口的响应
   * @return
   */
  public GatherTrade updateGatherTradeAfterDoHttpReq(
      String transactionCode, BluePayGatherResponseParams params) {
    GatherTrade gatherTrade = gatherTradeRepository.findByTransactionCode(transactionCode);
    if (gatherTrade == null) {
      return null;
    }
    return this.updateGatherTradeAfterDoHttpReq(gatherTrade, params);
  }

  /**
   * 使用redis存储transactionCode,保存完毕后删除key 防止回调处理早一步更新数据库
   *
   * @param transactionCode
   */
  public void redisLockTransactionCode(String transactionCode) {
    stringRedisTemplate.opsForValue().set(transactionCode, transactionCode);
  }

  public void redisUnLockTransactionCode(String transactionCode) {
    stringRedisTemplate.delete(transactionCode);
  }

  /**
   * 更新收款详细表
   *
   * @param gatherTrade 详情表entity
   * @param params bluepay收款接口的响应
   * @return 更新后的entity
   */
  public GatherTrade updateGatherTradeAfterDoHttpReq(
      GatherTrade gatherTrade, BluePayGatherResponseParams params) {
    if (!"200".equals(params.getHttpStatusCode())) {
      gatherTrade.setTradeStatus("fail");
      gatherTrade.setTradeMsg("http请求错误,http响应码:" + params.getHttpStatusCode());
      String channelResponse = JSON.toJSONString(params);
      gatherTrade.setChannelOrderInfo(channelResponse);
      return gatherTradeDao.updateGatherTrade(gatherTrade);
    }

    if (!"201".equals(params.getStatus())) {
      gatherTrade.setTradeStatus("fail");
      gatherTrade.setTradeMsg(params.getStatus() + ":" + params.getDescription());
      String channelResponse = JSON.toJSONString(params);
      gatherTrade.setChannelOrderInfo(channelResponse);
      return gatherTradeDao.updateGatherTrade(gatherTrade);
    }
    gatherTrade.setTradeStatus("process");
    gatherTrade.setTradeMsg("收款订单下单成功,等待用户付款");
    String channelResponse = JSON.toJSONString(params);
    gatherTrade.setChannelOrderInfo(channelResponse);

    return gatherTradeDao.updateGatherTrade(gatherTrade);
    /*System.out.println(
    "收款订单下单成功,等待用户付款保存时间,保存后:"
        + TimeUtil.DateToRFC3339String(new Date())
        + "\n"
        + gatherTrade.toString());*/

  }

  /**
   * 更新放款表详情
   *
   * @param loanTrade 详情表entity
   * @param params bluepay放款接口的响应
   * @return 更新后的entity
   */
  public LoanTrade updateLoanTradeAfterDoHttpReq(
      LoanTrade loanTrade, BluePayFintechResponseParams params) {

    if (!"200".equals(params.getHttpStatusCode())) {
      loanTrade.setTradeStatus("fail");
      loanTrade.setTradeMsg("http请求错误,http响应码:" + params.getHttpStatusCode());
      String channelResponse = JSON.toJSONString(params);
      loanTrade.setChannelOrderInfo(channelResponse);
      return loanTradeDao.updateGatherTrade(loanTrade);
    }
    if (!"200".equals(params.getCode())) {
      loanTrade.setTradeStatus("fail");
      loanTrade.setTradeMsg("fintech放款接口业务出错,code不是200:" + params.getCode());
      String channelResponse = JSON.toJSONString(params);
      loanTrade.setChannelOrderInfo(channelResponse);
      return loanTradeDao.updateGatherTrade(loanTrade);
    }
    if (!"201".equals(params.getTransferStatus())) {
      loanTrade.setTradeStatus("fail");
      loanTrade.setTradeMsg("fintech放款接口业务出错,transferStatus不是201:" + params.getCode());
      String channelResponse = JSON.toJSONString(params);
      loanTrade.setChannelOrderInfo(channelResponse);
      return loanTradeDao.updateGatherTrade(loanTrade);
    }
    loanTrade.setTradeStatus("process");
    loanTrade.setTradeMsg("放款订单下单成功,等待bluepay放款");
    String channelResponse = JSON.toJSONString(params);
    loanTrade.setChannelOrderInfo(channelResponse);
    return loanTradeDao.updateGatherTrade(loanTrade);
  }

  Logger logger = LoggerFactory.getLogger(BluePayChannelImpl.class);
  /**
   * 向bluepay放款接口发起http请求
   *
   * @param params 支付系统统一放款接口参数
   * @param transactionCode 支付系统唯一编号
   * @return
   */
  public CallbackResult<BluePayFintechResponseParams> doLoanHttpRequest(
      @NotNull UnifyLoanParams params, String transactionCode) {
    Boolean prodFlag = this.isProd(params);
    String productId = this.productId;
    if (StringUtils.isNotBlank(params.getChannelSpecialParams().getString("productId"))) {
      productId = params.getChannelSpecialParams().getString("productId");
    } else {
      productId = this.productId;
    }
    if (!prodFlag) {
      productId = this.productId;
    }
    String data = this.getDataStringFromLoanParams(params, transactionCode);
    String methodGetQueryParamsString = null;
    try {
      methodGetQueryParamsString =
          "?productId="
              + productId
              + "&data="
              + getAES(data)
              + "&encrypt="
              + getHandleEncrypt(getAES(data), productId);
    } catch (Exception e) {
      logger.error("bluepay放款http请求报文拼装异常,transactionCode:{}", e, transactionCode, e);
      e.printStackTrace();
      return CallbackResult.failure(
          "bluepay放款接口http请求", "加密失败", StatusEnum.AES_ERROR.getCode(), null, null);
    }

    String url = this.fintechUrl;
    url = url + methodGetQueryParamsString;

    ResponseEntity<String> entry = null;
    try {
      url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      logger.error("不支持的编码类型", e);
      e.printStackTrace();
    }
    logger.info("请求bluepay放款接口,url={}", url);
    try {
      entry = restTemplate.getForEntity(url, String.class);
    } catch (HttpClientErrorException e) {
      logger.error("bluepay放款http请求异常,transactionCode:{},url:{}", transactionCode, url, e);
      e.printStackTrace();
      return CallbackResult.failure(
          "bluepay_fintech放款",
          "HTTP请求错误:" + e.getMessage(),
          StatusEnum.HTTP_REQUEST_ERROR.getCode(),
          null,
          null);
    }
    BluePayFintechResponseParams bluePayFintechResponseParams =
        this.parseFintechHttpResponse(entry);

    return CallbackResult.success("bluepayHTTP请求", "请求成功", bluePayFintechResponseParams);
  }

  private String getAES(String data) throws Exception {
    String dataEncodeFirst = AES.encryptAES(data, this.productIdKey);
    String dataEncodeSecond = Base64.encode(dataEncodeFirst);
    String dataEncodeLast = URLEncoder.encode(dataEncodeSecond, StandardCharsets.UTF_8.name());
    String dataDecodeLast =
        AES.decryptr(
            Base64.decode(URLDecoder.decode(dataEncodeLast, StandardCharsets.UTF_8.name())),
            productIdKey);
    return dataEncodeLast;
  }

  private String getHandleEncrypt(String dataEncodeLast, String productId) {
    String queryString = "productId=" + productId + "&data=" + dataEncodeLast;
    String str = queryString + this.productIdKey;
    return MD5Util.getMD5String(str);
  }

  private String getQueryEncrypt(String transactionId, String productId, String operatorId) {
    String queryString =
        "operatorId=" + operatorId + "&productid=" + productId + "&t_id=" + transactionId;
    String str = queryString + this.productIdKey;
    return MD5Util.getMD5String(str);
  }

  /**
   * 从统一放款接口参数从生成bluepay放款接口的queryString
   *
   * @param params
   * @param transactionCode 支付系统唯一编码
   * @return bluepay放款接口的queryString
   */
  public String getDataStringFromLoanParams(
      @NotNull UnifyLoanParams params, String transactionCode) {
    String transactionId = transactionCode;
    String payeeBankName = params.getChannelSpecialParams().getString("payeeBankName");
    String payeeName = params.getChannelSpecialParams().getString("payeeName");
    String payeeAccount = params.getChannelSpecialParams().getString("payeeAccount");
    String payeeConutry = params.getChannelSpecialParams().getString("payeeCountry"); // 印尼=ID
    String payeeType =
        Optional.ofNullable(params.getChannelSpecialParams().getString("payeeType"))
            .orElse("NORMAL");
    if (StringUtils.isBlank(payeeConutry)) {
      payeeConutry = params.getNation();
    }
    int transferAmount = params.getPrice();
    String zone = NationZoneMap.getZoneByNation(params.getNation());
    String payeeMsisdn = zone + params.getMobile(); // 手机号必须是62开头，用户如果输入的是0 开头，需要把0用62替换
    return "transactionId="
        + transactionId
        + "&promotionId=1000&payeeCountry="
        + payeeConutry
        + "&payeeBankName="
        + payeeBankName
        + "&payeeName="
        + payeeName
        + "&payeeAccount="
        + payeeAccount
        + "&payeeMsisdn="
        + payeeMsisdn
        + "&payeeType="
        + payeeType
        + "&amount="
        + transferAmount
        + "&currency="
        + params.getCurrency().toUpperCase();
  }

  /**
   * 发起bluepay收款接口的http请求
   *
   * @param params 支付系统统一收款接口参数
   * @param transactionCode 支付系统唯一编码
   * @return bluepay收款接口的响应
   */
  public CallbackResult<BluePayGatherResponseParams> doGatherHttpRequest(
      @NotNull UnifyGatherParams params, String transactionCode) {
    // Boolean prodFlag = this.isProd(params);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    if (StringUtils.isNotBlank(params.getChannelSpecialParams().getString("productId"))) {
      map.add("productId", params.getChannelSpecialParams().getString("productId"));
    } else {
      map.add("productId", this.productId);
    }
    /* if (!prodFlag) {
      map.add("productId", this.productId);
    }*/
    map.add("transactionId", transactionCode);
    map.add("price", params.getPrice() + "");
    map.add("promotionId", "1000");
    map.add("ui", "none");
    String zone = NationZoneMap.getZoneByNation(params.getNation());
    String msisdn = zone + params.getMobile();
    String payType = params.getChannelSpecialParams().getString("payType");
    String bankType = params.getChannelSpecialParams().getString("bankType");
    if (ChannelConstant.BLUEPAY_BIZ_CODE_GATHER_STATIC_VA.equals(params.getChannelBizCode())) {
      map.add("msisdn", msisdn);
    }
    map.add("payType", payType);
    map.add("bankType", bankType);

    HttpHeaders headers = new HttpHeaders();
    MediaType type =
        MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE + "; charset=UTF-8");
    headers.setContentType(type);
    // HttpEntity entity = new HttpEntity<>(map, headers);

    String queryString = this.map2QueryString(map.toSingleValueMap());

    // TODO 配置
    // String url = "http://test.webpay.bluepay.tech/bluepay/offline.php";
    String url = this.gatherUrl;
    /*if (prodFlag) {
      url = "http://in.webpay.bluepay.tech/bluepay/offline.php"; // 印尼正式环境地址
    }*/
    ResponseEntity<String> entry = null;
    logger.info("请求bluepay收款接口,url={},param={}", url, queryString);
    try {
      entry = restTemplate.getForEntity(url + "?" + queryString, String.class);
    } catch (HttpClientErrorException e) {
      logger.error(
          "bluepay收款http请求异常,transactionCode:{},url:{},param:{}",
          transactionCode,
          url,
          queryString,
          e);

      e.printStackTrace();
      return CallbackResult.failure(
          "bluepay动态VA收款接口",
          "HTTP请求错误:" + e.getMessage(),
          StatusEnum.HTTP_REQUEST_ERROR.getCode(),
          null,
          null);
    }
    BluePayGatherResponseParams bluePayGatherResponseParams = this.parseGatherHttpResponse(entry);

    return CallbackResult.success("bluepayHTTP请求", "请求成功", bluePayGatherResponseParams);
  }

  /**
   * 测试用,调用bluepay收款接口(测试)
   *
   * @param params
   * @param transactionCode
   * @return
   */
  /* public ResponseEntity<String> doGatherHttpRequestEntry(
      @NotNull UnifyGatherParams params, String transactionCode) {
    Boolean prodFlag = this.isProd(params);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    if (prodFlag) {
      map.add("productId", this.productIdProd);
    } else {
      map.add("productId", this.productIdDev);
    }
    map.add("transactionId", transactionCode);
    map.add("price", params.getPrice() + "");
    map.add("promotionId", "1000");
    map.add("ui", "none");
    String msisdn = params.getNation() + params.getMobile();
    String payType = params.getChannelSpecialParams().getString("payType");
    String bankType = params.getChannelSpecialParams().getString("bankType");
    if (ChannelConstant.BLUEPAY_BIZ_CODE_GATHER_STATIC_VA.equals(params.getChannelBizCode())) {
      map.add("msisdn", msisdn);
    }

    map.add("payType", payType);
    map.add("bankType", bankType);
    HttpHeaders headers = new HttpHeaders();
    MediaType type =
        MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE + "; charset=UTF-8");
    headers.setContentType(type);
    HttpEntity entity = new HttpEntity<>(map, headers);
    String url = "http://test.webpay.bluepay.tech/bluepay/offline.php";
    if (prodFlag) {
      url = "http://in.webpay.bluepay.tech/bluepay/offline.php"; // 印尼正式环境地址
    }
    ResponseEntity<String> entry = null;
    try {
      entry =
          restTemplate.getForEntity(
              "http://test.webpay.bluepay.tech/bluepay/offline.php?productId=1357&transactionId=20180328bian&price=10000&payType=atm&bankType=permata&ui=none&promotionId=1000",
              String.class);
    } catch (HttpClientErrorException e) {
      e.printStackTrace();
      return null;
    }
    return entry;
  }*/

  /**
   * 解析bluepay放款接口的响应
   *
   * @param entry 放款接口响应
   * @return
   */
  private BluePayFintechResponseParams parseFintechHttpResponse(ResponseEntity<String> entry) {
    BluePayFintechResponseParams bluePayFintechResponseParams = new BluePayFintechResponseParams();
    bluePayFintechResponseParams.setHttpStatusCode(entry.getStatusCodeValue() + "");
    if (200 != entry.getStatusCodeValue()) {
      bluePayFintechResponseParams.setDescription("http请求响应码不是200");
      return bluePayFintechResponseParams;
    }
    JSONObject result = JSONObject.parseObject(entry.getBody());
    if (!"200".equals(result.getString("code"))) {
      bluePayFintechResponseParams.setCode(result.getString("code"));
      bluePayFintechResponseParams.setTransferStatus(result.getString("transferStatus"));
      bluePayFintechResponseParams.setBizStatusCode(result.getString("code"));
      bluePayFintechResponseParams.setBizStatusMessage(
          result.getString("description") == null ? "请查阅错误码" : result.getString("description"));
      bluePayFintechResponseParams.setDescription(
          result.getString("description") == null ? "请查阅错误码" : result.getString("description"));
      return bluePayFintechResponseParams;
    }
    if (!"201".equals(result.getString("transferStatus"))) {
      bluePayFintechResponseParams.setCode(result.getString("code"));
      bluePayFintechResponseParams.setTransferStatus(result.getString("transferStatus"));
      bluePayFintechResponseParams.setBizStatusCode(result.getString("transferStatus"));
      bluePayFintechResponseParams.setBizStatusMessage(
          result.getString("description") == null ? "请查阅错误码" : result.getString("description"));
      bluePayFintechResponseParams.setDescription(
          result.getString("description") == null ? "请查阅错误码" : result.getString("description"));
      return bluePayFintechResponseParams;
    }
    bluePayFintechResponseParams.setCode(result.getString("code"));
    bluePayFintechResponseParams.setTransferStatus(result.getString("transferStatus"));
    bluePayFintechResponseParams.setBizStatusCode(result.getString("transferStatus"));
    bluePayFintechResponseParams.setBizStatusMessage("success");
    bluePayFintechResponseParams.setDescription("success");
    bluePayFintechResponseParams.setTransactionId(result.getString("transactionId"));
    return bluePayFintechResponseParams;
  }

  /**
   * blue_pay收款接口的http响应解析
   *
   * @param entry 响应
   * @return
   */
  private BluePayGatherResponseParams parseGatherHttpResponse(ResponseEntity<String> entry) {
    BluePayGatherResponseParams bluePayGatherResponseParams = new BluePayGatherResponseParams();
    bluePayGatherResponseParams.setHttpStatusCode(entry.getStatusCodeValue() + "");
    // TODO bluepay通过响应中的status判断状态
    if (200 != entry.getStatusCode().value()) {
      // bluePayGatherResponseParams.setHttpStatusCode(entry.getStatusCodeValue()+"");
      bluePayGatherResponseParams.setDescription("http请求响应码不是200");
      return bluePayGatherResponseParams;
    }
    JSONObject result = JSONObject.parseObject(entry.getBody());
    if (!"201".equals(result.getString("status"))) {
      bluePayGatherResponseParams.setStatus(result.getString("status"));
      bluePayGatherResponseParams.setBizStatusCode(result.getString("status"));
      bluePayGatherResponseParams.setBizStatusMessage(result.getString("description"));
      bluePayGatherResponseParams.setDescription(result.getString("description"));
      return bluePayGatherResponseParams;
    }
    bluePayGatherResponseParams.setStatus(result.getString("status"));
    bluePayGatherResponseParams.setBizStatusCode(result.getString("status"));
    bluePayGatherResponseParams.setBizStatusMessage(result.getString("description"));
    bluePayGatherResponseParams.setDescription(result.getString("description"));
    bluePayGatherResponseParams.setIsStatic(result.getString("isStatic"));
    bluePayGatherResponseParams.setVaFee(result.getString("vaFee"));
    bluePayGatherResponseParams.setOtcFee(result.getString("otcFee"));
    bluePayGatherResponseParams.setPaymentCode(result.getString("payment_code"));
    return bluePayGatherResponseParams;
  }
  /**
   * 判断是否是生成环境 支付系统的统一收款 放款接口参数中extend 中指定 "prodFlag":"true" 默认为"prodFlag":"false"
   *
   * @param params json
   * @return
   */
  private Boolean isProd(@NotNull UnifyParams params) {
    String prodflag =
        Optional.ofNullable(params.getExtend())
            .map(j -> j.getString("prodFlag"))
            .map(String::toLowerCase)
            .map(
                s -> {
                  if ("0".equals(s)) return "false";
                  else return "true";
                })
            .orElse("false");

    return "true".equals(prodflag);
  }

  /**
   * 判断是否是生成环境 支付系统的统一收款 放款接口参数中extend 中指定 "prodFlag":"true" 默认为"prodFlag":"false"
   *
   * @param extendstr jsonString
   * @return
   */

  /**
   * 保存gather_trade表新记录
   *
   * @param params 统一收款参数
   * @param transactionCode 支付系统生成的唯一标识
   * @return
   */
  protected CallbackResult<GatherTrade> saveNewGatherTradeRecord(
      @NotNull UnifyGatherParams params, @NotNull String transactionCode) {

    boolean outTradeNoExist = gatherTradeDao.isOutTradeNoExist(params.getOutTradeNo());
    if (outTradeNoExist) {
      // 唯一键存在
      return CallbackResult.failure(
          "保存gather_trade新记录",
          "out_trade_no已存在",
          StatusEnum.GATHERTRADE_OUT_TRADE_NO_EXIST.getCode(),
          null,
          null);
    }
    // JSONObject channelSpecialParams = params.getChannelSpecialParams();
    JSONObject personInfo = this.getPersonInfoFromGatherParams(params);

    GatherTrade gatherTrade =
        gatherTradeDao.saveNewRecord(
            transactionCode,
            params.getOutTradeNo(),
            params.getChannelCode(),
            params.getChannelBizCode(),
            params.getPrice(),
            params.getCurrency(),
            params.getNation(),
            params.getChannelSpecialParams(),
            params.getExtend(),
            personInfo,
            params.getPassBack(),
            params.getNotifyUrl(),
            params.getNotifyType());

    return CallbackResult.success("保存gather_trade新记录", "操做成功", gatherTrade);
  }

  /**
   * 保存一条放款详细表的记录
   *
   * @param params
   * @param transactionCode
   * @return
   */
  protected CallbackResult<LoanTrade> saveNewLoanTradeRecord(
      @NotNull UnifyLoanParams params, @NotNull String transactionCode) {

    boolean outTradeNoExist = loanTradeDao.isOutTradeNoExist(params.getOutTradeNo());
    if (outTradeNoExist) {
      // 唯一键存在
      return CallbackResult.failure(
          "保存loan_trade新记录",
          "out_trade_no已存在",
          StatusEnum.LOANTRADE_OUT_TRADE_NO_EXIST.getCode(),
          null,
          null);
    }
    // JSONObject channelSpecialParams = params.getChannelSpecialParams();
    JSONObject personInfo = this.getPersonInfoFromLoanParams(params);
    LoanTrade LoanTrade =
        loanTradeDao.saveNewRecord(
            transactionCode,
            params.getOutTradeNo(),
            params.getChannelCode(),
            params.getChannelBizCode(),
            params.getPrice(),
            params.getCurrency(),
            params.getChannelSpecialParams(),
            params.getExtend(),
            personInfo,
            params.getPassBack(),
            params.getNotifyUrl(),
            params.getNotifyType());

    return CallbackResult.success("保存loan_trade新记录", "操做成功", LoanTrade);
  }
  /**
   * 保存pay_trade表新纪录
   *
   * @param params 统一收款参数
   * @return 成功(包含新纪录对象) 失败(包含错误原因)
   */
  protected CallbackResult<PayTrade> saveNewPayTradeRecord(@NotNull UnifyParams params) {

    /*boolean outTradeNoExist = payTradeDao.isOutTradeNoExist(params.getOutTradeNo());
    if(outTradeNoExist){
        //唯一键存在
        return CallbackResult.failure("保存pay_trade新记录","out_trade_no已存在",StatusEnum.PAYTRADE_OUT_TRADE_NO_EXIST.getCode(),
                null,null);
    }*/
    String transactionCode = UUIDUtils.getUUID32();

    boolean isOutTradeNoExist = payTradeDao.isOutTradeNoExist(params.getOutTradeNo());
    if (isOutTradeNoExist) {
      return CallbackResult.failure(
          "保存pay_trade新记录",
          "out_trade_no已存在",
          StatusEnum.PAYTRADE_OUT_TRADE_NO_EXIST.getCode(),
          null,
          null);
    }

    /*JSONObject personInfo = new JSONObject();
    personInfo.put("nation",params.getNation());
    personInfo.put("mobile",params.getMobile());*/
    PayTrade payTrade =
        payTradeDao.saveNewRecord(
            transactionCode,
            params.getOutTradeNo(),
            params.getChannelCode(),
            params.getChannelBizCode(),
            params.getChannelSpecialParams(),
            params.getExtend());
    return CallbackResult.success("保存pay_trade新记录", "操做成功", payTrade);
  }

  /**
   * 查询订单详情(收款 放款)
   *
   * @param detail 渠道查询接口的详细表信息类
   * @return 统一响应结果
   */
  @Override
  public CallbackResult<UnifyResponse> doQuery(Object detail) {

    if (detail instanceof GatherTrade) {
      GatherTrade gatherTrade = (GatherTrade) detail;
      CallbackResult<JSONObject> callbackResult =
          this.doQueryBluePayGatherDetail((GatherTrade) detail);
      if (!callbackResult.isSuccess()) {
        return CallbackResult.failure(
            callbackResult.getBizName(),
            callbackResult.getBizMsg(),
            callbackResult.getErrCode(),
            null,
            null);
      }
      UnifyResponse response = new UnifyResponse();
      response.setTransactionCode(gatherTrade.getTransactionCode());
      response.setPayServeBizType("query_gather");
      JSONObject j = callbackResult.getBusinessObject();
      j.put("channelCode", ChannelConstant.BLUEPAY_CHANNEL_CODE);
      j.put("channelBizCode", ChannelConstant.BLUEPAY_BIZ_QUERY_GATHER_DETAIL);
      response.setBizParams(j);
      return CallbackResult.success("bluepay收款详细查询", "成功", response);
    }

    if (detail instanceof LoanTrade) {
      LoanTrade loanTrade = (LoanTrade) detail;
      CallbackResult<JSONObject> callbackResult =
          this.doQueryBluePayFintechDetail((LoanTrade) detail);
      if (!callbackResult.isSuccess()) {
        return CallbackResult.failure(
            callbackResult.getBizName(),
            callbackResult.getBizMsg(),
            callbackResult.getErrCode(),
            null,
            null);
      }
      UnifyResponse response = new UnifyResponse();
      response.setTransactionCode(loanTrade.getTransactionCode());
      response.setPayServeBizType("query_loan");
      JSONObject j = callbackResult.getBusinessObject();
      j.put("channelCode", ChannelConstant.BLUEPAY_CHANNEL_CODE);
      j.put("channelBizCode", ChannelConstant.BLUEPAY_BIZ_QUERY_LOAN_DETAIL);
      response.setBizParams(j);
      return CallbackResult.success("bluepay放款详细查询", "成功", response);
    }

    return CallbackResult.failure(
        "bluepay查询", "未找到支持的查询业务", StatusEnum.BLUEPAY_BIZ_NOT_FOUND.getCode(), null, null);
  }

  /**
   * bluepay放款详情查询接口的http请求
   *
   * @param loanTrade 放款详细表entity
   * @return
   */
  public CallbackResult<JSONObject> doQueryBluePayFintechDetail(LoanTrade loanTrade) {
    // boolean prodFlag = this.isProd(loanTrade.getExtend());
    String productId = this.productId;
    String operatorId = this.operatorId;
    String url = this.fintechQueryUrl;
    String encrypt = this.getQueryEncrypt(loanTrade.getTransactionCode(), productId, operatorId);
    String queryString =
        "operatorId="
            + operatorId
            + "&productid="
            + productId
            + "&t_id="
            + loanTrade.getTransactionCode()
            + "&encrypt="
            + encrypt;

    url = url + "?" + queryString;

    ResponseEntity<String> entry = null;
    try {
      entry = restTemplate.getForEntity(url, String.class);
    } catch (HttpClientErrorException e) {
      logger.error(
          "bluepay查询http请求异常,transactionCode:{},url:{}", loanTrade.getTransactionCode(), url, e);
      e.printStackTrace();
      return CallbackResult.failure(
          "bluepay放款详细查询", "http请求失败", StatusEnum.HTTP_REQUEST_ERROR.getCode(), null, null);
    }
    if (entry.getStatusCodeValue() != 200) {
      return CallbackResult.failure(
          "bluepay放款详细查询", "http响应码不是200", StatusEnum.HTTP_REQUEST_ERROR.getCode(), null, null);
    }
    JSONObject j = JSONObject.parseObject(entry.getBody());
    if (!"200".equals(j.getString("code"))) {
      return CallbackResult.failure(
          "bluepay放款详细查询", "code不是200", StatusEnum.HTTP_REQUEST_ERROR.getCode(), j, null);
    }
    return CallbackResult.success("bluepay收款详细查询", "查询成功", j);
  }

  /**
   * bluepay收款接口详细查询的http请求
   *
   * @param gatherTrade
   * @return
   */
  public CallbackResult<JSONObject> doQueryBluePayGatherDetail(GatherTrade gatherTrade) {
    // boolean prodFlag = this.isProd(gatherTrade.getExtend());
    String productId = this.productId;
    String operatorId = this.operatorId;
    String url = this.gatherQueryUrl;
    String encrypt = this.getQueryEncrypt(gatherTrade.getTransactionCode(), productId, operatorId);
    String queryString =
        "operatorId="
            + operatorId
            + "&productid="
            + productId
            + "&t_id="
            + gatherTrade.getTransactionCode()
            + "&encrypt="
            + encrypt;

    url = url + "?" + queryString;

    ResponseEntity<String> entry = null;
    try {
      entry = restTemplate.getForEntity(url, String.class);
    } catch (HttpClientErrorException e) {
      logger.error(
          "bluepay查询http请求异常,transactionCode:{},url:{}", gatherTrade.getTransactionCode(), url, e);
      e.printStackTrace();
      return CallbackResult.failure(
          "bluepay收款详细查询", "http请求失败", StatusEnum.HTTP_REQUEST_ERROR.getCode(), null, null);
    }
    if (entry.getStatusCodeValue() != 200) {
      return CallbackResult.failure(
          "bluepay收款详细查询", "http响应码不是200", StatusEnum.HTTP_REQUEST_ERROR.getCode(), null, null);
    }
    JSONObject j = JSONObject.parseObject(entry.getBody());
    if (!"200".equals(j.getString("status"))) {
      return CallbackResult.failure(
          "bluepay收款详细查询", "status不是200", StatusEnum.HTTP_REQUEST_ERROR.getCode(), j, null);
    }
    JSONObject child = JSONObject.parseObject(j.getString("result"));
    j.put("result", child);
    return CallbackResult.success("bluepay收款详细查询", "查询成功", j);
  }

  @Override
  public String getChannelCode() {
    return ChannelConstant.BLUEPAY_CHANNEL_CODE;
  }

  @Override
  public List<String> getSupportBizCodes() {
    return null;
  }

  /**
   * 验证bluepay收款接口参数
   *
   * @param params
   * @return
   */
  private CallbackResult validGatherParams(@NotNull UnifyGatherParams params) {
    if (params.getChannelSpecialParams() == null) {
      return CallbackResult.failure(
          "bluepay收款接口", "却少渠道特有参数", StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(), null, null);
    }
    // TODO 验证本渠道参数
    /** 2.验证接口的特有参数 */
    if (StringUtils.isBlank(NationZoneMap.getZoneByNation(params.getNation()))) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "不支持的nation值,目前支持ID和CN",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    if (!"sqs".equals(params.getNotifyType()) && !"http".equals(params.getNotifyType())) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "不支持的notifyType值,目前支持sqs和http",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    if (params.getOutTradeNo().length() > 128) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "outTradeNo长度必须<=128",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    if (!params.getChannelSpecialParams().containsKey("payType")) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "缺少渠道特有参数payType",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }

    /*if (!params.getChannelSpecialParams().containsKey("productId")) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "缺少渠道特有参数productId",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }*/
    /** 3. 验证通过 可以进行下一步请求操作 */
    return CallbackResult.success();
  }
  /**
   * 验证bluepay放款接口参数
   *
   * @param params
   * @return
   */
  private CallbackResult validLoanParams(@NotNull UnifyLoanParams params) {
    if (params.getChannelSpecialParams() == null) {
      return CallbackResult.failure(
          "bluepay收款接口", "却少渠道特有参数", StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(), null, null);
    }
    // TODO 验证本渠道参数
    /** 2.验证接口的特有参数 */
    if (StringUtils.isBlank(NationZoneMap.getZoneByNation(params.getNation()))) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "不支持的nation值,目前支持ID和CN",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    if (!"sqs".equals(params.getNotifyType()) && !"http".equals(params.getNotifyType())) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "不支持的notifyType值,目前支持sqs和http",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    if (params.getOutTradeNo().length() > 128) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "outTradeNo长度必须<=128",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    if (!params.getChannelSpecialParams().containsKey("payeeBankName")) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "缺少渠道特有参数payeeBackName",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    if (!params.getChannelSpecialParams().containsKey("payeeName")) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "缺少渠道特有参数payeeName",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    if (!params.getChannelSpecialParams().containsKey("payeeAccount")) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "缺少渠道特有参数payeeAccount",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    /* if (!params.getChannelSpecialParams().containsKey("productId")) {
      return CallbackResult.failure(
          "bluepay收款接口",
          "缺少渠道特有参数prductId",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }*/
    if (!"NORMAL".equals(params.getChannelSpecialParams().getString("payeeType"))
        && !"MERCHANT".equals(params.getChannelSpecialParams().getString("payeeType"))) {

      return CallbackResult.failure(
          "bluepay收款接口",
          "不支持的payeeType",
          StatusEnum.BLUEPAY_REQ_PARAMS_ERROR.getCode(),
          null,
          null);
    }

    /** 3. 验证通过 可以进行下一步请求操作 */
    return CallbackResult.success();
  }

  /**
   * 把map中的参数转为get请求的query串
   *
   * @return
   */
  public String map2QueryString(Map<String, String> map) {
    List<NameValuePair> params = new ArrayList<>();
    map.forEach(
        (key, value) -> {
          if (StringUtils.isNotBlank(value)) {
            params.add(new BasicNameValuePair(key, value));
          }
        });
    String str = "";
    // 转换为键值对
    try {
      str = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
    } catch (IOException e) {
      logger.error("map转为queryString出现异常,map:{}", map.toString());
      e.printStackTrace();
    }
    return str;
  }
}
