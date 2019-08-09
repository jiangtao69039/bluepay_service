package com.panda.pay.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Title:GlobalDefaultExceptionHandler @Copyright: Copyright (c) 2016 @Description: <br>
 * @Company: panda-fintech @Created on 2018/6/22上午11:21
 *
 * @miaoxuehui@panda-fintech.com
 */
@ControllerAdvice
public class GlobalDefaultExceptionHandler {

  private Logger logger = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseData exceptionHandler(Exception e) {
    logger.error(e.getMessage(), e);
    return ResponseData.unifyException(e.getMessage() == null ? e.getClass() + "" : e.getMessage());
  }
}
