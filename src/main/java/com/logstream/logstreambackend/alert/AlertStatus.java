package com.logstream.logstreambackend.alert;

public class AlertStatus {

    private long errorCount;
    private int threshold;
    private boolean triggered;

    public AlertStatus(long errorCount, int threshold, boolean triggered) {
        this.errorCount = errorCount;
        this.threshold = threshold;
        this.triggered = triggered;
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