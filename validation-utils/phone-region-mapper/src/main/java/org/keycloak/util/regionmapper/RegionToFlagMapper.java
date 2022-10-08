package org.keycloak.util.regionmapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A Country to flag mapper which provides Country aname, flags, 2-alphabet country code, calling code.
 *
 * @author Yejneshwar Sivamoorthy
 */
public class RegionToFlagMapper {
    private static RegionToFlagMapper instance = null;
    private static final String DATA_DIRECTORY =
        "/org/keycloak/util/regionmapper/data/countrycodes.csv";
    private static DataLoaderHelper dataLoader = null;

    RegionToFlagMapper(String dataDirectory) {
        dataLoader = new DataLoaderHelper(dataDirectory);
    }

    /**
     * Gets a {@link RegionToFlagMapper} instance to carry out country lookup.
     *
     * <p> The {@link RegionToFlagMapper} is implemented as a singleton. Therefore, calling
     * this method multiple times will only result in one instance being created.
     *
     * @return  a {@link RegionToFlagMapper} instance
     */
    public static synchronized RegionToFlagMapper getInstance() {
      if (instance == null) {
        instance = new RegionToFlagMapper(DATA_DIRECTORY);
      }
      return instance;
    }

    public DataMap getFromCountryName(String code){
        return dataLoader.getCountryMap().get(code);
    }

    public DataMap getFromISOName(String code){
        return dataLoader.getCountryISOMap().get(code);
    }

    public Stream<DataMap> getList(){
        List<DataMap> list = new ArrayList<DataMap>();
        for (DataMap data : dataLoader.getCountryISOMap().values()) {
            list.add(data);
        }
        return list.stream();
    }
}

