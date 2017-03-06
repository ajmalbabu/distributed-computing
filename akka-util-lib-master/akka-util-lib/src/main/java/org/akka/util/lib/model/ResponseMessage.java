package org.akka.util.lib.model;

import java.io.Serializable;
import java.util.Map;

import static org.akka.util.lib.model.ResponseMessage.ResponseType.MessageProcessed;
import static org.akka.util.lib.model.ResponseMessage.ResponseType.UnknownMessage;


public class ResponseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private ResponseType responseType;
    private Message message;

    public ResponseMessage(ResponseType responseType, Message message) {
        this.responseType = responseType;
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public Map<String, Object> getMdc() {
        return message.getMdc();
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                ", responseType=" + responseType +
                ", message=" + message +
                '}';
    }

    public boolean isCompleted() {
        return responseType == MessageProcessed || responseType == UnknownMessage;
    }

    public static enum ResponseType {MessageSubmitted, MessageProcessed, UnknownMessage}
}
