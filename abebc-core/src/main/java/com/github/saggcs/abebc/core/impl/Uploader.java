
package com.github.saggcs.abebc.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
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
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.FileUtils;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.HttpConnector;
import com.github.jochenw.afw.core.util.HttpConnector.HttpConnection;
import com.github.jochenw.afw.core.util.RestAccess;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.saggcs.abebc.core.api.AbebcBean;


public class Uploader {
	private @LogInject ILog log;
	private @Inject RestAccess rest;

	public void run(AbebcBean pAbebcBean, Build pBuild, Consumer<String> pLogger) {
		log.entering("run", pBuild.getSession().getSessionId(), pBuild.getBuildId(),
				     pAbebcBean.getAbebsUrl());
		final Path outputDir = pAbebcBean.getOutputDir();
		final Path outputFile = pAbebcBean.getDestFile();
		final Holder<Path> outputPath = new Holder<>();
		rest.builder(pAbebcBean.getAbebsUrl())
		    .resource("/rest/upload")
		    .resourceId(pBuild.getSession().getSessionId())
		    .parameter("buildId", pBuild.getBuildId())
		    .header("content-type", "application/zip")
		    .body((out) -> {
				sendFiles(pAbebcBean.getProjectDir(), out);
		    })
		    .consumer((in) -> {
		    	if (outputFile != null) {
		    		pLogger.accept("Creating output file: " + outputFile);
		    		outputPath.set(outputFile);
		    		if (outputFile.getParent() != null) {
		    			Files.createDirectories(outputFile.getParent());
		    		}
		    		try (OutputStream out = Files.newOutputStream(outputFile)) {
		    			Streams.copy(in, out);
		    		}
		    	} else {
		    		pLogger.accept("Extracting output to directory: " + outputDir);
		    		outputPath.set(outputDir);
		    		try (ZipInputStream zin = new ZipInputStream(in)) {
		    			for (;;) {
		    				final ZipEntry ze = zin.getNextEntry();
		    				if (ze == null) {
		    					break;
		    				}
		    				if (!ze.isDirectory()) {
		    					final Path zePath = outputDir.resolve(ze.getName());
		    					if (FileUtils.isWithin(outputDir, zePath)) {
		    						final Path zeDir = zePath.getParent();
		    						if (zeDir != null) {
		    							Files.createDirectories(zeDir);
		    						}
		    						try (OutputStream out = Files.newOutputStream(zePath)) {
		    							Streams.copy(zin, out);
		    						}
		    					} else {
		    						throw new IllegalStateException("Invalid zip entry, file would be outside of output directory: " + ze.getName());
		    					}
		    				}
		    			}
		    		}
  		    	}
		    })
		    .send();
		log.exiting("run", outputPath.get());
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
				final String path = pPackageDir.relativize(pFile).toString().replace('\\', '/');
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
