package com.github.saggcs.abebc.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

import javax.inject.Inject;

import com.github.jochenw.afw.core.data.Data;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.HttpConnector;
import com.github.jochenw.afw.core.util.HttpConnector.HttpConnection;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.saggcs.abebc.core.api.AbebcBean;
import com.github.saggcs.abebc.uttill.JsonUtils;

public class SessionProvider {
	private @Inject HttpConnector httpConnector;
	private @LogInject ILog log;

	public Session getSession(AbebcBean pBean) {
		log.entering("getSession", pBean.getAbebsUrl(), pBean.getWmVersion(), pBean.getAbebsUserName());
		try {
			final URL url = new URL(pBean.getAbebsUrl(), "/rest/authenticate?wmVersion="
					+ URLEncoder.encode(pBean.getWmVersion(), StandardCharsets.UTF_8));
			try (HttpConnection conn = httpConnector.connect(url)) {
				final Authenticator auth = new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication (pBean.getAbebsUserName(), pBean.getAbebsPassword().toCharArray());
					}
				};
				final HttpURLConnection urlConn = conn.getUrlConnection();
				urlConn.setAuthenticator(auth);
				try (final InputStream in = urlConn.getInputStream()) {
					final Map<String,Object> response = JsonUtils.parse(in);
					final String sessionId = Data.requireString(response, "sessionId");
					final String wmVersion = Data.requireString(response, "wmVersion");
					final String userName = Data.requireString(response, "userName");
					final String validUntilStr = Data.requireString(response, "validUntil");
					final ZonedDateTime validUntil;
					try {
						validUntil = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(validUntilStr));
					} catch (DateTimeException e) {
						throw Exceptions.show(e);
					}
					return new Session(sessionId, wmVersion, userName, validUntil);
				}
			}
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}
}
