package com.enjin.velocity.utils.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class EnjinLogFormatter extends Formatter {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

    public String format(LogRecord record) {
        StringBuffer buffer = new StringBuffer(5);
        buffer.append(calcDate(record.getMillis())).append(" ");
        buffer.append("[").append(record.getLevel()).append("]");
        buffer.append(' ');
        buffer.append(formatMessage(record)).append("\n");
        return buffer.toString();
    }

    private String calcDate(long milliSeconds) {
        Date resultDate = new Date(milliSeconds);
        return dateFormat.format(resultDate);
    }

    public String getHead(Handler h) {
        return "Started logging the Enjin plugin on "
            + calcDate(System.currentTimeMillis()) + "\n";
    }

    public String getTail(Handler h) {
        return "";
    }
}
