package com.github.saggcs.abebc.core.impl;

public class Build {
	private final Session session;
	private final String buildId;
	private final String destFileName;

	public Build(Session pSession, String pBuildId, String pDestFileName) {
		session = pSession;
		buildId = pBuildId;
		destFileName = pDestFileName;
	}

	public Session getSession() { return session; }
	public String getBuildId() { return buildId; }
	public String getDestFileName() { return destFileName; }
}
