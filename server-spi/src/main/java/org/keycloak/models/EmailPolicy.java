/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models;

import org.jboss.logging.Logger;


import java.io.Serializable;
import java.util.ArrayList;
// import java.io.UnsupportedEncodingException;
// import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class EmailPolicy implements Serializable {

    protected static final Logger logger = Logger.getLogger(EmailPolicy.class);

    protected boolean enabled;
    protected List<String> domainsAllowed;
    protected List<String> domainsBlocked;
    protected List<String> topLevelDomainsAllowed;
    protected List<String> topLevelDomainsBlocked;
    protected boolean disableUsers;
    
    public EmailPolicy(){

    }

    public EmailPolicy(boolean enabled, List<String> domainsAllowed, List<String> domainsBlocked, List<String> topLevelDomainsAllowed, List<String> topLevelDomainsBlocked, boolean disableUsers) {
        this.enabled = enabled;
        this.domainsAllowed = domainsAllowed == null ? new ArrayList<String>() : domainsAllowed;
        this.domainsBlocked = domainsBlocked == null ? new ArrayList<String>() : domainsBlocked;
        this.topLevelDomainsAllowed = topLevelDomainsAllowed == null ? new ArrayList<String>() : topLevelDomainsAllowed;
        this.topLevelDomainsBlocked = topLevelDomainsBlocked == null ? new ArrayList<String>() : topLevelDomainsBlocked;
        this.disableUsers = disableUsers;
    }

    public static EmailPolicy DEFAULT_POLICY = new EmailPolicy(false, null, null, null, null, false);



    public boolean getEnabled(){
        return enabled;
    } 

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public List<String> getAllowedDomains(){
        return domainsAllowed;
    }

    public void setAllowedDomains(List<String> domainsAllowed){
        this.domainsAllowed = domainsAllowed;
    }

    public List<String> getBlockedDomains(){
        return domainsBlocked;
    }

    public void setBlockedDomains(List<String> domainsBlocked){
        this.domainsBlocked = domainsBlocked;
    }

    public List<String> getAllowedTopLevelDomains(){
        return topLevelDomainsAllowed;
    }

    public void setAllowedTopLevelDomains(List<String> topLevelDomainsAllowed){
        this.topLevelDomainsAllowed = topLevelDomainsAllowed;
    }

    public List<String> getBlockedTopLevelDomains(){
        return topLevelDomainsBlocked;
    }

    public void setBlockedTopLevelDomains(List<String> topLevelDomainsBlocked){
        this.topLevelDomainsBlocked = topLevelDomainsBlocked;
    }

    public boolean getDisableUsers(){
        return disableUsers;
    }

    public void setDisableUsers(boolean disableUsers){
        this.disableUsers = disableUsers;
    }

}
