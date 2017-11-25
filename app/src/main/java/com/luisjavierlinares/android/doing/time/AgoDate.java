package com.luisjavierlinares.android.doing.time;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Luis on 27/03/2017.
 */

public class AgoDate {

    public static enum DateScope {SECONDS, MINUTES, HOURS, DAYS, MONTHS, YEARS};

    private static final int SECONDS_IN_A_MINUTE = 60;
    private static final int MINUTES_IN_A_HOUR = 60;
    private static final int HOURS_IN_A_DAY = 24;
    private static final int DAYS_IN_A_MONTH = 30;
    private static final int MONTHS_IN_A_YEAR = 12;

    private int mValue;
    private DateScope mScope;

    public static AgoDate getAgoDate(Date date) {
        return getAgoDate(date, Calendar.getInstance().getTime());
    }

    public static AgoDate getAgoDate(Date iniDate, Date endDate) {

        Calendar iniCalendar = Calendar.getInstance();
        iniCalendar.setTime(iniDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        long miliseconds_ago = endDate.getTime() - iniDate.getTime();
        int seconds_ago = (int) (miliseconds_ago / 1000);
        int minutes_ago = seconds_ago / SECONDS_IN_A_MINUTE;
        int hours_ago = minutes_ago / MINUTES_IN_A_HOUR;
        int days_ago = hours_ago / HOURS_IN_A_DAY;
        int months_ago = days_ago / DAYS_IN_A_MONTH;
        int years_ago = months_ago / MONTHS_IN_A_YEAR;

        if (years_ago != 0) {
            return new AgoDate(years_ago, AgoDate.DateScope.YEARS);
        } else if (months_ago != 0) {
            return new AgoDate(months_ago, AgoDate.DateScope.MONTHS);
        } else if (days_ago != 0) {
            return new AgoDate(days_ago, AgoDate.DateScope.DAYS);
        } else if (hours_ago != 0) {
            return new AgoDate(hours_ago, AgoDate.DateScope.HOURS);
        } else if (minutes_ago != 0) {
            return new AgoDate(minutes_ago, AgoDate.DateScope.MINUTES);
        } else {
            return new AgoDate(seconds_ago, AgoDate.DateScope.SECONDS);
        }
    }


    public AgoDate() {
        this(0, DateScope.SECONDS);
    }

    public AgoDate(int value, DateScope scope) {
        mValue = value;
        mScope = scope;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public DateScope getScope() {
        return mScope;
    }

    public void setScope(DateScope scope) {
        mScope = scope;
    }

    public boolean isShorterThan(AgoDate agoDate) {
        return !isLongerThan(agoDate);
    }

    public boolean isLongerThan(AgoDate agoDate) {
        if (mScope == null){
            return true;
        }

        if (mValue == 0) {
            return false;
        }

        if (agoDate.getScope() == null) {
            return false;
        }

        if (agoDate.getValue() == 0) {
            return true;
        }

        if (mScope.ordinal() < agoDate.getScope().ordinal()) {
            return false;
        }

        if ((mScope.ordinal() == agoDate.getScope().ordinal()) && (mValue < agoDate.getValue())) {
            return false;
        }

        return true;
    }

}
