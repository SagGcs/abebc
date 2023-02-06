package com.github.saggcs.abebc.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

public class BuildProvider {
	private @Inject HttpConnector httpConnector;
	private @LogInject ILog log;

	public Build getBuild(AbebcBean pBean, Session pSession) {
		log.entering("getBuild", pSession.getSessionId(), pBean.getDestFileName());
		try {
			final URL url = new URL(pBean.getAbebsUrl(), "/rest/build/"
		            + pSession.getSessionId());
			try (HttpConnection conn = httpConnector.connect(url)) {
				final HttpURLConnection urlConn = conn.getUrlConnection();
				try (final InputStream in = urlConn.getInputStream()) {
					final Map<String,Object> response = JsonUtils.parse(in);
					final String buildId = Data.requireString(response, "buildId");
					final String wmVersion = Data.requireString(response, "wmVersion");
					if (!pSession.getWmVersion().equals(wmVersion)) {
						throw new IllegalStateException("Server reports unexpected value"
							+ " for parameter wmVersion: Expected " + pSession.getWmVersion()
							+ ", got " + wmVersion);
					}
					final String userName = Data.requireString(response, "userName");
					if (!pSession.getUserName().equals(userName)) {
						throw new IllegalStateException("Server reports unexpected value"
							+ " for parameter userName: Expected " + pSession.getUserName()
							+ ", got " + userName);
					}
					log.exiting("getBuild", pSession.getSessionId(), buildId, pBean.getDestFileName());
					return new Build(pSession, buildId, pBean.getDestFileName());
				}
			}
		} catch (IOException e) {
			log.error("getBuild", e.getClass().getName() + ": " + e.getMessage());
			throw Exceptions.show(e);
		}
	}

}
