package com.panda.pay.util;

import java.util.UUID;

/** Created by jiangtao on 18-9-17 下午3:13 */
public class UUIDUtils {

  public static String getUUID32() {
    return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    //  return UUID.randomUUID().toString().replace("-", "").toLowerCase();
  }

  public static String getHash16() {
    int machineId = 1; // 最大支持1-9个集群机器部署
    int hashCodeV = UUID.randomUUID().toString().hashCode();
    if (hashCodeV < 0) { // 有可能是负数
      hashCodeV = -hashCodeV;
    }
    // 0 代表前面补充0
    // 4 代表长度为4
    // d 代表参数为正数型
    return machineId + String.format("%015d", hashCodeV);
  }

  public static String getRandom16() {
    String s1 = getHash16();
    String s2 = getHash16();
    String pre = s1.substring(8);
    String suf = s2.substring(8);
    return pre + suf;
  }
}
