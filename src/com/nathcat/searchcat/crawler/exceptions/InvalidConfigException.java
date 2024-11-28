package com.nathcat.searchcat.crawler.exceptions;

/**
 * Should be thrown if the supplied configuration file is invalid in some way.
 */
public class InvalidConfigException extends Exception {
    private final String msg;

    public InvalidConfigException(String s) {
        msg = s;
    }

    @Override
    public String toString() {
        return msg;
    }
}
