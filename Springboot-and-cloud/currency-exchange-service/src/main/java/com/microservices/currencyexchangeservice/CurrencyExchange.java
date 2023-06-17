package com.microservices.currencyexchangeservice;

import java.math.BigInteger;

public class CurrencyExchange {
    private Long id;
    private String from;
    private String to;
    private BigInteger conversionMultiple;
    private String environment;

    public CurrencyExchange(Long id, String from, String to, BigInteger conversionMultiple) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.conversionMultiple = conversionMultiple;
    }

    public CurrencyExchange() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigInteger getConversionMultiple() {
        return conversionMultiple;
    }

    public void setConversionMultiple(BigInteger conversionMultiple) {
        this.conversionMultiple = conversionMultiple;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
