package com.jslink.wc.service;

import com.jslink.wc.security.IAuthenticationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

@RestController
public class BaseController {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BaseController.class);
    @Autowired
    public IAuthenticationFacade authenticationFacade;

    @ExceptionHandler({SQLException.class, org.springframework.dao.DataAccessException.class})
    public Object databaseError(Exception e) {
        logger.error("Operator: " + authenticationFacade.getAuthentication().getName(), e);
        e.printStackTrace();
//        return new WsBaseResult(false, generateExceptionMessage(e));
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e instanceof ResponseStatusException)
            status = ((ResponseStatusException) e).getStatus();
        throw new ResponseStatusException(status, generateExceptionMessage(e));
    }

    @ExceptionHandler(Exception.class)
    public Object handleError(Exception e) {
        logger.error("Operator: " + authenticationFacade.getAuthentication().getName(), e);
        e.printStackTrace();
//        return new WsBaseResult(false, generateExceptionMessage(e));
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e instanceof ResponseStatusException)
            status = ((ResponseStatusException) e).getStatus();
        throw new ResponseStatusException(status, generateExceptionMessage(e));
    }

    private String generateExceptionMessage(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage() + "\n");
        Throwable cause = e.getCause();
        while (cause != null) {
            sb.append("cause: " + cause.getMessage());
            if (cause.getCause() != cause) {//exception may put cause to itself cause,
                cause = cause.getCause();
            }
        }
        return sb.toString();
    }
}
