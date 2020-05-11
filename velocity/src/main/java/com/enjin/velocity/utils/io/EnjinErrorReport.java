package com.enjin.velocity.utils.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EnjinErrorReport {

    Throwable throwable = null;
    String info = "";
    String otherInformation = "";
    long timeThrown = System.currentTimeMillis();

    public EnjinErrorReport(Throwable throwable, String otherInformation) {
        this.throwable = throwable;
        this.otherInformation = otherInformation;
    }

    public EnjinErrorReport(String data, String otherInformation) {
        info = data;
        this.otherInformation = otherInformation;
    }

    @Override
    public String toString() {
        StringBuilder errorString = new StringBuilder();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
        Date date = new Date(timeThrown);

        errorString.append("Enjin plugin error report. Error generated on: ")
            .append(dateFormat.format(date))
            .append(":\n");
        errorString.append("Extra data: ").append(otherInformation).append("\n");
        if (throwable != null) {
            errorString.append("Stack trace:\n");
            errorString.append(throwable.toString()).append("\n");
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                errorString.append(stackTraceElement.toString()).append("\n");
            }
        } else {
            errorString.append("More Info:\n");
            errorString.append(info);
        }
        return errorString.toString();
    }
}
