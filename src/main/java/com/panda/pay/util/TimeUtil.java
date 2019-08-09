package com.panda.pay.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/** Created by jiangtao on 18-10-15 下午10:52 */
public class TimeUtil {

  public static String RFC3339Time(Date date) {

    if (date == null) return "";
    return DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ss.SZZ");
  }

  public static String DateToRFC3339String(Date date) {

    return DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ss.SZZ");
  }

  /**
   * yyyy-MM-dd HH:mm:ss
   *
   * @param date
   * @return
   */
  public static String DateToTimeString(Date date) {
    if (date == null) return null;
    return DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
  }

  public static Date StringToDate(String s, String pat) throws ParseException {
    return DateUtils.parseDate(s, pat);
  }

  /**
   * yyyy-MM-dd HH:mm:ss
   *
   * @param date
   * @return
   */
  public static String DateToString(Date date, String pat) {
    if (date == null) return null;

    if (StringUtils.isBlank(pat)) return DateToTimeString(date);
    return DateFormatUtils.format(date, pat);
  }

  public static Date RFC3339StringToDate(String s1) throws ParseException {

    return DateUtils.parseDate(s1, "yyyy-MM-dd'T'HH:mm:ss.SZZ");
    /* ZonedDateTime dateTime1 = ZonedDateTime.parse("2018-10-27T11:32:26.553955473Z");
    System.out.println(dateTime1.toString());

    ZonedDateTime dateTime2 = ZonedDateTime.parse("2018-10-27T11:32:26.123445+08:00");
    System.out.println(dateTime2.toString());

    System.out.println(d);*/
  }

  /**
   * 获取年月日
   *
   * @return
   */
  public static String getDateByCalendar() {
    Calendar cal = Calendar.getInstance();
    String year = "" + cal.get(Calendar.YEAR); // 获取年份
    String month =
        (cal.get(Calendar.MONTH) + 1) < 10
            ? "0" + (cal.get(Calendar.MONTH) + 1)
            : "" + (cal.get(Calendar.MONTH) + 1); // 获取月份
    String day =
        cal.get(Calendar.DATE) < 10
            ? "0" + cal.get(Calendar.DATE)
            : "" + cal.get(Calendar.DATE); // 获取日
    StringBuilder sb = new StringBuilder();
    return sb.append(year).append(month).append(day).toString();
  }

  /**
   * 获取年月日时分秒
   *
   * @return
   */
  public static String getTimeByCalendar() {
    Calendar cal = Calendar.getInstance();
    String year = "" + cal.get(Calendar.YEAR); // 获取年份
    String month =
        (cal.get(Calendar.MONTH) + 1) < 10
            ? "0" + (cal.get(Calendar.MONTH) + 1)
            : "" + (cal.get(Calendar.MONTH) + 1); // 获取月份
    String day =
        cal.get(Calendar.DATE) < 10
            ? "0" + cal.get(Calendar.DATE)
            : "" + cal.get(Calendar.DATE); // 获取日
    String hour =
        cal.get(Calendar.HOUR_OF_DAY) < 10
            ? "0" + cal.get(Calendar.HOUR_OF_DAY)
            : "" + cal.get(Calendar.HOUR_OF_DAY); // 小时
    String minute =
        cal.get(Calendar.MINUTE) < 10
            ? "0" + cal.get(Calendar.MINUTE)
            : "" + cal.get(Calendar.MINUTE); // 分
    String second =
        cal.get(Calendar.SECOND) < 10
            ? "0" + cal.get(Calendar.SECOND)
            : "" + cal.get(Calendar.SECOND); // 秒
    //    int WeekOfYear = cal.get(Calendar.DAY_OF_WEEK);//一周的第几天
    //    System.out.println("现在的时间是：公元"+year+"年"+month+"月"+day+"日
    // "+hour+"时"+minute+"分"+second+"秒");
    StringBuilder sb = new StringBuilder();
    return sb.append(year)
        .append(month)
        .append(day)
        .append(hour)
        .append(minute)
        .append(second)
        .toString();
  }

  /**
   * 获取过去第几天的日期
   *
   * @param past
   * @return
   */
  public static Date getPastDate(int past) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
    return calendar.getTime();
  }
}
