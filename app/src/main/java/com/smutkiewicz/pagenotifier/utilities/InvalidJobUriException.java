package com.smutkiewicz.pagenotifier.utilities;

/**
 * Created by Admin on 2017-09-05.
 */

public class InvalidJobUriException extends Exception {
    public InvalidJobUriException(String message) {
        super(message);
    }

    public InvalidJobUriException(String message, Throwable cause) {
        super(message, cause);
    }
}
