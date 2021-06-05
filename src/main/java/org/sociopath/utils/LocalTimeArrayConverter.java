package org.sociopath.utils;

import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LocalTimeArrayConverter implements AttributeConverter<LocalTime[], List<String>> {
    @Override
    public List<String> toGraphProperty(LocalTime[] localTimes) {
        List<String> localTimeArray = new ArrayList<>();
        for(LocalTime time : localTimes){
            localTimeArray.add(time.toString());
        }
        return localTimeArray;
    }

    @Override
    public LocalTime[] toEntityAttribute(List<String> strings) {
        LocalTime[] localTimes = new LocalTime[strings.size()];

        for(int i = 0; i< localTimes.length; i++){
            LocalTime time = LocalTime.parse(strings.get(i));
            localTimes[i] = time;
        }

        return localTimes;
    }
}