package org.keycloak.util.regionmapper;

import java.util.HashMap;
import java.util.Map;

import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

public class DataMappingStrategy {

    public static HeaderColumnNameTranslateMappingStrategy<DataMap> dataMapping() {
        // Hashmap to map CSV data to
        // Bean attributes.
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("CLDR display name", "name");
        mapping.put("ISO3166-1-Alpha-2", "ISOName");
        mapping.put("Dial", "callingCode");

        // HeaderColumnNameTranslateMappingStrategy
        // for DataMap class
        HeaderColumnNameTranslateMappingStrategy<DataMap> strategy = new HeaderColumnNameTranslateMappingStrategy<DataMap>();
        strategy.setType(DataMap.class);
        strategy.setColumnMapping(mapping);

        return strategy;
    }
}
