package com.twasyl.compilerfx.utils;

import java.util.Locale;
import java.util.regex.Pattern;

public class LocaleUtils {

    public static Locale parseString(final String localeAsString) {
        Locale locale = null;

        if(localeAsString != null && !localeAsString.trim().isEmpty()) {
            final String[] parts = localeAsString.trim().split("_");

            if(parts.length > 0) {
                final Locale.Builder builder = new Locale.Builder();
                builder.setLanguage(parts[0]);

                if(parts.length > 1) {
                    builder.setRegion(parts[1]);

                    if(parts.length > 2) {
                        builder.setVariant(parts[2]);
                    }
                }

                locale = builder.build();
            }
        }

        return locale;
    }

    public static String toString(Locale locale) {
        String result = null;

        if(locale != null)   {
            final StringBuilder builder = new StringBuilder();

            if(locale.getLanguage() != null && !locale.getLanguage().trim().isEmpty()) {
                builder.append(locale.getLanguage().trim());

                if(locale.getCountry() != null && !locale.getCountry().trim().isEmpty()) {
                    builder.append("_");
                    builder.append(locale.getCountry().trim());

                    if(locale.getVariant() != null && !locale.getVariant().trim().isEmpty()) {
                        builder.append("_");
                        builder.append(locale.getVariant().trim());
                    }
                }
            }

            result = builder.toString().trim().isEmpty() ? null : builder.toString();
        }

        return result;
    }
}
