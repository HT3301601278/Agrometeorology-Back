package org.agro.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 账户冻结异常
 */
public class AccountFrozenException extends AuthenticationException {
    public AccountFrozenException(String msg) {
        super(msg);
    }
    
    public AccountFrozenException(String msg, Throwable cause) {
        super(msg, cause);
    }
} 