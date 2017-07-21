/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.apps.acrypto.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;

import static java.text.DateFormat.SHORT;

public class TimeUtils {

    public static final Locale DEFAULT_LOCALE = App.getInstance().getLocale();
    public static final String TODAY = "Today";
    public static final String YESTERDAY = "Yesterday";
    public static final String TOMORROW = "Tomorrow";

    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;
    public static final long MILLIS_IN_A_DAY = 86400000L;
    public static final long MILLIS_IN_A_SECOND = 1000L;
    public static final long MILLIS_IN_A_MINUTE = 60000L;
    public static final long MORNING_START_TIME_IN_MILLIS = 21600000L;
    public static final long MORNING_END_TIME_IN_MILLIS = 43200000L;
    public static final long EVENING_START_TIME_IN_MILLIS = 57600000L;
    public static final long EVENING_END_TIME_IN_MILLIS = 72000000L;
    public static final int SECS_IN_AN_HOUR = 3600;
    public static final long HOUR_IN_MILLIS = SECS_IN_AN_HOUR * SECOND;
    // General configuration

    public static final TimeZone CONFERENCE_TIMEZONE = TimeZone.getTimeZone("America/Los_Angeles");
    public static final long MONTH_IN_MILLIS = DateUtils.DAY_IN_MILLIS * 7;
    public static final long YEAR_IN_MILLIS = DateUtils.DAY_IN_MILLIS * 7 * 12;
    private static final SimpleDateFormat DATE_FORMAT_DISPLAY =
            new SimpleDateFormat("EEE, dd MMM", DEFAULT_LOCALE);

    private static final SimpleDateFormat DATE_FORMAT_DISPLAY_HEADER =
            new SimpleDateFormat("dd MMM yyyy", DEFAULT_LOCALE);

