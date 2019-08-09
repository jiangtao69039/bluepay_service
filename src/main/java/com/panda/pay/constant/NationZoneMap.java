package com.panda.pay.constant;

import java.util.HashMap;
import java.util.Map;

/** Created by jiangtao on 19-1-28 上午9:50 国家与区号的对应关系 两位的国家代码缩写 */
public class NationZoneMap {

  static final Map<String, String> map;

  static {
    map = new HashMap<>(4, 1.0f);
    map.put("ID", "62");
    map.put("CN", "86");
  }

  public static String getZoneByNation(String n) {
    return map.get(n);
  }
}
