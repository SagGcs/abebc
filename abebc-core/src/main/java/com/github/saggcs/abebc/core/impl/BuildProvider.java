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
import com.github.jochenw.afw.core.util.RestAccess;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.saggcs.abebc.core.api.AbebcBean;
import com.github.saggcs.abebc.uttill.JsonUtils;

public class BuildProvider {
	private @Inject RestAccess rest;
	private @LogInject ILog log;

	public Build getBuild(AbebcBean pBean, Session pSession) {
		log.entering("getBuild", pSession.getSessionId(), pBean.getDestFile());
		return rest.builder(pBean.getAbebsUrl())
		           .resource("/rest/build")
		           .resourceId(pSession.getSessionId())
		           .parameter("projectVersion", pBean.getProjectVersion())
		           .parameter("buildNumber", pBean.getBuildNumber())
		           .call((in) -> {
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
		        	   log.exiting("getBuild", pSession.getSessionId(), buildId, pBean.getDestFile());
		        	   return new Build(pSession, buildId, pBean.getDestFile());
		           });
	}

}
