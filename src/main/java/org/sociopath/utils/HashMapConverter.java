package org.sociopath.utils;

import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapConverter implements AttributeConverter<HashMap<String, Integer>, List<String>> {

    @Override
    public List<String> toGraphProperty(HashMap<String, Integer> hashMap) {
        List<String> friends = new ArrayList<>();
        for(Map.Entry<String, Integer> value : hashMap.entrySet()){
            String temp = value.getKey() + ":" + value.getValue();
            friends.add(temp);
        }

        return friends;
    }

    @Override
    public HashMap<String, Integer> toEntityAttribute(List<String> list) {
        HashMap<String, Integer> friends = new HashMap<>();
        for(int i = 0; i<list.size(); i++){
            String temp = list.get(i);
            String [] separated = temp.split(":");
            friends.put(separated[0], Integer.parseInt(separated[1]));
        }

        return null;
    }
}