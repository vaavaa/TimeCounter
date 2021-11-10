package core;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Helper {
    public static StatEntry OutgoingTotalTime(Activity activity) {

        Date currentDate = new Date();
        Calendar calendarTo = Calendar.getInstance();
        Calendar calendarFrom = Calendar.getInstance();
        calendarTo.add(Calendar.DATE, -3);

        Map<String, StatEntry> callLogMap1 = new HashMap<>();

        String[] whereValue = new String[]{String.valueOf(calendarTo.getTimeInMillis()), String.valueOf(calendarFrom.getTimeInMillis())};
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC limit 50";

        Cursor cur = activity.getContentResolver()
                .query(CallLog.Calls.CONTENT_URI, null, android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?", whereValue, strOrder);

        StatEntry StatEntry1 = null;

        if (cur != null) {
            try {
                while (cur.moveToNext()) {

                    String callNumber = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                    //    String callDate = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.DATE));
                    int duration = cur.getInt(cur.getColumnIndex(android.provider.CallLog.Calls.DURATION));

                    String name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));


                    int id = cur.getInt(cur.getColumnIndex(CallLog.Calls._ID));

                    int type = cur.getInt(cur.getColumnIndex(CallLog.Calls.TYPE));

                    if (callNumber != null & duration > 0 && (type == 1 || type == 2)) {

                        int n = callNumber.length();
                        String lastDigits;
                        String number = callNumber.replaceAll(Pattern.quote("+"), ""); //replacing the plus
                        //am just checking last 5digitis and saving to map so that we can get same //number duration
                        if (n >= 5) {
                            try {

                                lastDigits = String.valueOf(Long.parseLong(number) % 100000);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                lastDigits = callNumber;
                            }
                        } else {
                            lastDigits = callNumber;
                        }

                        if (callLogMap1.containsKey(lastDigits)) {
                            StatEntry1 = callLogMap1.get(callNumber);

                            if (StatEntry1 != null) StatEntry1.setTitle(callNumber);
                            if (StatEntry1 != null) StatEntry1.Duration += duration;


                        } else {
                            StatEntry1 = new StatEntry();

                            StatEntry1.setTitle(callNumber);

                            StatEntry1.Duration += duration;
                        }


                        if (StatEntry1 != null) StatEntry1.setTime((StatEntry1.Duration) / 60);
                        callLogMap1.put(callNumber, StatEntry1);


                    }
                }

            } catch (Exception e) {
                e.printStackTrace(

                );
            } finally {
                cur.close();
            }
        }
        return StatEntry1;
    }

    public static String getLastCall(Activity activity) {
        StringBuffer sb = new StringBuffer();
        Cursor cur = activity.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

        int number = cur.getColumnIndex(CallLog.Calls.NUMBER);
        int duration = cur.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details : \n");
        while (cur.moveToNext()) {
            String phNumber = cur.getString(number);
            String callDuration = cur.getString(duration);
            sb.append("\nPhone Number:" + phNumber);
            break;
        }
        cur.close();
        String str = sb.toString();
        return str;

    }

    private String getCallDetails(Activity activity) {

        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = activity.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        managedCursor.moveToLast();
        String phNumber = managedCursor.getString(number);
        String callType = managedCursor.getString(type);
        String callDate = managedCursor.getString(date);
        Date callDayTime = new Date(Long.valueOf(callDate));
        String callDuration = managedCursor.getString(duration);
        String dir = null;
        int dircode = Integer.parseInt(callType);

        switch (dircode) {
            case CallLog.Calls.OUTGOING_TYPE:
                dir = "OUTGOING";
                break;

            case CallLog.Calls.INCOMING_TYPE:
                dir = "INCOMING";
                break;

            case CallLog.Calls.MISSED_TYPE:
                dir = "MISSED";
                break;
        }

        if(dir.equals("OUTGOING")){
            //whatever you want here
            return "yes";
        }

        managedCursor.close();
        return "no";

    }
}
