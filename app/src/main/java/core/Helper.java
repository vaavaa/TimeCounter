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
    public static StatEntry OutgoingCallsTime(Activity activity, String phoneNumber, int action) {

        Calendar calendarTo = Calendar.getInstance();
        Calendar calendarFrom = Calendar.getInstance();
        calendarTo.add(Calendar.DATE, -3);

        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        String selection3Days = android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?";
        String selection = null;
        String[] whereValue = null;

        if (action == 1) {
            selection = selection3Days;
            whereValue = new String[]{String.valueOf(calendarTo.getTimeInMillis()), String.valueOf(calendarFrom.getTimeInMillis())};
        }

        Cursor cur = activity.getContentResolver()
                .query(CallLog.Calls.CONTENT_URI, null, selection, whereValue, strOrder);

        StatEntry statEntry = new StatEntry();
        statEntry.setTitle(phoneNumber);

        if (cur != null) {
            try {
                while (cur.moveToNext()) {

                    String callNumber = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                    if (callNumber != null & phoneNumber.equals(callNumber)) {
                        //String callDate = cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.DATE));
                        int duration = cur.getInt(cur.getColumnIndex(android.provider.CallLog.Calls.DURATION));
                        //String name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));
                        //int id = cur.getInt(cur.getColumnIndex(CallLog.Calls._ID));
                        int type = cur.getInt(cur.getColumnIndex(CallLog.Calls.TYPE));
                        if (duration > 0 && (type == 1 || type == 2))
                            statEntry.Duration += duration;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace(

                );
            } finally {
                cur.close();
            }
        }
        return statEntry;
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

        if (dir.equals("OUTGOING")) {
            //whatever you want here
            return "yes";
        }

        managedCursor.close();
        return "no";

    }
}
