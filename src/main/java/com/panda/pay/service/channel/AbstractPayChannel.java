package com.panda.pay.service.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jiangtao on 19-1-24 下午8:27
 *
 * <p>具体各个渠道类的抽象父类 主要用于各个渠道对接时的公共操作 如数据库记录保存等
 */
public abstract class AbstractPayChannel implements PayChannelInterface {

  @Autowired protected RestTemplate restTemplate;
}
