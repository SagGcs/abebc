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

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import com.github.jochenw.afw.core.cli.Cli;
import com.github.jochenw.afw.core.inject.AfwCoreOnTheFlyBinder;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.jochenw.afw.core.log.ILog.Level;

public class Main {
	public static class Options {
		private String abebsPassword;
		private URL abebsUrl;
		private String abebsUsername;
		private Path logFile;
		private Level logLevel;
		private Path outputDir;
		private Path projectDir;
		private String projectVersion, buildNumber;
		private String wmVersion;
		private Path destFile;
		private boolean skipTests, skipIsccr;
	}

	protected Options parse(String[] pArgs, Function<String,RuntimeException> pErrorHandler) {
		return Cli.of(new Options())
				.stringOption("abebsPassword", "ap").required().handler((c,s) -> { c.getBean().abebsPassword = s; }).end()
				.stringOption("abebsUrl", "aur").required().handler((c,s) -> {
					try {
						c.getBean().abebsUrl = new URL(s);
					} catch (MalformedURLException e) {
						throw c.error("Invalid value for option " + c.getOptionName()
						              + ": Expected valid URL, got " + s);
					}
				}).end()
				.stringOption("abebsUsername", "aus").required().handler((c,s) -> { c.getBean().abebsUsername = s; }).end()
				.pathOption("logFile", "lf").handler((c,p) -> { c.getBean().logFile = p; }).end()
				.enumOption(Level.class, "logLevel", "ll").handler((c,l) -> { c.getBean().logLevel = l; }).end()
				.pathOption("outputDir", "od").handler((c,p) -> { c.getBean().outputDir = p; }).end()
				.pathOption("projectDir", "pd").dirRequired().defaultValue(".").handler((c,p) -> { c.getBean().projectDir = p; }).end()
				.stringOption("projectVersion", "pv").required().handler((c,s) -> { c.getBean().projectVersion = s; }).end()
				.stringOption("buildNumber", "bn").required().handler((c,s) -> { c.getBean().buildNumber = s; }).end()
				.pathOption("destFileName", "df").handler((c,p) -> { c.getBean().destFile = p; }).end()
				.booleanOption("skipIsccr", "si").handler((c,b) -> { c.getBean().skipIsccr=b; }).end()
				.booleanOption("skipTests", "st").handler((c,b) -> { c.getBean().skipTests=b; }).end()
				.stringOption("wmVersion", "wv").required().handler((c,s) -> { c.getBean().wmVersion = s; }).end()
				.errorHandler(pErrorHandler)
				.beanValidator((b) -> {
					if (b.outputDir == null  &&  b.destFile == null) {
						return "Either of the options --destFileName, or --outputDir, is required.";
					} else if (b.outputDir != null  &&  b.destFile != null) {
						return "The options --destFileName, and --outputDir, are mutually exclusive.";
					}
					return null;
				})
				.parse(pArgs);
	}

	protected void run(Options pOptions) {
		final Level logLevel = Objects.notNull(pOptions.logLevel, Level.INFO);
		final ILogFactory lf;
		if (pOptions.logFile == null) {
			lf = SimpleLogFactory.of(System.out, logLevel);
		} else {
			lf = SimpleLogFactory.of(pOptions.logFile, logLevel);
		}
		final IComponentFactory cf =
				IComponentFactory.builder().onTheFlyBinder(new AfwCoreOnTheFlyBinder()).module(AbebcCore.MODULE.extend((b) -> {
					b.bind(ILogFactory.class).toInstance(lf);
				})).build();
		try {
			Files.createDirectories(pOptions.outputDir);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		final AbebcBean ab = AbebcBean.builder()
				.abebsPassword(pOptions.abebsPassword)
				.abebsUrl(pOptions.abebsUrl)
				.abebsUserName(pOptions.abebsUsername)
				.outputDir(pOptions.outputDir)
				.projectDir(pOptions.projectDir)
				.projectVersion(pOptions.projectVersion)
				.buildNumber(pOptions.buildNumber)
				.destFile(pOptions.destFile)
				.skippingIsccr(pOptions.skipIsccr)
				.skippingTests(pOptions.skipTests)
				.wmVersion(pOptions.wmVersion)
				.build();
		cf.init(ab);
		ab.run(System.out::println);
	}

	public static void main(String[] pArgs) {
		final Main main = new Main();
		final Options options = main.parse(pArgs, Main::usage);
		main.run(options);
	}

	public static RuntimeException usage(String pMsg) {
		final PrintStream ps = System.err;
		if (pMsg != null) {
			ps.println(pMsg);
			ps.println();
		}
		ps.println("Usage: java " + Main.class.getName() + "<Options>");
		ps.println();
		ps.println("Required options are:");
		ps.println(" -abebsPassword=<P> | -ap=<P> Sets the ABE build servers password.");
		ps.println(" -abebsUsername=<U> | -aus=<U> Sets the ABE build servers user name.");
		ps.println(" -abebsUrl=<U> | -aur=<U> Sets the ABE build servers URL.");
		ps.println(" -destFileName=<F> | -df=<F> Sets the destination file name.");
		ps.println(" -outputDir=<D> | -od=<D> Sets the output directory, where the build file");
		ps.println("                          is being created.");
		ps.println(" -projectDir=<D> | -pd=<D> Sets the project directory.");
		ps.println(" -projectVersion=<V> | -pv=<V> Sets the project version.");
		ps.println(" -wmVersion=<V> | -wv=<V> Sets the webMethods version.");
		ps.println();
		ps.println("Other options are:");
		ps.println(" -logFile=<F> | -lf=<F> Sets the log file to use (Default System.out)");
		ps.println(" -logLevel=<L> | -lf=<L> Sets the log level (Either of");
		ps.println("                         TRACE,DEBUG,INFO,WARN, or ERROR, default is INFO.)");
		ps.println(" -skipIsccr=true | -si=true Skip running the ISCCR.");
		ps.println(" -skipTests=true | -st=true Skip running the webMethods test suite.");
		ps.println(" -help | -h Print this help message, and exit with error status.");
		System.exit(1);
		return null;
	}
}
