package com.github.saggcs.abebc.core.impl;

import java.nio.file.Path;

public class Build {
	private final Session session;
	private final String buildId;
	private final Path destFile;

	public Build(Session pSession, String pBuildId, Path pDestFile) {
		session = pSession;
		buildId = pBuildId;
		destFile = pDestFile;
	}

	public Session getSession() { return session; }
	public String getBuildId() { return buildId; }
	public Path getDestFile() { return destFile; }
}
