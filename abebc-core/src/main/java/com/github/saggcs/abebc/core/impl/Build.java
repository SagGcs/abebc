package com.github.saggcs.abebc.core.impl;

public class Build {
	private final Session session;
	private final String buildId;
	private final String projectName;
	private final String projectVersion;

	public Build(Session pSession, String pBuildId, String pProjectName, String pProjectVersion) {
		session = pSession;
		buildId = pBuildId;
		projectName = pProjectName;
		projectVersion = pProjectVersion;
	}

	public Session getSession() { return session; }
	public String getBuildId() { return buildId; }
	public String getProjectName() { return projectName; }
	public String getProjectVersion() { return projectVersion; }
}
