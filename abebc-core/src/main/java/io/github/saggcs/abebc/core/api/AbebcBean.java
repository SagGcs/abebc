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
package io.github.saggcs.abebc.core.api;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.github.jochenw.afw.core.util.AbstractBuilder;

import io.github.saggcs.abebc.core.impl.Build;
import io.github.saggcs.abebc.core.impl.BuildProvider;
import io.github.saggcs.abebc.core.impl.Session;
import io.github.saggcs.abebc.core.impl.SessionProvider;
import io.github.saggcs.abebc.core.impl.Uploader;

/**
 * This class is the core implementation of the ABE Build Client.
 * Typical usage will be through either of the frontends (CLI.
 * Maven Plugin, Ant Task), that are embeddinfg an instance
 * of this class.
 */
public class AbebcBean {
	public static class Builder extends AbstractBuilder<AbebcBean,Builder> {
		private Path projectDir;
		private String buildNumber, projectVersion;
		private Path outputDir;
		private String wmVersion;
		private URL abebsUrl;
		private String abebsUserName;
		private String abebsPassword;
		private boolean skippingTests;
		private boolean skippingIsccr;
		private Path destFile;

		Builder() {}
		
		public Path getOutputDir() { return outputDir; }
		public Builder outputDir(Path pOutputDir) {
			final Path outputDir = Objects.requireNonNull(pOutputDir, "Output directory");
			assertMutable();
			this.outputDir = outputDir;
			return this;
		}
		public Path getProjectDir() { return projectDir; }
		public Builder projectDir(Path pProjectDir) {
			final Path projectDir = Objects.requireNonNull(pProjectDir, "Project directory");
			assertMutable();
			this.projectDir = projectDir;
			return this;
		}
		public String getBuildNumber() { return buildNumber; }
		public Builder buildNumber(String pBuildNumber) {
			final String buildNumber = Objects.requireNonNull(pBuildNumber, "Build number");
			assertMutable();
			this.buildNumber = buildNumber;
			return this;
		}
		public String getProjectVersion() { return projectVersion; }
		public Builder projectVersion(String pProjectVersion) {
			final String projectVersion = Objects.requireNonNull(pProjectVersion, "Project version");
			assertMutable();
			this.projectVersion = projectVersion;
			return this;
		}
		public String getWmVersion() { return wmVersion; }
		public Builder wmVersion(String pWmVersion) {
			final String wmVersion = Objects.requireNonNull(pWmVersion, "webMethods version");
			assertMutable();
			this.wmVersion = wmVersion;
			return this;
		}
		public URL getAbebsUrl() { return abebsUrl; }
		public Builder abebsUrl(URL pUrl) {
			final URL abebsUrl = Objects.requireNonNull(pUrl, "AbeBS URL");
			assertMutable();
			this.abebsUrl = abebsUrl;
			return this;
		}
		public String getAbebsUserName() { return abebsUserName; }
		public Builder abebsUserName(String pUserName) {
			final String abebsUserName = Objects.requireNonNull(pUserName, "AbeBS user name");
			assertMutable();
			this.abebsUserName = abebsUserName;
			return this;
		}
		public String getAbebsPassword() { return abebsPassword; }
		public Builder abebsPassword(String pPassword) {
			final String abebsPassword = Objects.requireNonNull(pPassword, "AbeBS password");
			assertMutable();
			this.abebsPassword = abebsPassword;
			return this;
		}
		public boolean isSkippingTests() { return skippingTests; }
		public Builder skippingTests() {
			return skippingTests(true);
		}
		public Builder skippingTests(boolean pSkippingTests) {
			assertMutable();
			skippingTests = pSkippingTests;
			return this;
		}
		public boolean isSkippingIsccr() { return skippingIsccr; }
		public Builder skippingIsccr() {
			return skippingIsccr(true);
		}
		public Builder skippingIsccr(boolean pSkippingIsccr) {
			assertMutable();
			skippingIsccr = pSkippingIsccr;
			return this;
		}
		public Path getDestFile() { return destFile; }
		public Builder destFile(Path pDestFile) {
			final Path destFile = Objects.requireNonNull(pDestFile, "Destination file");
			assertMutable();
			this.destFile = destFile;
			return this;
		}
		@Override public AbebcBean newInstance() {
			return new AbebcBean(getProjectDir(), getOutputDir(), getWmVersion(),
					             getDestFile(),
					             getAbebsUrl(), getAbebsUserName(),
					             getAbebsPassword(), getProjectVersion(),
					             getBuildNumber(), isSkippingTests(),
					             isSkippingIsccr());
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private @Inject SessionProvider sessionProvider;
	private @Inject BuildProvider buildProvider;
	private @Inject Uploader uploader;

	private final Path projectDir;
	private final Path outputDir;
	private final String wmVersion;
	private final URL abebsUrl;
	private final String abebsUserName;
	private final String abebsPassword;
	private final boolean skippingTests;
	private final boolean skippingIsccr;
	private final Path destFile;
	private final String projectVersion, buildNumber;

	AbebcBean(Path pProjectDir, Path pOutputDir, String pWmVersion,
			  Path pDestFile, URL pAbebsUrl, String pAbebsUserName,
			  String pAbebsPassword, String pProjectVersion, String pBuildNumber,
			  boolean pSkippingTests, boolean pSkippingIsccr) {
		projectDir = pProjectDir;
		outputDir = pOutputDir;
		wmVersion = pWmVersion;
		abebsUrl = pAbebsUrl;
		abebsUserName = pAbebsUserName;
		abebsPassword = pAbebsPassword;
		projectVersion = pProjectVersion;
		buildNumber = pBuildNumber;
		skippingTests = pSkippingTests;
		skippingIsccr = pSkippingIsccr;
		destFile = pDestFile;
	}

	public Path getProjectDir() { return projectDir; }
	public Path getOutputDir() { return outputDir; }
	public String getWmVersion() { return wmVersion; }
	public URL getAbebsUrl() { return abebsUrl; }
	public String getAbebsUserName() { return abebsUserName; }
	public String getAbebsPassword() { return abebsPassword; }
	public String getProjectVersion() { return projectVersion; }
	public String getBuildNumber() { return buildNumber; }
	public boolean isSkippingTests() { return skippingTests; }
	public boolean isSkippingIsccr() { return skippingIsccr; }
	public Path getDestFile() { return destFile; }

	public void run(Consumer<String> pLogger) {
		final Session session = sessionProvider.getSession(this);
		final Build build = buildProvider.getBuild(this, session);
		uploader.run(this, build, pLogger);
	}
}
