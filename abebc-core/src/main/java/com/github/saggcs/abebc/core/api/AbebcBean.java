package com.github.saggcs.abebc.core.api;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import javax.inject.Inject;

import com.github.jochenw.afw.core.util.AbstractBuilder;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.saggcs.abebc.core.impl.Build;
import com.github.saggcs.abebc.core.impl.BuildProvider;
import com.github.saggcs.abebc.core.impl.Session;
import com.github.saggcs.abebc.core.impl.SessionProvider;
import com.github.saggcs.abebc.core.impl.Uploader;

/**
 * This class is the core implementation of the ABE Build Client.
 * Typical usage will be through either of the frontends (CLI.
 * Maven Plugin, Ant Task), that are embeddinfg an instance
 * of this class.
 */
public class AbebcBean {
	public static class Builder extends AbstractBuilder<AbebcBean,Builder> {
		private Path projectDir;
		private String projectName, projectVersion;
		private Path outputDir;
		private String wmVersion;
		private URL abebsUrl;
		private String abebsUserName;
		private String abebsPassword;
		private boolean skippingTests;
		private boolean skippingIsccr;
		private String destFileName;

		Builder() {}
		
		public Path getOutputDir() { return outputDir; }
		Builder outputDir(Path pOutputDir) {
			final Path outputDir = Objects.requireNonNull(pOutputDir, "Output directory");
			assertMutable();
			this.outputDir = outputDir;
			return this;
		}
		public Path getProjectDir() { return projectDir; }
		Builder projectDir(Path pProjectDir) {
			final Path projectDir = Objects.requireNonNull(pProjectDir, "Project directory");
			assertMutable();
			this.projectDir = projectDir;
			return this;
		}
		public String getProjectName() { return projectName; }
		Builder projectName(String pProjectName) {
			final String projectName = Objects.requireNonNull(pProjectName, "Project name");
			assertMutable();
			this.projectName = projectName;
			return this;
		}
		public String getProjectVersion() { return projectVersion; }
		Builder projectVersion(String pProjectVersion) {
			final String projectVersion = Objects.requireNonNull(pProjectVersion, "Project version");
			assertMutable();
			this.projectVersion = projectVersion;
			return this;
		}
		public String getWmVersion() { return wmVersion; }
		Builder wmVersion(String pWmVersion) {
			final String wmVersion = Objects.requireNonNull(pWmVersion, "webMethods version");
			assertMutable();
			this.wmVersion = wmVersion;
			return this;
		}
		public URL getAbebsUrl() { return abebsUrl; }
		Builder abebsUrl(URL pUrl) {
			final URL abebsUrl = Objects.requireNonNull(pUrl, "AbeBS URL");
			assertMutable();
			this.abebsUrl = abebsUrl;
			return this;
		}
		public String getAbebsUserName() { return abebsUserName; }
		Builder abebsUserName(String pUserName) {
			final String abebsUserName = Objects.requireNonNull(pUserName, "AbeBS user name");
			assertMutable();
			this.abebsUserName = abebsUserName;
			return this;
		}
		public String getAbebsPassword() { return abebsPassword; }
		Builder abebsPassword(String pPassword) {
			final String abebsPassword = Objects.requireNonNull(pPassword, "AbeBS password");
			assertMutable();
			this.abebsPassword = abebsPassword;
			return this;
		}
		public boolean isSkippingTests() { return skippingTests; }
		Builder skippingTests() {
			return skippingTests(true);
		}
		Builder skippingTests(boolean pSkippingTests) {
			assertMutable();
			skippingTests = pSkippingTests;
			return this;
		}
		public boolean isSkippingIsccr() { return skippingIsccr; }
		Builder skippingIsccr() {
			return skippingIsccr(true);
		}
		Builder skippingIsccr(boolean pSkippingIsccr) {
			assertMutable();
			skippingIsccr = pSkippingIsccr;
			return this;
		}
		public String getDestFileName() { return destFileName; }
		public Builder destFileName(String pDestFileName) {
			final String destFileName = Objects.requireNonNull(pDestFileName, "Destination file name");
			assertMutable();
			this.destFileName = destFileName;
			return this;
		}
		@Override public AbebcBean newInstance() {
			return new AbebcBean(getProjectDir(), getOutputDir(), getWmVersion(),
					             getDestFileName(),
					             getAbebsUrl(), getAbebsUserName(),
					             getAbebsPassword(), isSkippingTests(),
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
	private final String destFileName;

	AbebcBean(Path pProjectDir, Path pOutputDir, String pWmVersion,
			  String pDestFileName, URL pAbebsUrl, String pAbebsUserName,
			  String pAbebsPassword, boolean pSkippingTests, boolean pSkippingIsccr) {
		projectDir = pProjectDir;
		outputDir = pOutputDir;
		wmVersion = pWmVersion;
		abebsUrl = pAbebsUrl;
		abebsUserName = pAbebsUserName;
		abebsPassword = pAbebsPassword;
		skippingTests = pSkippingTests;
		skippingIsccr = pSkippingIsccr;
		destFileName = pDestFileName;
	}

	public Path getProjectDir() { return projectDir; }
	public Path getOutputDir() { return outputDir; }
	public String getWmVersion() { return wmVersion; }
	public URL getAbebsUrl() { return abebsUrl; }
	public String getAbebsUserName() { return abebsUserName; }
	public String getAbebsPassword() { return abebsPassword; }
	public boolean isSkippingTests() { return skippingTests; }
	public boolean isSkippingIsccr() { return skippingIsccr; }
	public String getDestFileName() { return destFileName; }

	public void run() {
		final Session session = sessionProvider.getSession(this);
		final Build build = buildProvider.getBuild(this, session);
		uploader.run(this, build);
	}
}
