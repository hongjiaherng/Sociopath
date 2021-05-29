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
        return LocalTime.parse(s);
    }
}