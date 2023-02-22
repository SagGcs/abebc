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
package io.github.saggcs.abebc.ant;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.di.api.IComponentFactory;

import io.github.saggcs.abebc.core.api.AbebcBean;
import io.github.saggcs.abebc.core.api.AbebcCore;

public class AbebcAntTask extends Task {
	private URL abebsUrl;
	private String abebsUserName, abebsPassword;
	private String logLevel;
	private File logFile;
	private File outputFile, projectDir;
	private File destFile;
	private boolean skippingTests, skippingIsccr;
	private String wmVersion, projectVersion, buildNumber;

	public String getLogLevel() { return logLevel; }
	public void setLogLevel(String pLogLevel) { logLevel = pLogLevel; }
	public File getLogFile() { return logFile; }
	public void setLogFile(File pLogFile) { logFile = pLogFile; }
	public URL getAbebsUrl() { return abebsUrl; }
	public void setAbebsUrl(URL pUrl) { abebsUrl = pUrl; }
	public String getAbebsUserName() { return abebsUserName; }
	public void setAbebsUserName(String pAbebsUserName) { abebsUserName = pAbebsUserName; }
	public String getAbebsPassword() { return abebsPassword; }
	public void setAbebsPassword(String pPassword) { abebsPassword = pPassword; }
	public File getOutputFile() { return outputFile; }
	public void setOutputFile(File pOutputFile) { outputFile = pOutputFile; }
	public File getProjectDir() { return projectDir; }
	public void setProjectDir(File pProjectDir) { projectDir = pProjectDir; }
	public boolean isSkippingTests() { return skippingTests; }
	public void setSkippingTests(boolean pSkippingTests) { skippingTests = pSkippingTests; }
	public boolean isSkippingIsccr() { return skippingIsccr; }
	public void setSkippingIsccr(boolean pSkippingIsccr) { skippingIsccr = pSkippingIsccr; }
	public String getWmVersion() { return wmVersion; }
	public void setWmVersion(String pWmVersion) { wmVersion = pWmVersion; }
	public String getProjectVersion() { return projectVersion; }
	public void setProjectVersion(String pProjectVersion) { projectVersion = pProjectVersion; }
	public String getBuildNumber() { return buildNumber; }
	public void setBuildNumber(String pBuildNumber) { buildNumber = pBuildNumber; }

	@Override
	public void execute() throws BuildException {
		final URL url = getAbebsUrl();
		if (url == null) {
			throw new BuildException("Missing value for attribute abebsUrl");
		}
		final String userName = getAbebsUserName();
		if (userName == null  ||  userName.length() == 0) {
			throw new BuildException("Missing, or empty, value for attribute abebsUserName");
		}
		final String password = getAbebsPassword();
		if (password == null  ||  password.length() == 0) {
			throw new BuildException("Missing, or empty, value for attribute abebsPassword");
		}
		final File outDirFile = getOutputFile();
		if (outDirFile == null) {
			throw new BuildException("Missing value for attribute outputDir");
		}
		final Path outDir = outDirFile.toPath().getParent();
		if (!Files.isDirectory(outDir)) {
			throw new BuildException("Invalid value for attribute outputFile:"
					+ " Output directory " + outDir + " does not exist, or is no directory.");
		}
		final Path destFile;
		if (this.destFile == null) {
			destFile = null;
		} else {
			destFile = this.destFile.toPath();
		}
		final File projectDirFile = getProjectDir();
		final Path projectDir;
		if (projectDirFile == null) {
			projectDir = getProject().getBaseDir().toPath();
		} else {
			projectDir = projectDirFile.toPath();
		}
		if (!Files.isDirectory(projectDir)) {
			throw new BuildException("Invalid value for attribute projectDir:"
					+ " Project directory " + projectDirFile.getPath() + " does not exist, or is no directory.");
		}
		final String wmVer = getWmVersion();
		if (wmVer == null  ||  wmVer.length() == 0) {
			throw new BuildException("Missing, or empty, value for attribute wmVersion");
		}
		final String prjVer = getProjectVersion();
		if (prjVer == null  ||  prjVer.length() == 0) {
			throw new BuildException("Missing, or empty, value for attribute projectVersion");
		}
		final String buildNmbr = getBuildNumber();
		if (buildNmbr == null  ||  buildNmbr.length() == 0) {
			throw new BuildException("Missing, or empty, value for attribute buildNumber");
		}
		
		final String logLevelStr = getLogLevel();
		final Level logLevel;
		if (logLevelStr == null  ||  logLevelStr.length() == 0) {
			logLevel = Level.INFO;
		} else {
			try {
				logLevel = Level.valueOf(logLevelStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BuildException("Invalid value for attribute logLevel:"
						+ " Expected TRACE|DEBUG|INFO|WARN|ERROR|FATAL, got " + logLevelStr);
			}
		}
		final File logFile = getLogFile();
		final ILogFactory lf;
		if (logFile == null) {
			lf = SimpleLogFactory.of(System.out, logLevel);
		} else {
			lf = SimpleLogFactory.of(logFile.toPath(), logLevel);
		}
		final IComponentFactory cf =
				IComponentFactory.builder().module(AbebcCore.MODULE.extend((b) -> {
					b.bind(ILogFactory.class).toInstance(lf);
				})).build();
		final AbebcBean ab = AbebcBean.builder()
				.abebsPassword(password)
				.abebsUrl(url)
				.abebsUserName(userName)
				.outputDir(outDir)
				.projectDir(projectDir)
				.destFile(destFile)
				.skippingIsccr(isSkippingIsccr())
				.skippingTests(isSkippingTests())
				.wmVersion(wmVer)
				.projectVersion(prjVer)
				.buildNumber(buildNmbr)
				.build();
		cf.init(ab);
		ab.run((s) -> getProject().log(s));
	}

}
