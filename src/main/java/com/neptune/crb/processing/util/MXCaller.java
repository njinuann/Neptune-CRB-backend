package com.neptune.crb.processing.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Component
public class MXCaller implements Serializable {
    private String extraIndent = "    ";
    private String refNumber = "", alertCode;
    private String alertDescription = "", duration = "";
    private ArrayList<Exception> exceptionsList = new ArrayList();
    private String xapiRespCode = "EVCODE_01";
    private String xapiMessage = "EVCODE_01";
    private final HashMap<String, Object> callsMap = new HashMap<>();

    public void logException(Exception ex) {
        getExceptionsList().add(ex);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<caller>");
        buffer.append("\r\n").append(getExtraIndent()).append("<refNo>").append(getRefNumber()).append("</refNo>");
        buffer.append("\r\n").append(getExtraIndent()).append("<alertCode>").append(getAlertCode()).append("</alertCode>");
        buffer.append("\r\n").append(getExtraIndent()).append("<alertDesc>").append(getAlertDescription()).append("</alertDesc>");

        String[] callKeys = callsMap.keySet().toArray(new String[callsMap.size()]);
        Arrays.sort(callKeys);
        for (Object key : callKeys) {
            buffer.append("\r\n").append(getExtraIndent()).append("<").append(String.valueOf(key).replaceAll("\\d", "")).append(">").append(cleanText(callsMap.get(key).toString())).append("</").append(String.valueOf(key).replaceAll("\\d", "")).append(">");
        }

        for (Object exception : getExceptionsList().toArray()) {
            if (exception != null) {

                buffer.append("\r\n").append(getExtraIndent()).append("<exception>");
                buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("<class>").append(((Exception) exception).getClass().getSimpleName()).append("</class>");
                String emsg = (((Exception) exception).getMessage() == null) ? "" : ((Exception) exception).getMessage();

                if (emsg.contains("\r\n")) {
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("<message>");
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append(getExtraIndent()).append(((Exception) exception).getMessage().replaceAll("\r\n", "\r\n" + getExtraIndent() + getExtraIndent() + getExtraIndent()));
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("</message>");
                } else {
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("<message>").append(((Exception) exception).getMessage()).append("</message>");
                }
                buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("<stacktrace>");
                for (StackTraceElement s : ((Throwable) exception).getStackTrace()) {
                    buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append(getExtraIndent()).append("at ").append(s.toString());
                }
                buffer.append("\r\n").append(getExtraIndent()).append(getExtraIndent()).append("</stacktrace>");
                buffer.append("\r\n").append(getExtraIndent()).append("</exception>");

            }
        }

        buffer.append("\r\n").append(getExtraIndent()).append("<xapiMessage>").append(getXapiMessage()).append("</xapiMessage>");
        buffer.append("\r\n").append(getExtraIndent()).append("<respCode>").append(getXapiRespCode()).append("</respCode>");
        buffer.append("\r\n").append(getExtraIndent()).append("<duration>").append(getDuration()).append("</duration>");
        buffer.append("\r\n").append("</caller>");
        return buffer.toString();
    }

    private String cleanText(String text) {
        String line = "", buffer = "";
        text = (text != null) ? text : "";

        InputStream is = new ByteArrayInputStream(text.getBytes());
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        try {
            while (line != null) {
                buffer += line;
                line = bis.readLine();
            }
        } catch (IOException ex) {
            return buffer;
        }

        return buffer;
    }


    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }


    public ArrayList<Exception> getExceptionsList() {
        return exceptionsList;
    }


    public void setExceptionsList(ArrayList<Exception> exceptionsList) {
        this.exceptionsList = exceptionsList;
    }


    public String getRefNumber() {
        return refNumber;
    }


    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getExtraIndent() {
        return extraIndent;
    }


    public void setExtraIndent(String extraIndent) {
        this.extraIndent = extraIndent;
    }


    public void setCall(String callRef, Object callObject) {
        this.callsMap.put(callsMap.size() + callRef, callObject);
    }

    public String getAlertDescription() {
        return alertDescription;
    }


    public void setAlertDescription(String alertDescription) {
        this.alertDescription = alertDescription;
    }


    public String getAlertCode() {
        return alertCode;
    }


    public void setAlertCode(String alertCode) {
        this.alertCode = alertCode;
    }


    public String getXapiMessage() {
        return xapiMessage;
    }


    public void setXapiMessage(String xapiMessage) {
        this.xapiMessage = xapiMessage;
    }

    public String getXapiRespCode() {
        return xapiRespCode;
    }

    public void setXapiRespCode(String xapiRespCode) {
        this.xapiRespCode = xapiRespCode;
    }
}