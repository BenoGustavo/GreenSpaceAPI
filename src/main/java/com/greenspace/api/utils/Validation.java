package com.greenspace.api.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class Validation {
    private final String BRAZILIAM_CELLPHONE_NUMBER_REGEX = "^\\+55\\d{2}9\\d{8}$";
    public final Pattern BRAZILIAM_CELLPHONE_NUMBER_PATTERN = Pattern.compile(BRAZILIAM_CELLPHONE_NUMBER_REGEX);

    private final String USERNAME_REGEX = "@([A-Za-z0-9._]{1,30})";
    public final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    private final String BRAZILIAN_ZIPCODE_REGEX = "^\\d{5}-\\d{3}$";
    public final Pattern BRAZILIAN_ZIPCODE_PATTERN = Pattern.compile(BRAZILIAN_ZIPCODE_REGEX);

    public boolean isFieldValid(String field, Pattern pattern) {
        if (field == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(field);
        return matcher.matches();
    }
}
