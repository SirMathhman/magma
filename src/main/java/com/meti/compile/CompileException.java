package com.meti.compile;

import com.meti.ApplicationException;

public class CompileException extends ApplicationException {
    public CompileException(String message) {
        super(message);
    }

    public CompileException(String message, Exception cause) {
        super(message, cause);
    }
}
