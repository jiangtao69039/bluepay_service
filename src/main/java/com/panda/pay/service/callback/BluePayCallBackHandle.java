package com.panda.pay.service.callback;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.panda.pay.constant.enums.StatusEnum;
import com.panda.pay.controller.params.BluePayCallBackParams;
import com.panda.pay.dao.GatherTradeDao;
import com.panda.pay.dao.LoanTradeDao;
import com.panda.pay.dao.PayTradeDao;
import com.panda.pay.ds.primary.entity.GatherTrade;
import com.panda.pay.ds.primary.entity.LoanTrade;
import com.panda.pay.ds.primary.entity.PayTrade;
import com.panda.pay.ds.primary.repository.GatherTradeRepository;
import com.panda.pay.ds.primary.repository.LoanTradeRepository;
import com.panda.pay.framework.CallbackResult;
import com.panda.pay.service.notify.NotifyBizRequestParams;
import com.panda.pay.service.notify.NotifyBizServiceImpl;
import com.panda.pay.util.TimeUtil;
import java.util.Date;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by jiangtao on 19-1-28 下午3:50
 *
 * <p>bluepay回调通知处理类
 */
@Service("bluePayCallBackHandle")
public class BluePayCallBackHandle {

  @Autowired NotifyBizServiceImpl notifyBizService;
  @Autowired PayTradeDao payTradeDao;
  @Autowired GatherTradeDao gatherTradeDao;
  @Autowired LoanTradeDao loanTradeDao;
  @Autowired GatherTradeRepository gatherTradeRepository;
  @Autowired LoanTradeRepository loanTradeRepository;

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
  /**
   * bluepay回调处理总入口
   *
   * @param params bluepay回调的参数
   * @return 处理结果
   */
  public CallbackResult<?> handle(@NotNull BluePayCallBackParams params) {
    PayTrade payTrade = payTradeDao.findByTransactionCode(params.getT_id());
    if (payTrade == null) {
      return CallbackResult.failure(
          "bluepay回调处理", "pay_trade表记录未找到", StatusEnum.PAYTRADE_NOT_FOUND.getCode(), null, null);
    }

    if (payTrade.getChannelBizCode().contains("gather")) {
      // shou款
      CallbackResult callbackResult = this.handleGather(params);
      if (callbackResult.isSuccess()) {
        return CallbackResult.success();
      } else {
        return CallbackResult.failure(
            callbackResult.getBizName(),
            callbackResult.getBizMsg(),
            callbackResult.getErrCode(),
            callbackResult.getErrCode(),
            callbackResult.getThrowable());
      }
    }
    if (payTrade.getChannelBizCode().contains("loan")) {
      // 放款
      CallbackResult callbackResult = this.handleLoan(params);
      if (callbackResult.isSuccess()) {
        return CallbackResult.success();
      } else {
        return CallbackResult.failure(
            callbackResult.getBizName(),
            callbackResult.getBizMsg(),
            callbackResult.getErrCode(),
            callbackResult.getErrCode(),
            callbackResult.getThrowable());
      }
    }
    return CallbackResult.failure(
        "bluepay回调处理", "不支持的业务类型", StatusEnum.BLUEPAY_BIZ_NOT_FOUND.getCode(), null, null);
  }

