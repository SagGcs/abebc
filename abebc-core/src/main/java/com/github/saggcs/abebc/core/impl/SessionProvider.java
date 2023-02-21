package com.github.saggcs.abebc.core.impl;

import java.io.InputStream;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.inject.Inject;

import com.github.jochenw.afw.core.data.Data;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.RestAccess;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.saggcs.abebc.core.api.AbebcBean;
import com.github.saggcs.abebc.uttill.JsonUtils;

public class SessionProvider {
	private @Inject RestAccess rest;
	private @LogInject ILog log;

	public Session getSession(AbebcBean pBean) {
		log.entering("getSession", pBean.getAbebsUrl(), pBean.getWmVersion(), pBean.getAbebsUserName());
		final FailableFunction<InputStream,Session,?> callable = (in) -> {
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
			log.exiting("getSession", sessionId, wmVersion, userName, validUntilStr);
			return new Session(sessionId, wmVersion, userName, validUntil);
		};
		return rest.builder(pBean.getAbebsUrl())
				.resource("/rest/authenticate")
				.parameter("wmVersion", pBean.getWmVersion())
				.basicAuth(pBean.getAbebsUserName(), pBean.getAbebsPassword())
				.call(callable);
	}
}
