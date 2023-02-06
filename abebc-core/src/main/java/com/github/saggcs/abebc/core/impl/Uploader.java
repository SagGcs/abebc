
package com.github.saggcs.abebc.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.HttpConnector;
import com.github.jochenw.afw.core.util.HttpConnector.HttpConnection;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.saggcs.abebc.core.api.AbebcBean;


public class Uploader {
	private @LogInject ILog log;
	private @Inject HttpConnector httpConnector;

	public void run(AbebcBean pAbebcBean, Build pBuild) {
		log.entering("run", pBuild.getSession().getSessionId(), pBuild.getBuildId(),
				     pAbebcBean.getAbebsUrl());
		try {
			final URL url = new URL(pAbebcBean.getAbebsUrl(), "/rest/upload/"
		            + pBuild.getSession().getSessionId() + "?buildId="
		            + URLEncoder.encode(pBuild.getBuildId(), StandardCharsets.UTF_8));
			try (HttpConnection conn = httpConnector.connect(url)) {
				final HttpURLConnection urlConn = conn.getUrlConnection();
				urlConn.setRequestMethod("POST");
				urlConn.setDoOutput(true);
				urlConn.setDoInput(true);
				try (final OutputStream out = urlConn.getOutputStream()) {
					sendFiles(pAbebcBean.getProjectDir(), out);
				}
				try (final InputStream in = urlConn.getInputStream()) {
					final Path outputPath = pAbebcBean.getOutputDir().resolve(pAbebcBean.getDestFileName());
					Files.createDirectories(outputPath.getParent());
					try (OutputStream out = Files.newOutputStream(outputPath)) {
						Streams.copy(in, out);
					}
				}
			}
		} catch (IOException e) {
			log.error("getBuild", e.getClass().getName() + ": " + e.getMessage());
			throw Exceptions.show(e);
		}
	}

	protected void sendFiles(Path pProjectDir, OutputStream pOut) {
		final Set<Path> configDirs = new HashSet<>();
		try (final ZipOutputStream zos = new ZipOutputStream(pOut)) {
			final Map<String,Path> packages = findPackages(pProjectDir);
			packages.forEach((packageName, packageDir) -> {
				final Path packageDirParent = packageDir.getParent();
				if ("packages".equals(packageDirParent.getFileName().toString())) {
					final Path configDir = packageDirParent.resolveSibling("config");
					if (Files.isDirectory(configDir)) {
						configDirs.add(configDir.toAbsolutePath());
					}
				}
				sendPackage(zos, packageName, packageDir);
			});
			configDirs.forEach((cd) -> {
				sendConfigDir(zos, cd);
			});
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	protected void sendConfigDir(ZipOutputStream pOut, Path pConfigDir) {
		final FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttrs) throws IOException {
				final String path = pConfigDir.resolve(pFile).toString().replace('\\', '/');
				sendFile(pOut, pFile, pAttrs, "IS/config/" + path);
				return FileVisitResult.CONTINUE;
			}
		};
		try {
			Files.walkFileTree(pConfigDir, fv);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	protected void sendPackage(ZipOutputStream pOut, String pPackageName, Path pPackageDir) {
		final FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttrs) throws IOException {
				final String path = pPackageDir.resolve(pFile).toString().replace('\\', '/');
				sendFile(pOut, pFile, pAttrs, "IS/packages/" + pPackageName + "/" + path);
				return FileVisitResult.CONTINUE;
			}
		};
		try {
			Files.walkFileTree(pPackageDir, fv);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}
	
	protected void sendFile(ZipOutputStream pOut, Path pFile, BasicFileAttributes pAttrs, String pFileName) {
		final ZipEntry ze = new ZipEntry(pFileName);
		ze.setSize(pAttrs.size());
		ze.setCreationTime(pAttrs.creationTime());
		ze.setMethod(ZipEntry.DEFLATED);
		ze.setLastModifiedTime(pAttrs.lastModifiedTime());
		try {
			pOut.putNextEntry(ze);
			try (InputStream in = Files.newInputStream(pFile)) {
				Streams.copy(in, pOut);
			}
			pOut.closeEntry();
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}
	protected Map<String,Path> findPackages(Path pProjectDirectory) {
		final Map<String,Path> packages = new HashMap<>();
		final FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path pDir, BasicFileAttributes pAttrs) throws IOException {
				final Path manifestFile = pDir.resolve("manifest.v3");
				final Path dirRelative = pProjectDirectory.relativize(pDir);
				if (Files.isRegularFile(manifestFile)) {
					final String packageName = pDir.getFileName().toString();
					final Path existingPackageDir = packages.put(packageName, dirRelative);
					if (existingPackageDir != null) {
						throw new IllegalStateException("Duplicate package name "
								+ packageName + " for directories "
								+ existingPackageDir + ", and " + dirRelative);
					}
					return FileVisitResult.SKIP_SUBTREE;
				} else {
					return FileVisitResult.CONTINUE;
				}
			}
		};
		try {
			Files.walkFileTree(pProjectDirectory, fv);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return packages;
	}
}
