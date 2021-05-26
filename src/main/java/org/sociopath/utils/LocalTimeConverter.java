package org.sociopath.utils;

import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.time.LocalTime;

public class LocalTimeConverter implements AttributeConverter<LocalTime, String> {
    @Override
    public String toGraphProperty(LocalTime localTime) {
        return localTime.toString();
    }

    @Override
    public LocalTime toEntityAttribute(String s) {
        String[] separated = s.split(":");

        return LocalTime.of(Integer.parseInt(separated[0]), Integer.parseInt(separated[1]));
    }
}