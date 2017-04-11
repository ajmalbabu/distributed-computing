package com.cep.bank.service.domain;


import akka.sdk.model.DefaultMessage;

import java.io.Serializable;
import java.util.Date;

public class CustomerTransactionEvent extends DefaultMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String customerName;
    private String transactionType;
    private String transactionSubType;
    private Date transactionTime;
    private double transactionAmount;
    private String transactionCurrency;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionSubType() {
        return transactionSubType;
    }

    public void setTransactionSubType(String transactionSubType) {
        this.transactionSubType = transactionSubType;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof CustomerTransactionEvent)) return false;

        CustomerTransactionEvent that = (CustomerTransactionEvent) o;

        return customerName.equals(that.customerName);

    }

    @Override
    public int hashCode() {
        return customerName.hashCode();
    }

    @Override
    public String toString() {
        return "CustomerTransactionEvent{" +
                "customerName='" + customerName + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", transactionSubType='" + transactionSubType + '\'' +
                ", transactionTime=" + transactionTime +
                ", transactionAmount=" + transactionAmount +
                ", transactionCurrency='" + transactionCurrency + '\'' +
                '}';
    }

    public static CustomerTransactionEvent createEvent(String customerName, String transactionType, String transactionSubType,
                                                       double transactionAmount, String transactionCurrency) {

        CustomerTransactionEvent customerTransactionEvent = new CustomerTransactionEvent();
        customerTransactionEvent.setEntityId(customerName);
        customerTransactionEvent.setCustomerName(customerName);
        customerTransactionEvent.setTransactionType(transactionType);
        customerTransactionEvent.setTransactionSubType(transactionSubType);
        customerTransactionEvent.setTransactionTime(new Date());
        customerTransactionEvent.setTransactionAmount(transactionAmount);
        customerTransactionEvent.setTransactionCurrency(transactionCurrency);
        return customerTransactionEvent;
    }
}
