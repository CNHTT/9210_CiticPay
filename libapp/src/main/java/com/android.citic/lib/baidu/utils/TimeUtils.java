package com.android.citic.lib.baidu.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间相关工具类 注意：操作单位使用(秒)
 * 简化方法<br />
 * 时间格式化，只保留 {@link TimeUtils#formatDate(Date, String)} 、{@link TimeUtils#getAbsoluteTime()} 、{@link TimeUtils#parseDate(String, String)}<br />
 * 如果需要格式其它的时间格式，请使用 {@link TimeUtils#formatTime(Date, String)} 和 {@link TimeUtils#parseDate(String, String)} 来生成，不必每种格式都写一个方法 <br />
 * 同时，类中添加了{@link TimeUtils#FORMAT_STANDARD} 和 {@link TimeUtils#FORMAT_SERIALIZABLE} 两个常量，分别指向 "yyyy-MM-dd HH:mm:ss" 和 "yyyyMMddHHmmss"
 */
public class TimeUtils {
	
	public final static String FORMAT_STANDARD= "yyyy-MM-dd HH:mm:ss";
	public final static String FORMAT_SERIALIZABLE = "yyyyMMddHHmmss";

	/** 星座日期 **/
	private final static int[] dayArr = new int[] { 20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22 };
	private final static String[] constellationArr = new String[] { "摩羯座", "水瓶座", "双鱼座", "白羊座", 
			"金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };
	/**
	 * 将秒转为HH:mm:ss
	 * @param second
	 * @return
	 */
	public static String getMediaTime(int second) {
		return getMediaTime((long)second);
	}
	/**
	 * 将秒转为HH:mm:ss
	 * @param second
	 * @return
	 */
	public static String getMediaTime(long second) {
		long h = 0;
		long d = 0;
		long s = 0;
		long temp = second % 3600;
		if (second > 3600) {
			h = second / 3600;
			if (temp != 0) {
				if (temp > 60) {
					d = temp / 60;
					if (temp % 60 != 0) {
						s = temp % 60;
					}
				} else {
					s = temp;
				}
			}
		} else {
			d = second / 60;
			if (second % 60 != 0) {
				s = second % 60;
			}
		}

		StringBuffer time = new StringBuffer();
		if (h != 0) {
			if (h < 10) {
				time.append("0");
			}
			time.append(h).append(":");
		}
		if (d != 0) {
			if (d < 10) {
				time.append("0");
			}
			time.append(d);
		} else {
			time.append("00");
		}
		time.append(":");
		if (s != 0) {
			if (s < 10) {
				time.append("0");
			}
			time.append(s);
		} else {
			time.append("00");
		}

		return time.toString();
	}
	
	/**
	 * 获取绝对时间(系统当前时间)
	 * 
	 * @return 时间格式为"yyyyMMddHHmmss"
	 */ 
	public static String getAbsoluteTime() {
		return formatDate(new Date(), FORMAT_SERIALIZABLE);
	}
	
	public static String getCurrentDate() {
		return formatDate(new Date(), FORMAT_STANDARD);
	}
	
	/**
	 * 格式化日期
	 * @param date
	 * @param format yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
		return sdf.format(date);
	}
	
	/**
	 * 格式化银联标注日期（将yyyyMMddHHmmss日期格式转换为指定格式）
	 * @param oldDate 
	 * @param format
	 * @return
	 */
	public static String formatUnionpayDate(String oldDate, String format) {
		return changeTimeFormat(oldDate, FORMAT_SERIALIZABLE, format);
	}
	
	public static String changeTimeFormat(String dateStr, String oldFormat, String targetFormat) {
		return formatDate(parseDate(dateStr, oldFormat), targetFormat);
	}
	
	public static Date parseDate(String dateStr, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
		Date d = null;
		try {
			 d = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * 获取相对时间(将给点的时间变换成相对于系统当前时间的差值)，格式为“XX分钟前”
	 * @param time 格式为：8888888888888
	 * @return 格式为“XX分钟前”
	 */
	public static String getRelativeTime(long time) {
		String strTime = "";
		Date dt1 = new Date(time);

		Calendar cl = Calendar.getInstance();
		int year2 = cl.get(Calendar.YEAR);
		int month2 = cl.get(Calendar.MONTH);
		int day2 = cl.get(Calendar.DAY_OF_MONTH);
		int hour2 = cl.get(Calendar.HOUR_OF_DAY);
		int minute2 = cl.get(Calendar.MINUTE);
		
		cl.setTime(dt1);
		int year1 = cl.get(Calendar.YEAR);
		int month1 = cl.get(Calendar.MONTH);
		int day1 = cl.get(Calendar.DAY_OF_MONTH);
		int hour1 = cl.get(Calendar.HOUR_OF_DAY);
		int minute1 = cl.get(Calendar.MINUTE);
		
		if (year1 != year2) {
			strTime = year1 + "年" + month1 + "月" + day1;
		} else if (month1 != month2) {
			strTime =  (month1 + 1) + "月" + day1 + "日";
		} else if (day1 == day2) {
			if (hour1 == hour2) {
				strTime = (minute1 >= minute2) ? "刚才" : (minute2 - minute1) + "分钟前";
			} else if ((hour2 - hour1) == 1) {
				strTime = (minute2 - minute1 > 0) ? "1小时前" : (60 + minute2 - minute1) + "分钟前";
			} else {
				strTime = (hour2 - hour1) + "小时前";
			}
		} else if ((day2 - day1) == 1) { // 昨天
				strTime = "昨天";//(month1 + 1) + "月" + day1 + "日 ";
		} else {
			strTime = (month1 + 1) + "月" + day1 + "日";
		}
		return strTime;
	}
	
	/**
	 * 获取传递进来的时间与当前时间相差多少
	 * @param time
	 * @return
	 */
	public static String getSurplusTime(long time) {
		long t = time - System.currentTimeMillis();

		int day = 0;
		int hour = 0;
		int min = 0;
		int second = 0;

		long d = 60 * 60 * 24;// 天
		long h = 60 * 60;// 小时
		long m = 60;// 分钟

		// 计算天
		if (t > day) {
			day = (int) (t / d);
			t = t % d;
		}
		// 计算小时
		if (t > h) {
			hour = (int) (t / h);
			t = t % h;
		}
		// 计算分钟
		if (t > m) {
			min = (int) (t / m);
			t = t % m;
		}
		// 剩余秒数
		second = (int) t;
		
		String td = day <= 0 ? "" : (day + "天");
		String th = hour <= 0 ? "" : (hour + "小时");
		String tm = min <= 0 ? "" : (min + "分");
		String ts = second <= 0 ? "预约时间已过" : second +"秒";
		
		String str = "";
		if (second > 0) {
			str = "剩" + td + th + tm + ts;
		}else{
			str = td + th + tm + ts;
		}
		return str;
	}
	

	/**
	 * 根据出生日期返回年龄
	 * @param birthday 格式为：8888888888888
	 * @return
	 */
	public static int getAge(long time) {
        Calendar cal = Calendar.getInstance();
        Date birthday = new Date(time);	
        if (cal.before(birthday)) {
        	//LoggerUtils.e("出生时间:" + formatDate(birthday, FORMAT_STANDARD) + ", 大于当前时间!");
        	return 0;
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;//注意此处，如果不加1的话计算结果是错误的
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthday);

        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        } 
        return age;
    }
	

	/**
	 * 根据月和日获取星座
	 * 
	 * @param month
	 * @param day
	 * @return
	 */
	public static String getConstellation(int month, int day) {
		return day < dayArr[month - 1] ? constellationArr[month - 1] : constellationArr[month];
	}
	
	/**
	 * 比较格式为(08:00)的时间
	 * 
	 * @usage compareTime("08:00", "13:28")
	 * @param time1
	 * @param time2
	 * @return
     *     -1 :  time1 > time2   
     *     0  :  time1 = time2   
     *     1  :  time1 < time2   
	 */
//    public static int compareTime(String time1, String time2) {
//        if (StringUtils.isEmpty(time1)) {
//            return 1;
//        } else if (StringUtils.isEmpty(time2)) {
//            return -1;
//        }
//    
//        String[] timeArray1 = time1.split(":");
//        String[] timeArray2 = time2.split(":");
//        
//        int hourtime1 = Integer.parseInt(timeArray1[0]);
//        int minutetime1 = Integer.parseInt(timeArray1[1]);
//        
//        int hourtime2 = Integer.parseInt(timeArray2[0]);
//        int minutetime2 = Integer.parseInt(timeArray2[1]);
//        
//        return compareHourMinute(hourtime1, minutetime1, hourtime2, minutetime2);
//    }
    
    /**
	 * 比较格式为(08:00)的时间
	 * 
	 * @usage compareHourMinute(calendar1, calendar2)
	 * @param calendar1
	 * @param calendar2
	 * @return
     *     -1 :  time1 < time2   
     *     0  :  time1 = time2   
     *     1  :  time1 > time2   
	 */
    public static int compareCalendarTime(long time1, long time2) {
    	Calendar calendar1 =Calendar.getInstance();
    	calendar1.setTimeInMillis(time1);
    	Calendar calendar2 =Calendar.getInstance();
    	calendar2.setTimeInMillis(time2);
    	
    	return calendar1.compareTo(calendar2);
    }
    
	/**
	 * 比较格式为(08:00)的时间
	 * 
	 * @usage compareHourMinute(calendar1, calendar2)
	 * @param calendar1
	 * @param calendar2
	 * @return
     *     -1 :  time1 > time2   
     *     0  :  time1 = time2   
     *     1  :  time1 < time2   
	 */
    public static int compareHourMinute(Calendar calendar1, Calendar calendar2) {
        if (calendar1 == null) {
            return 1;
        } else if (calendar2 == null){
            return -1;
        }
    
        int hourtime1 = calendar1.get(Calendar.HOUR_OF_DAY);
        int minutetime1 = calendar1.get(Calendar.MINUTE);
        
        int hourtime2 = calendar2.get(Calendar.HOUR_OF_DAY);
        int minutetime2 = calendar2.get(Calendar.MINUTE);
        
        return compareHourMinute(hourtime1, minutetime1, hourtime2, minutetime2);
    }
    
    private static int compareHourMinute(int hourtime1, int minutetime1, int hourtime2, int minutetime2) {
        if (hourtime1<hourtime2) {//先判断小时
            return 1;
        } else if (hourtime1>hourtime2) {
            return -1;
        } else if (minutetime1 < minutetime2) {//小时一样时判断分钟
            return 1;
        } else if (minutetime1 > minutetime2) {
            return -1;
        } else {//最后剩下一样的时候
            return 0;
        }
    }
    
	/**
	 * 获取当前的系统年份
	 * @return
	 */
	public static int getCurrentYear(){
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.YEAR);
	}
	
	
	/**
	 * 日期和时间格式化 24小制
	 * 
	 * @param date
	 * @param time
	 * @return
	 */
	public static String timeFormat(String date, String time) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
		String reslt = "";
		try {
			Date date2 = simpleDateFormat2.parse(
					String.valueOf(Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR)) +date+ time);
			
			reslt = simpleDateFormat.format(date2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return reslt;
	}

	/**
	 * 从格式"yyyy/MM/dd hh:mm:ss"中获取MMdd
	 */
	public static String getDate(String formatDate) {
		return formatDate.substring(5, 7) + formatDate.substring(8, 10);
	}
	
}
