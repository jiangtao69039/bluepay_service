package com.panda.pay.ds.primary.repository;

import com.panda.pay.ds.primary.entity.BizNotify;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/** Created by jiangtao on 19-1-29 下午4:14 */
public interface BizNotifyRepository
    extends PagingAndSortingRepository<BizNotify, Long>, JpaSpecificationExecutor {

  BizNotify findByTransactionCode(String code);
}
