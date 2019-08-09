package com.panda.pay.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @Title:WebMvcConfig @Copyright: Copyright (c) 2016 @Description: <br>
 * @Company: panda-fintech @Created on 2018/6/17下午2:55
 *
 * @miaoxuehui@panda-fintech.com
 */
// @Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 将templates目录下的CSS、JS文件映射为静态资源，防止Spring把这些资源识别成thymeleaf模版
    // registry.addResourceHandler("/templates/**.js").addResourceLocations("classpath:/templates/");
    // registry.addResourceHandler("/templates/**.css").addResourceLocations("classpath:/templates/");
    // 其他静态资源
    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
  }
}