  /**
   * 处理放款回调
   *
   * @param params
   * @return
   */
  public CallbackResult<?> handleLoan(@NotNull BluePayCallBackParams params) {
    LoanTrade loanTrade = loanTradeDao.findByTransactionCode(params.getT_id());
    if (loanTrade == null) {
      return CallbackResult.failure(
          "bluepay回调处理",
          "loan_trade表记录未找到",
          StatusEnum.GATHERTRADE_OR_LOANTRADE_NOT_FOUND.getCode(),
          null,
          null);
    }
    if (!params.getCurrency().equals(loanTrade.getCurrency())
        && !"2257".equals(params.getProductid())) {
      return CallbackResult.failure(
          "bluepay回调处理", "currency不匹配", StatusEnum.CURRENCY_NOT_MATCH.getCode(), null, null);
    }
    if (!params.getPrice().equals(loanTrade.getPrice() + "")
        && !"2257".equals(params.getProductid())) {
      return CallbackResult.failure(
          "bluepay回调处理", "price不匹配", StatusEnum.PRICE_NOT_MATCH.getCode(), null, null);
    }

    // 修改详细表数据
    loanTrade = updateBluePayLoanCallback(loanTrade, params);

    if ("200".equals(params.getStatus())) {

      // 交易成功了,通知业务系统8
      NotifyBizRequestParams notifyBizRequestParams =
          this.getNotifyBizReqParamsFromTrade(loanTrade, params);
      if (notifyBizRequestParams == null) {
        return CallbackResult.failure(
            "bluepayGather回调处理",
            "生成统一回调业务系统的参数失败,不支持的类类型",
            StatusEnum.GET_NOTIFY_BIZ_PARAMS_ERROR.getCode(),
            null,
            null);
      }
      notifyBizService.notifyBizLoan(loanTrade, notifyBizRequestParams);
      //
      return CallbackResult.success();
    }
    if ("201".equals(params.getStatus())) {
      // 状态待定,不用通知业务系统
      return CallbackResult.success();
    }
    // 其他状态,全是失败.得通知业务系统失败啦
    NotifyBizRequestParams notifyBizRequestParams =
        this.getNotifyBizReqParamsFromTrade(loanTrade, params);
    if (notifyBizRequestParams == null) {
      return CallbackResult.failure(
          "bluepayGather回调处理",
          "生成统一回调业务系统的参数失败,不支持的类类型",
          StatusEnum.GET_NOTIFY_BIZ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    notifyBizService.notifyBizLoan(loanTrade, notifyBizRequestParams);
    return CallbackResult.failure(
        "bluepay回调处理",
        "错误信息请参阅bluepay错误码",
        StatusEnum.BLUEPAY_GATHER_CALLBACK_ERROR.getCode(),
        null,
        null);
  }

  /**
   * 处理收款回调
   *
   * @param params
   * @return
   */
  public CallbackResult<?> handleGather(@NotNull BluePayCallBackParams params) {
    GatherTrade gatherTrade = gatherTradeDao.findByTransactionCode(params.getT_id());

    if (gatherTrade == null) {
      return CallbackResult.failure(
          "bluepay回调处理",
          "gather_trade表记录未找到",
          StatusEnum.GATHERTRADE_OR_LOANTRADE_NOT_FOUND.getCode(),
          null,
          null);
    }
    // System.out.println("callback第一次find记录:" + gatherTrade.toString());
    if (!params.getCurrency().equals(gatherTrade.getCurrency())
        && !"2257".equals(params.getProductid())) {
      return CallbackResult.failure(
          "bluepay回调处理", "currency不匹配", StatusEnum.CURRENCY_NOT_MATCH.getCode(), null, null);
    }
    if (!params.getPrice().equals(gatherTrade.getPrice() + "")
        && !"2257".equals(params.getProductid())) {
      return CallbackResult.failure(
          "bluepay回调处理", "price不匹配", StatusEnum.PRICE_NOT_MATCH.getCode(), null, null);
    }

    // 修改详细表数据
    gatherTrade = updateBluePayGatherCallback(gatherTrade, params);
    // System.out.println("updateCallback记录后:" + gatherTrade.toString());
    if ("200".equals(params.getStatus())) {

      // 交易成功了,通知业务系统8
      NotifyBizRequestParams notifyBizRequestParams =
          this.getNotifyBizReqParamsFromTrade(gatherTrade, params);
      if (notifyBizRequestParams == null) {
        return CallbackResult.failure(
            "bluepayGather回调处理",
            "生成统一回调业务系统的参数失败,不支持的类类型",
            StatusEnum.GET_NOTIFY_BIZ_PARAMS_ERROR.getCode(),
            null,
            null);
      }
      notifyBizService.notifyBizGather(gatherTrade, notifyBizRequestParams);
      //
      return CallbackResult.success();
    }
    if ("201".equals(params.getStatus())) {
      // 状态待定,不用通知业务系统
      return CallbackResult.success();
    }
    // 其他状态,全是失败.得通知业务系统失败啦
    NotifyBizRequestParams notifyBizRequestParams =
        this.getNotifyBizReqParamsFromTrade(gatherTrade, params);
    if (notifyBizRequestParams == null) {
      return CallbackResult.failure(
          "bluepayGather回调处理",
          "生成统一回调业务系统的参数失败,不支持的类类型",
          StatusEnum.GET_NOTIFY_BIZ_PARAMS_ERROR.getCode(),
          null,
          null);
    }
    notifyBizService.notifyBizGather(gatherTrade, notifyBizRequestParams);
    return CallbackResult.failure(
        "bluepay回调处理",
        "错误信息请参阅bluepay错误码",
        StatusEnum.BLUEPAY_GATHER_CALLBACK_ERROR.getCode(),
        null,
        null);
  }

  /**
   * 从详细表entity中生成 回调业务系统的信息
   *
   * @param trade 详细表entity
   * @param params bluepay回调信息
   * @return 回调业务系统的信息
   */
  public NotifyBizRequestParams getNotifyBizReqParamsFromTrade(
      Object trade, @NotNull BluePayCallBackParams params) {
    if (trade instanceof GatherTrade) {
      GatherTrade gatherTrade = (GatherTrade) trade;
      NotifyBizRequestParams notifyBizRequestParams = new NotifyBizRequestParams();
      notifyBizRequestParams.setTransactionCode(gatherTrade.getTransactionCode());
      notifyBizRequestParams.setOutTradeNo(gatherTrade.getOutTradeNo());
      notifyBizRequestParams.setChannelCode(gatherTrade.getChannelCode());
      notifyBizRequestParams.setChannelBizCode(gatherTrade.getChannelBizCode());
      notifyBizRequestParams.setPassBack(
          Optional.ofNullable(gatherTrade.getPassBack()).map(JSONObject::parseObject).orElse(null));
      notifyBizRequestParams.setTradeStatus(gatherTrade.getTradeStatus());
      notifyBizRequestParams.setTradeMsg(gatherTrade.getTradeMsg());
      notifyBizRequestParams.setOriginChannelCallBack(
          JSONObject.parseObject(JSON.toJSONString(params)));
      return notifyBizRequestParams;
    }
    if (trade instanceof LoanTrade) {
      LoanTrade gatherTrade = (LoanTrade) trade;
      NotifyBizRequestParams notifyBizRequestParams = new NotifyBizRequestParams();
      notifyBizRequestParams.setTransactionCode(gatherTrade.getTransactionCode());
      notifyBizRequestParams.setOutTradeNo(gatherTrade.getOutTradeNo());
      notifyBizRequestParams.setChannelCode(gatherTrade.getChannelCode());
      notifyBizRequestParams.setChannelBizCode(gatherTrade.getChannelBizCode());
      notifyBizRequestParams.setPassBack(
          Optional.ofNullable(gatherTrade.getPassBack()).map(JSONObject::parseObject).orElse(null));
      notifyBizRequestParams.setTradeStatus(gatherTrade.getTradeStatus());
      notifyBizRequestParams.setTradeMsg(gatherTrade.getTradeMsg());
      notifyBizRequestParams.setOriginChannelCallBack(
          JSONObject.parseObject(JSON.toJSONString(params)));
      return notifyBizRequestParams;
    }
    return null;
  }

  /**
   * 根据bluepay回调信息,更新收款详细表
   *
   * @param gatherTrade 详细表eneity
   * @param params bluepay回调信息
   * @return 更新后的entity
   */
  public GatherTrade updateBluePayGatherCallback(
      @NotNull GatherTrade gatherTrade, @NotNull BluePayCallBackParams params) {

    /*System.out.println(
        "BluepayCallBackHandle-updateBluePayGatherCallback1:" + gatherTrade.toString());
    */
    Integer notifyCount =
        Optional.ofNullable(gatherTrade.getChannelCallbackNotifyStatus())
            .map(Integer::parseInt)
            .orElse(0);
    notifyCount++;
    gatherTrade.setChannelCallbackNotifyStatus(notifyCount + "");

    gatherTrade.setTradeStatus("fail");
    gatherTrade.setTradeMsg("失败,请查阅bluepay回调错误码:" + params.getStatus());
    if ("200".equals(params.getStatus())) {
      gatherTrade.setTradeStatus("success");
      gatherTrade.setReceivePrice(Integer.valueOf(params.getPrice()));
      // System.out.println("---------\n" + params.getPrice() + "------------\n");
      gatherTrade.setTradeMsg("交易成功");
    }
    if ("201".equals(params.getStatus())) {
      gatherTrade.setTradeStatus("process");
      gatherTrade.setTradeMsg("交易进行中");
    }
    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(params));
    jsonObject.put("notifyTime", TimeUtil.DateToRFC3339String(new Date()));
    // gatherTrade.setChannelCallbackExtendInfo(JSON.toJSONString(params));
    JSONArray array =
        Optional.ofNullable(gatherTrade.getChannelCallbackExtendInfo())
            .map(JSONArray::parseArray)
            .orElseGet(JSONArray::new);
    array.add(jsonObject);
    gatherTrade.setChannelCallbackExtendInfo(array.toJSONString());
    gatherTrade.setUpdatedAt(new Date());
    /*System.out.println(
    "回调201交易进行中保存时间:"
        + TimeUtil.DateToRFC3339String(new Date())
        + "\n"
        + gatherTrade.toString());*/

    /*System.out.println(
    "BluepayCallBackHandle-updateBluePayGatherCallback2:" + gatherTrade.toString());*/

    return gatherTradeRepository.save(gatherTrade);
  }

  /**
   * 根据bluepay回调信息,更新放款详细表
   *
   * @param loanTrade 详细表eneity
   * @param params bluepay回调信息
   * @return 更新后的entity
   */
  public LoanTrade updateBluePayLoanCallback(
      @NotNull LoanTrade loanTrade, @NotNull BluePayCallBackParams params) {

    Integer notifyCount =
        Optional.ofNullable(loanTrade.getChannelCallbackNotifyStatus())
            .map(Integer::parseInt)
            .orElse(0);
    notifyCount++;
    loanTrade.setChannelCallbackNotifyStatus(notifyCount + "");

    loanTrade.setTradeStatus("fail");
    loanTrade.setTradeMsg("失败,请查阅bluepay回调错误码:" + params.getStatus());
    if ("200".equals(params.getStatus())) {
      loanTrade.setTradeStatus("success");
      loanTrade.setTradeMsg("交易成功");
    }
    if ("201".equals(params.getStatus())) {
      loanTrade.setTradeStatus("process");
      loanTrade.setTradeMsg("交易进行中");
    }
    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(params));
    jsonObject.put("notifyTime", TimeUtil.DateToRFC3339String(new Date()));
    // gatherTrade.setChannelCallbackExtendInfo(JSON.toJSONString(params));
    JSONArray array =
        Optional.ofNullable(loanTrade.getChannelCallbackExtendInfo())
            .map(JSONArray::parseArray)
            .orElseGet(JSONArray::new);
    array.add(jsonObject);
    loanTrade.setChannelCallbackExtendInfo(array.toJSONString());
    loanTrade.setUpdatedAt(new Date());
    return loanTradeRepository.save(loanTrade);
  }
}
