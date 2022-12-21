package me.deecaad.core.file.inline;

import me.deecaad.core.file.SerializerException;

/**
 * Wraps a {@link SerializerException} with an index location in the string
 * that contains the error. This can be used to literally "point out" where the
 * error occurred.
 */
public class InlineException extends Exception {

    private String issue;
    private int offset;
    private String lookAfter;

    private int index;
    private final SerializerException exception;

    public InlineException(int index, SerializerException exception) {
        this.index = index;
        this.issue = null;
        this.exception = exception;
    }

    public InlineException(String issue, SerializerException exception) {
        this.index = -1;
        this.issue = issue;
        this.exception = exception;
    }

    public InlineException(String issue, int offset, SerializerException exception) {
        this.index = -1;
        this.issue = issue;
        this.offset = offset;
        this.exception = exception;
    }

    public InlineException(String issue, int offset, String lookAfter, SerializerException exception) {
        this.index = -1;
        this.issue = issue;
        this.offset = offset;
        this.lookAfter = lookAfter;
        this.exception = exception;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getLookAfter() {
        return lookAfter;
    }

    public void setLookAfter(String lookAfter) {
        this.lookAfter = lookAfter;
    }

    public SerializerException getException() {
        return exception;
    }
}
