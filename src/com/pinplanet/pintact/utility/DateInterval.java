package com.pinplanet.pintact.utility;

import java.util.Calendar;

/**
 * Convert a timestamp into a interval based label
 * 
 * @author Erik Hayes
 */
public class DateInterval {
  
  private static String[] intervals = new String[]{
    "1 HOUR", "1 DAY (24 HOURS)",
    "2 DAYS", "3 DAYS",
    "1 WEEK", "2 WEEKS", "3 WEEKS",
    "1 MONTH"
  };
  
  public static String[] getLabels() {
    return intervals;
  }
  
  public static int getIndexForTimeInMillis(long timeInMillis) {
    Calendar expireCalendar = Calendar.getInstance();
    expireCalendar.setTimeInMillis(timeInMillis);
    
    int index = 0;
    Calendar intervalCalendar = Calendar.getInstance();
    intervalCalendar.add(Calendar.DAY_OF_MONTH, 1);
    if (intervalCalendar.getTimeInMillis() < expireCalendar.getTimeInMillis()) {
      // 2 days
      index = 2;
      intervalCalendar.add(Calendar.DAY_OF_MONTH, 1);
      if (intervalCalendar.getTimeInMillis() < expireCalendar.getTimeInMillis()) {
        // 3 days
        index = 3;
        intervalCalendar.add(Calendar.DAY_OF_MONTH, 1);
        if (intervalCalendar.getTimeInMillis() < expireCalendar.getTimeInMillis()) {
          // 1 week
          index = 4;
          intervalCalendar.add(Calendar.DAY_OF_MONTH, 4);
          if (intervalCalendar.getTimeInMillis() < expireCalendar.getTimeInMillis()) {
            // 2 weeks
            index = 5;
            intervalCalendar.add(Calendar.DAY_OF_MONTH, 7);
            if (intervalCalendar.getTimeInMillis() < expireCalendar.getTimeInMillis()) {
              // 3 weeks
              index = 6;
              intervalCalendar.add(Calendar.DAY_OF_MONTH, 7);
              if (intervalCalendar.getTimeInMillis() < expireCalendar.getTimeInMillis()) {
                // 1 month
                index = 7;
              }
            }
          }
        }
      }
    }
    return index;
  }
  
  public static Long getTimeInMillisForIndex(int index) {
    Calendar cal = Calendar.getInstance();        
    switch (index) {
    case 0:
      cal.add(Calendar.HOUR, 1);
      break;
    case 1:
      cal.add(Calendar.DAY_OF_MONTH, 1);
      break;
    case 2:
      cal.add(Calendar.DAY_OF_MONTH, 2);
      break;
    case 3:
      cal.add(Calendar.DAY_OF_MONTH, 3);
      break;
    case 4:
      cal.add(Calendar.WEEK_OF_YEAR, 1);
      break;
    case 5:
      cal.add(Calendar.WEEK_OF_YEAR, 2);
      break;
    case 6:
      cal.add(Calendar.WEEK_OF_YEAR, 3);
      break;
    case 7:
      cal.add(Calendar.MONTH, 1);
      break;
    default:
      return null;
    }
    return cal.getTimeInMillis();
  }

}
