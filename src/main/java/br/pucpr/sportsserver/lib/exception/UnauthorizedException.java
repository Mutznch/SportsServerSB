package br.pucpr.sportsserver.lib.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends NoSuchElementException {
    public UnauthorizedException() {
    }

    public UnauthorizedException(Long id) {
        this("Not Found: " + id);
    }

    public UnauthorizedException(String s, Throwable cause) {
        super(s, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    public UnauthorizedException(String s) {
        super(s);
    }
}
