/*
 * Copyright 2023 The open source project at https://github.com/saggcs/abebc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.saggcs.abebc.core.impl;

import java.time.ZonedDateTime;

public class Session {
	private final String sessionId, wmVersion, userName;
	private final ZonedDateTime validUntil;

	Session(String pSessionId, String pWmVersion, String pUserName,
			ZonedDateTime pValidUntil) {
		sessionId = pSessionId;
		wmVersion = pWmVersion;
		userName = pUserName;
		validUntil = pValidUntil;
	}

	public String getSessionId() { return sessionId; }
	public String getWmVersion() { return wmVersion; }
	public String getUserName() { return userName; }
	public ZonedDateTime getValidUntil() { return validUntil; }
}
