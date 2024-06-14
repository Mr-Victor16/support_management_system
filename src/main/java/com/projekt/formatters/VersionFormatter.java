package com.projekt.formatters;

import com.projekt.models.Version;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class VersionFormatter implements Formatter<Version> {
    @Override
    public Version parse(String text, Locale locale) throws ParseException {
        if(text.isEmpty()){
            return null;
        }

        var tokens = text.split("\\.",3);

        if(tokens.length == 3){
            return new Version(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
        }
        else{
            throw new ParseException("exception",0);
        }
    }

    @Override
    public String print(Version version, Locale locale) {
        if(version == null){
            return "";
        }

        return String.format("%s.%s.%s", version.getVersionYear(), version.getVersionMonth(), version.getVersionNumber());
    }
}
