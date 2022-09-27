package com.elexlab.myalbum.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by BruceYoung on 10/17/17.
 */
public class FormatUtils {
    public static long mbToB(float mb){
        float bSize = 1024*1024*mb;
        return (long) bSize;
    }

    public static float bToMB(long b){
        float mbSize = (float)b/(float)(1024*1024);
        return mbSize;
    }

    public static String timeStampToFormatStr(String format,long timeStamp){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(new Date(timeStamp));
    }

    public static String floatToPercent(float percent){
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(percent)+"%";
    }

    /**
     *
     * @param duration mil second
     * @return
     */
    public static String parseTimeToDuration(int duration){
        duration = duration/1000;
        int hour = duration/(60*60);
        int min = duration/60;
        int second = duration%60;
        String durationStr = (hour<=0?"":((hour<10?"0":"")+hour+":"))+(min<10?"0":"")+min+":"+(second<10?"0":"")+second;
        return durationStr;
    }
}
