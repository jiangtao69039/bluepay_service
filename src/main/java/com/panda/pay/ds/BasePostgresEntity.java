package com.panda.pay.ds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.persistence.*;

/**
 * @Title:BaseEntity @Copyright: Copyright (c) 2016 @Description: <br>
 * @Company: panda-fintech @Created on 2018/6/5下午3:49
 *
 * @miaoxuehui@panda-fintech.com
 */
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class BasePostgresEntity extends CommonEntity {

  /** 主键 * */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // @SequenceGenerator(name = "banner_id_seq", sequenceName = "banner_id_seq", allocationSize = 1)
  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banner_id_seq")
  @Column(name = "id")
  protected Long id;
  /** 创建时间 * */
  @Column(insertable = false, updatable = false)
  protected Date createdAt;
  /** 最后修改时间 * */
  @Column(insertable = false)
  public Date updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getCreatedAt() {
    if (createdAt == null) {
      return null;
    }
    return (Date) createdAt.clone();
  }

  public void setCreatedAt(Date createdAt) {
    if (createdAt == null) {
      createdAt = new Date();
    }
    this.createdAt = (Date) createdAt.clone();
  }

  public Date getUpdatedAt() {
    if (updatedAt == null) {
      return null;
    }
    return (Date) updatedAt.clone();
  }

  public void setUpdatedAt(Date updatedAt) {
    if (updatedAt == null) {
      updatedAt = new Date();
    }
    this.updatedAt = (Date) updatedAt.clone();
  }
}
