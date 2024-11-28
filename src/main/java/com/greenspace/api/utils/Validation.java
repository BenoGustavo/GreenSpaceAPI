package com.greenspace.api.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class Validation {
    // 5548999142018 valid sc number
    // 5511987654321 valid sp number
    private final String BRAZILIAM_CELLPHONE_NUMBER_REGEX = "^55(11|12|13|14|15|16|17|18|19|21|22|24|27|28|31|32|33|34|35|37|38|41|42|43|44|45|46|47|48|49|51|53|54|55|61|62|63|64|65|66|67|68|69|71|73|74|75|77|79|81|82|83|84|85|86|87|88|89|91|92|93|94|95|96|97|98|99)9\\d{8}$";
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
