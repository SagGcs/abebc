package com.github.saggcs.abebc.maven;

import java.net.URL;
import java.nio.file.Path;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.saggcs.abebc.core.api.AbebcBean;
import com.github.saggcs.abebc.core.api.AbebcCore;

@Mojo(name="abe", threadSafe=true)
public class BuildGoal extends AbstractMojo {
	@Parameter(property="abebc:abebsUrl", required=true)
	private URL abebsUrl;
	@Parameter(property="abebc:abebsUserName", required=true)
	private String abebsUserName;
	@Parameter(property="abebc:abebsPassword", required=true)
	private String abebsPassword;
	@Parameter(property="abebc:logLevel", required=false, defaultValue="INFO")
	private Level logLevel;
	@Parameter(property="abebc:logFile", required=false, defaultValue="${project.build.directory}/abebs/abebc.log")
	private Path logFile;
	@Parameter(property="abebc:outputDir", required=false, defaultValue="${project.build.directory}/abebs")
	private Path outputDir;
	@Parameter(property="abebc:projectDir", required=false, defaultValue="${project.basedir}")
	private Path projectDir;
	@Parameter(property="abebc:destFile", required=false, defaultValue="${project.artifactId}-${project.version}-is.zip")
	private Path destFile;
	@Parameter(property="abebc:skippingTests", required=false, defaultValue="false")
	private boolean skippingTests;
	@Parameter(property="abebc:skippingIsccr", required=false, defaultValue="false")
	private boolean skippingIsccr;
	@Parameter(property="abebc:wmVersion", required=false, defaultValue="10.5")
	private String wmVersion;
	@Parameter(property="abebc:projectVersion", required=false, defaultValue="${project.version}")
	private String projectVersion;
	@Parameter(property="abebc:buildNumber", required=true)
	private String buildNumber;
	@Component
	private MavenProject project;


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final Level logLevel = Objects.notNull(this.logLevel, Level.INFO);
		if (this.logFile == null) {
			throw new MojoFailureException("Required parameter not configured: " + logFile);
		}
		final ILogFactory lf = SimpleLogFactory.of(this.logFile, logLevel);
		final IComponentFactory cf =
				IComponentFactory.builder().module(AbebcCore.MODULE.extend((b) -> {
					b.bind(ILogFactory.class).toInstance(lf);
				})).build();
		final AbebcBean ab = AbebcBean.builder()
				.abebsPassword(this.abebsPassword)
				.abebsUrl(this.abebsUrl)
				.abebsUserName(this.abebsUserName)
				.outputDir(this.outputDir)
				.projectDir(projectDir)
				.destFile(destFile)
				.skippingIsccr(skippingIsccr)
				.skippingTests(skippingTests)
				.wmVersion(wmVersion)
				.projectVersion(projectVersion)
				.buildNumber(buildNumber)
				.build();
		cf.init(ab);
		ab.run((s) -> getLog().info(s));
	}
}
