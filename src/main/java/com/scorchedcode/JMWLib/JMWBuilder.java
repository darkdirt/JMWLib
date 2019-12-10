package com.scorchedcode.JMWLib;

public class JMWBuilder {

    protected String csrf, refresh;
    public JMWBuilder() {

    }

    public JMWBuilder setCSRF(String csrf) {
        this.csrf = csrf;
        return this;
    }

    public JMWBuilder setRefresh(String refresh) {
        this.refresh = refresh;
        return this;
    }

    public JMW build() {
        return new JMW(this);
    }
}
