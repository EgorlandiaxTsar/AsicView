package com.egorgoncharov.asicview.service.parsing;

import org.jsoup.nodes.Element;

public class Response {
    private Element content;
    private int statusCode;
    private String message;
    private String ip;
    private int requestTime;
    private int preferredRequestTime;

    public Response() {
    }

    public Response(Element content, int statusCode, String message, String ip, int requestTime, int preferredRequestTime) {
        this.content = content;
        this.statusCode = statusCode;
        this.message = message;
        this.ip = ip;
        this.requestTime = requestTime;
        this.preferredRequestTime = preferredRequestTime;
    }

    public Element getContent() {
        return content;
    }

    public void setContent(Element content) {
        this.content = content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(int requestTime) {
        this.requestTime = requestTime;
    }

    public int getPreferredRequestTime() {
        return preferredRequestTime;
    }

    public void setPreferredRequestTime(int preferredRequestTime) {
        this.preferredRequestTime = preferredRequestTime;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isTimingSuccessful() {
        return requestTime <= preferredRequestTime;
    }
}
