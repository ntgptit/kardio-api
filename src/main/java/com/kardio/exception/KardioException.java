package com.kardio.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class KardioException extends RuntimeException {

    private static final long serialVersionUID = 1509401079030096266L;
    private final HttpStatus status;

    public KardioException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public KardioException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public static KardioException resourceNotFound(String resourceName, Object id) {
        return new KardioException(String.format("%s not found with id: %s", resourceName, id), HttpStatus.NOT_FOUND);
    }

    public static KardioException resourceNotFound(MessageSource messageSource, String resourceKey, Object id) {
        return new KardioException(messageSource.getMessage("error.resource.notfound", new Object[]{
                messageSource.getMessage(resourceKey, null, LocaleContextHolder.getLocale()), id
        }, LocaleContextHolder.getLocale()), HttpStatus.NOT_FOUND);
    }

    public static KardioException resourceAlreadyExists(String resourceName, String field, Object value) {
        return new KardioException(
            String.format("%s already exists with %s: %s", resourceName, field, value),
            HttpStatus.CONFLICT);
    }

    public static KardioException resourceAlreadyExists(
            MessageSource messageSource,
            String resourceKey,
            String field,
            Object value) {
        return new KardioException(messageSource.getMessage("error.resource.alreadyexists", new Object[]{
                messageSource.getMessage(resourceKey, null, LocaleContextHolder.getLocale()), field, value
        }, LocaleContextHolder.getLocale()), HttpStatus.CONFLICT);
    }

    public static KardioException validationError(String message) {
        return new KardioException(message, HttpStatus.BAD_REQUEST);
    }

    public static KardioException validationError(MessageSource messageSource, String key, Object... args) {
        return new KardioException(
            messageSource.getMessage(key, args, LocaleContextHolder.getLocale()),
            HttpStatus.BAD_REQUEST);
    }

    public static KardioException forbidden(String message) {
        return new KardioException(message, HttpStatus.FORBIDDEN);
    }

    public static KardioException forbidden(MessageSource messageSource, String key, Object... args) {
        return new KardioException(
            messageSource.getMessage(key, args, LocaleContextHolder.getLocale()),
            HttpStatus.FORBIDDEN);
    }
}