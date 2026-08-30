package com.sameer.job.exception;

public final class ErrorCodes {

    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String CONFLICT = "CONFLICT";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String AI_UNAVAILABLE = "AI_UNAVAILABLE";

    public static final String ALREADY_APPLIED = "ALREADY_APPLIED";
    public static final String JOB_NOT_OPEN = "JOB_NOT_OPEN";
    public static final String EMAIL_REGISTERED = "EMAIL_REGISTERED";
    public static final String ADMIN_SELF_SIGNUP = "ADMIN_SELF_SIGNUP";
    public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    private ErrorCodes() {
    }
}
