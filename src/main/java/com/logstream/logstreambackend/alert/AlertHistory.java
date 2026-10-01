package com.logstream.logstreambackend.alert;

public class AlertHistory {

    private String time;
    private long errorCount;
    private int threshold;
    private boolean triggered;

    public AlertHistory(
            String time,
            long errorCount,
            int threshold,
            boolean triggered
    ) {
        this.time = time;
        this.errorCount = errorCount;
        this.threshold = threshold;
        this.triggered = triggered;
    }

    public String getTime() {
        return time;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public int getThreshold() {
        return threshold;
    }

    public boolean isTriggered() {
        return triggered;
    }
}