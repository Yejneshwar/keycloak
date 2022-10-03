/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.actiontoken.updatephonenumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.authentication.actiontoken.DefaultActionToken;

public class UpdatePhoneNumberActionToken extends DefaultActionToken {

	public static final String TOKEN_TYPE = "update-phone-number";

	@JsonProperty("oldPhoneNumber")
	private String oldPhoneNumber;
	@JsonProperty("newPhoneNumber")
	private String newPhoneNumber;
	@JsonProperty("oldPhoneNumberLocale")
	private String oldPhoneNumberLocale;
	@JsonProperty("newPhoneNumberLocale")
	private String newPhoneNumberLocale;

	public UpdatePhoneNumberActionToken(String userId, int absoluteExpirationInSecs, String oldPhoneNumber, String newPhoneNumber){
		super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null);
		this.oldPhoneNumber = oldPhoneNumber;
		this.newPhoneNumber = newPhoneNumber;
	}

	private UpdatePhoneNumberActionToken(){

	}

	public String getOldPhoneNumber() {
		return oldPhoneNumber;
	}

	public void setOldPhoneNumber(String oldPhoneNumber) {
		this.oldPhoneNumber = oldPhoneNumber;
	}

	public String getNewPhoneNumber() {
		return newPhoneNumber;
	}

	public void setNewPhoneNumber(String newPhoneNumber) {
		this.newPhoneNumber = newPhoneNumber;
	}

	public String getOldPhoneNumberLocale() {
		return oldPhoneNumberLocale;
	}

	public void setOldPhoneNumberLocale(String oldPhoneNumberLocale) {
		this.oldPhoneNumberLocale = oldPhoneNumberLocale;
	}

	public String getNewPhoneNumberLocale() {
		return newPhoneNumberLocale;
	}

	public void setNewPhoneNumberLocale(String newPhoneNumberLocale) {
		this.newPhoneNumberLocale = newPhoneNumberLocale;
	}
}
