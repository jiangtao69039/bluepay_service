package com.panda.pay.ds.primary.entity;

import com.panda.pay.config.jpa.MyUserType;
import com.panda.pay.ds.BasePostgresEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/** Created by jiangtao on 19-1-29 下午4:11 */
@Entity
@Getter
@Setter
@TypeDefs({@TypeDef(name = "json", typeClass = MyUserType.class)})
public class BizNotify extends BasePostgresEntity {

  String transactionCode;
  String outTradeNo;
  String notifyUrl;
  String notifyType;

  @Column(name = "biz_response", columnDefinition = "json")
  @Type(type = "json")
  String bizResponse;

  String notifyStatus;
  Integer notifyCount;

  @Column(name = "notify_params", columnDefinition = "json")
  @Type(type = "json")
  String notifyParams;
}
