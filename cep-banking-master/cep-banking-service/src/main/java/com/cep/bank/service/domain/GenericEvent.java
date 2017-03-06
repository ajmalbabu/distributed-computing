package com.cep.bank.service.domain;


import org.akka.util.lib.model.DefaultMessage;

import java.io.Serializable;
import java.util.Date;

public class GenericEvent extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType;
    private String eventSubType;
    private Date eventTime;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventSubType() {
        return eventSubType;
    }

    public void setEventSubType(String eventSubType) {
        this.eventSubType = eventSubType;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericEvent)) return false;

        GenericEvent that = (GenericEvent) o;

        if (!eventType.equals(that.eventType)) return false;
        return eventSubType.equals(that.eventSubType);

    }

    @Override
    public int hashCode() {
        int result = eventType.hashCode();
        result = 31 * result + eventSubType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GenericEvent{" +
                "eventType='" + eventType + '\'' +
                ", eventSubType='" + eventSubType + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}
