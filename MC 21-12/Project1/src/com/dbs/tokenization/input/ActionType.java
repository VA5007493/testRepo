package com.dbs.tokenization.input;

public enum ActionType {

    SUSPEND("/mdes/csapi/#env/v2/token/suspend?Format=JSON"),
    SEARCH("/mdes/csapi/#env/v2/search?Format=JSON"),
    STATUSHISTORY("/mdes/#env/csapi/v2/token/statushistory?Format=JSON"),
    ACTIVATIONMETHODS("/mdes/#env/csapi/v2/token/activationmethods?Format=JSON"),
    DELETE("/mdes/csapi/#env/v2/token/delete?Format=JSON"),
    UNSUSPEND("/mdes/csapi/#env/v2/token/unsuspend?Format=JSON"),
    RESENDACTIVATIONMETHOD("/mdes/csapi/#env/v2/token/resendactivationcode?Format=JSON");

    ActionType(String value) {
        this.value = value;
    }

    String value;

    public String getValue() {
        return value;
    }

    public static String getActionTypeValue(String value) {
        for (ActionType val : ActionType.values()) {
            if (val.name().equalsIgnoreCase(value)) {
                return val.getValue();
            }
        }
        return null;
    }

    public static String getActionTypeName(String value) {
        for (ActionType val : ActionType.values()) {
            if (val.getValue().equalsIgnoreCase(value)) {
                return val.name();
            }
        }
        return null;
    }
}
