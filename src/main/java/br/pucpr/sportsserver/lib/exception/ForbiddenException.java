package br.pucpr.sportsserver.lib.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends NoSuchElementException {
    public ForbiddenException() {
    }

    public ForbiddenException(Long id) {
        this("Not Found: " + id);
    }

    public ForbiddenException(String s, Throwable cause) {
        super(s, cause);
    }

    public ForbiddenException(Throwable cause) {
        super(cause);
    }

    public ForbiddenException(String s) {
        super(s);
    }
}