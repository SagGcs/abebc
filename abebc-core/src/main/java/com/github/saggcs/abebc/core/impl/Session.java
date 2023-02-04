package com.github.saggcs.abebc.core.impl;

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
