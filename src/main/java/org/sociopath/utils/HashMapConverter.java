package org.sociopath.utils;

import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that helps Neo4j to convert non-native type that is supported in Java
 * This class will help to convert the HashMap that is available in the Student class to a String
 * when saving and passing to the database
 *
 * This class will help to change back to HashMap when getting from the database too
 */
public class HashMapConverter implements AttributeConverter<HashMap<String, Double>, List<String>> {

    @Override
    public List<String> toGraphProperty(HashMap<String, Double> hashMap) {
        List<String> friends = new ArrayList<>();

        for(Map.Entry<String, Double> value : hashMap.entrySet()){
            String temp = value.getKey() + ":" + value.getValue();
            friends.add(temp);
        }

        return friends;
    }

    @Override
    public HashMap<String, Double> toEntityAttribute(List<String> list) {
        HashMap<String, Double> friends = new HashMap<>();
        for (String temp : list) {
            String[] separated = temp.split(":");
            friends.put(separated[0], Double.parseDouble(separated[1]));
        }

        return friends;
    }

}

