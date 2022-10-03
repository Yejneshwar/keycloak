package org.keycloak.util.regionmapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

public class DataLoaderHelper {

    private static List<DataMap> dataProvider = null;

    DataLoaderHelper(String dataDirectory) {
        System.out.println("LOADING!!" + dataDirectory);
        try {
            dataProvider = loadData(dataDirectory);
        } catch (Exception e) {
            System.out.println("Problem with loding data");
            e.printStackTrace();
        }
    }

    public static List<DataMap> readAll(InputStream read) throws Exception {

        InputStreamReader reader = new InputStreamReader(read, StandardCharsets.UTF_8);

        CsvToBean<DataMap> cb = new CsvToBeanBuilder<DataMap>(reader)
                                    .withType(DataMap.class)
                                    .withMappingStrategy(DataMappingStrategy.dataMapping())
                                    .build();
                                    
        List<DataMap> list = cb.parse();
        reader.close();
        return list;
    }

    public static List<DataMap> loadData(String fileName) throws Exception {
        InputStream reader = DataLoaderHelper.class.getResourceAsStream(fileName);
        return readAll(reader);
    }

    public SortedMap<String, DataMap> getCountryMap() {
        SortedMap<String, DataMap> finalList = new TreeMap<String, DataMap>();
        for (DataMap r : dataProvider) {
            finalList.put(r.getName(), r);
        }
        return finalList;
    }

    public SortedMap<String, DataMap> getCountryISOMap() {
        SortedMap<String, DataMap> finalList = new TreeMap<String, DataMap>();
        for (DataMap r : dataProvider) {
            finalList.put(r.getISOName(), r);
        }
        return finalList;
    }

}
