package org.keycloak.util.regionmapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.apache.commons.io.IOUtils;

public class DataMap {
    public String name;
    public String ISOName;
    public String callingCode;
    public String flag;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getISOName() {
        return this.ISOName;
    }

    public void setISOName(String ISOName) {
        this.ISOName = ISOName;
    }

    public String getCallingCode() {
        return this.callingCode;
    }

    public void setCallingCode(String callingCode) {
        this.callingCode = callingCode;
    }

    public String getFlag(){
        InputStream reader = DataLoaderHelper.class.getResourceAsStream("/org/keycloak/regionmapper/data/flags/"+this.ISOName.toLowerCase()+".png");
        byte[] fileContent = null;
        try {
            fileContent = IOUtils.toByteArray(reader);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
