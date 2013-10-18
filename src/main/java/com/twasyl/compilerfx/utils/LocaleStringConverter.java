package com.twasyl.compilerfx.utils;


import com.twasyl.compilerfx.beans.Configuration;
import javafx.util.StringConverter;

import java.util.Locale;

public class LocaleStringConverter extends StringConverter<Locale> {

    @Override
    public String toString(Locale locale) {
        if(locale == null) return "---";
        else {
            if(Configuration.getInstance().getUiLocale() != null) {
                return locale.getDisplayName(Configuration.getInstance().getUiLocale());
            } else {
                return  locale.getDisplayName();
            }
        }
    }

    @Override
    public Locale fromString(String s) {
        return null;
    }
}