    private static final SimpleDateFormat[] ACCEPTED_TIMESTAMP_FORMATS = {
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.US)
    };
    public static final SimpleDateFormat VALID_IFMODIFIEDSINCE_FORMAT =
            //new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", App.getInstance().getLocale());

    private static final SimpleDateFormat DATE_FORMAT_NOTIFICATION = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getTimeAgo(long time, long now) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }
        final long diff = now - time;
        if (diff < MINUTE) {
            return "just now";
        } else {
            return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString();
        }
    }

    public static String getTimeAgo(long time) {
        long now = System.currentTimeMillis();
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }
        final long diff = now - time;
        if (diff < MINUTE) {
            return "just now";
        } else {
            return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString();
        }
    }

    public static boolean isValidFormatForIfModifiedSinceHeader(String timestamp) {
        try {
            return VALID_IFMODIFIEDSINCE_FORMAT.parse(timestamp) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    public static long timestampToMillis(String timestamp, long defaultValue) {
        if (TextUtils.isEmpty(timestamp)) {
            return defaultValue;
        }

        Date d = parseTimestamp(timestamp);
        return d == null ? defaultValue : d.getTime();
    }

    public static String formatShortDate(Date date) {
        return DATE_FORMAT_DISPLAY.format(date);
    }

    public static String formatShortDateHeader(Date date) {
        return DATE_FORMAT_DISPLAY_HEADER.format(date);
    }

    public static Date parseTimestamp(String timestamp) {
        for (SimpleDateFormat format : ACCEPTED_TIMESTAMP_FORMATS) {
            try {
                return format.parse(timestamp);
            } catch (ParseException ex) {
                continue;
            }
        }
        // All attempts to parse have failed
        return null;
    }

    public static String formatShortTime(Context context, long millis) {
        Date time = new Date(millis);
        DateFormat format = DateFormat.getTimeInstance(SHORT);
        TimeZone tz = getDisplayTimeZone(context);
        if (tz != null) {
            format.setTimeZone(tz);
        }
        return format.format(time);
    }

    public static String formatShortTime(Context context, Date time) {
        DateFormat format = DateFormat.getTimeInstance(SHORT);
        TimeZone tz = getDisplayTimeZone(context);
        if (tz != null) {
            format.setTimeZone(tz);
        }
        return format.format(time);
    }

    public static TimeZone getDisplayTimeZone(Context context) {
        TimeZone defaultTz = TimeZone.getDefault();
        return defaultTz != null ? defaultTz : CONFERENCE_TIMEZONE;
    }

    /**
     * Returns "Today", "Tomorrow", "Yesterday", or a short date format.
     */
    public static String formatHumanFriendlyShortDate(long timestamp) {


        long localTimestamp, localTime;
        long now = System.currentTimeMillis();

        localTimestamp = timestamp;
        localTime = now;

        long dayOrd = localTimestamp / MILLIS_IN_A_DAY;
        long nowOrd = localTime / MILLIS_IN_A_DAY;

        if (dayOrd == nowOrd) {
            return TODAY;
        } else if (dayOrd == nowOrd - 1) {
            return YESTERDAY;
        } else if (dayOrd == nowOrd + 1) {
            return TOMORROW;
        } else {
            return formatShortDateHeader(new Date(timestamp));
        }
    }

    /**
     * Returns "Today,1 Jan 2016", "Tomorrow,2 Jan 2016", format for Notification list header
     */
    public static String formatHeaderDate(long timestamp) {
        long localTimestamp, localTime;
        long now = System.currentTimeMillis();

        localTimestamp = timestamp;
        localTime = now;

        long dayOrd = localTimestamp / MILLIS_IN_A_DAY;
        long nowOrd = localTime / MILLIS_IN_A_DAY;

        if (dayOrd == nowOrd) {
            return formatCloseDate(new Date(timestamp));
        } else if (dayOrd == nowOrd - 1) {
            return formatCloseDate(new Date(timestamp));
        } else if (dayOrd == nowOrd + 1) {
            return formatCloseDate(new Date(timestamp));
        } else {
            return formatShortDateHeader(new Date(timestamp));
        }
    }

    public static String formatCloseDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", DEFAULT_LOCALE);
        String dateFormat = format.format(date);
        return dateFormat;
    }

    public static String getCurrentUTCTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    public static boolean isEveningTime(long timeOfDay) {
        return timeOfDay >= EVENING_START_TIME_IN_MILLIS && timeOfDay < EVENING_END_TIME_IN_MILLIS;
    }

    public static boolean isMorningTime(long timeOfDay) {
        return timeOfDay >= MORNING_START_TIME_IN_MILLIS && timeOfDay < MORNING_END_TIME_IN_MILLIS;
    }

    public static long getCurrentTimeInMillisInCurrentTimezone() {
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeZone(TimeZone.getDefault());
        long currTimeInUtc = calendar.getTimeInMillis();
        TimeZone timeZone = TimeZone.getDefault();
        int mGMTOffset = 0;//timeZone.getRawOffset();
        return currTimeInUtc + mGMTOffset;
    }

    public static String toStandardTime(String militaryTime) {
        SimpleDateFormat militaryTimeFormat =
                new SimpleDateFormat("H:mm", DEFAULT_LOCALE);
        Date militaryDate = new Date();
        try {
            militaryDate = militaryTimeFormat.parse(militaryTime);
        } catch (ParseException e) {
            LogUtils.logException(e);
        }
        SimpleDateFormat standardTimeFormat =
                new SimpleDateFormat("h:mm aa", DEFAULT_LOCALE);
        return standardTimeFormat.format(militaryDate);
    }

    public static CharSequence formatDuration(long millis, Context context) {
        final Resources res = context.getResources();
        if (millis >= HOUR) {
            final int hours = (int) ((millis + HOUR / 2) / HOUR);
            return res.getQuantityString(
                    R.plurals.duration_hours, hours, hours);
        } else if (millis >= MINUTE) {
            final int minutes = (int) ((millis + HOUR / (2 * 60)) / MINUTE);
            return res.getQuantityString(
                    R.plurals.duration_minutes, minutes, minutes);
        } else {
            final int seconds = (int) ((millis + HOUR / (2 * 60 * 60)) / SECOND);
            return res.getQuantityString(
                    R.plurals.duration_seconds, seconds, seconds);
        }
    }

    public static Calendar getCalendarForBeginOfTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar getCalendarForBeginOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static String formatDate(long milliSeconds, String format) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(milliSeconds);
        return new SimpleDateFormat(format, Locale.getDefault()).format(calendar.getTime());
    }

    public static String getNotificationDate(String notificationDate) {
        Date date = new Date();
        if (!TextUtils.isEmpty(notificationDate)) {
            try {
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(notificationDate);
            } catch (ParseException e) {
                LogUtils.logException(e);
            }
        }
        return DATE_FORMAT_NOTIFICATION.format(date);
    }

    public static long notificationTimestampToMillis(String timestamp, long defaultValue) {
        if (TextUtils.isEmpty(timestamp)) {
            return defaultValue;
        }
        Date date = null;
        try {
            date = DATE_FORMAT_NOTIFICATION.parse(timestamp);
        } catch (ParseException e) {
            LogUtils.logException(e);
        }
        return date == null ? defaultValue : date.getTime();
    }

    public static String getValidDuration(long time, Context context) {
        long now = System.currentTimeMillis();
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }
        final long diff = time - now;
        return formatDuration(diff, context).toString();
    }

    public static String getFormattedDate(Context context, String timestamp) {
        Date date = null;
        date = parseTimestampToGmt(timestamp);
        DateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        TimeZone tz = getDisplayTimeZone(context);
        simpleDateFormat.setTimeZone(tz);
        return simpleDateFormat.format(date);
    }

    public static Date parseTimestampToGmt(String timestamp) {
        for (SimpleDateFormat format : ACCEPTED_TIMESTAMP_FORMATS) {
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                return format.parse(timestamp);
            } catch (ParseException ex) {
                continue;
            }
        }
        // All attempts to parse have failed
        return null;
    }

    public static long timestampToGmtMillis(String timestamp, long defaultValue) {
        if (TextUtils.isEmpty(timestamp)) {
            return defaultValue;
        }
        Date d = parseTimestampToGmt(timestamp);
        return d == null ? defaultValue : d.getTime();
    }

    public static String getFormattedDateTime(long timestampInMilliSeconds) {
        Date date = new Date();
        date.setTime(timestampInMilliSeconds);
        String formattedDate;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a");
        dateFormat.setTimeZone(TimeZone.getDefault());
        formattedDate = dateFormat.format(date);
        //formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        return formattedDate;
    }
}
