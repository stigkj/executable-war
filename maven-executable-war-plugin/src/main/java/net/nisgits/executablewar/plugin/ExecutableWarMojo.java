package net.nisgits.executablewar.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.util.FileUtils;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * Build an executable WAR file.
 *
 * @author <a href="from.executable-war@nisgits.net">Stig Kleppe-Jørgensen</a>
 * @version $Id: $
 * @goal war
 * @phase package
 * @requiresDependencyResolution runtime
 * TODO make a JDK v1.4 version too with that maven plugin
 */
public class ExecutableWarMojo extends AbstractMojo {
	/**
	 * The Maven Project Object
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * The Maven Session Object
	 *
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	private MavenSession session;

	/**
	 * The Maven PluginManager Object
	 *
	 * @component
	 * @required
	 */
	private PluginManager pluginManager;

	/**
	 * The directory for the generated WAR.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String buildDirectory;

	/**
	 * To look up Archiver/UnArchiver implementations
	 *
	 * @component
	 */
	private ArchiverManager archiverManager;

	/**
	 * The dependencies declared in your plugin.
	 *
	 * @parameter default-value="${plugin.artifacts}"
	 */
	private List<Artifact> pluginArtifacts;

	/**
	 * The name of the generated WAR.
	 *
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 */
	private String warName;

	/**
	 * Maps "groupId:artifactId" to the corresponding artifact.
	 * Built from pluginArtifacts.
	 */
	private Map<String, Artifact> idToArtifact;

	public void execute() throws MojoExecutionException, MojoFailureException {
		idToArtifact = mapIdToArtifact();
		final File expandedWarDirectory = new File(buildDirectory, warName);

		extractExecWarClassesTo(expandedWarDirectory);
		copyDependenciesTo(expandedWarDirectory);

		executeMojo(
				plugin(
						groupId("org.apache.maven.plugins"),
						artifactId("maven-war-plugin"),
						version("2.1-beta-1")
				),
				goal("war"),
				configuration(
						element(name("archive"),
								element(name("manifest"),
										element(name("mainClass"), "net.nisgits.executablewar.library.Main")
								)
						)
				),
				executionEnvironment(
						project,
						session,
						pluginManager
				)
		);
	}

	private Map<String, Artifact> mapIdToArtifact() {
		System.out.println("pluginArtifacts = " + pluginArtifacts);
		return Maps.uniqueIndex(pluginArtifacts, new Function<Artifact, String>() {
			public String apply(Artifact from) {
				return from.getGroupId() + ":" + from.getArtifactId();
			}
		});
	}

	private void extractExecWarClassesTo(final File expandedWarDirectory) {
        // LATER is it possible to not hard code these dependencies? 
		final Artifact artifact = idToArtifact.get("net.nisgits.executablewar:executable-war-library");
		final File artifactFile = artifact.getFile();

		try {
			final UnArchiver unArchiver = archiverManager.getUnArchiver(artifactFile);
			unArchiver.setSourceFile(artifactFile);

			expandedWarDirectory.mkdirs();
			unArchiver.setFileSelectors(new FileSelector[]{new IsClassFileSelector()});
			unArchiver.setDestDirectory(expandedWarDirectory);
			unArchiver.extract();
		} catch (NoSuchArchiverException e) {
			throw new IllegalStateException("Could not get unarchiver for " + artifactFile, e);
		} catch (ArchiverException e) {
			throw new IllegalStateException("Could not extract " + artifactFile, e);
		}
	}

	private void copyDependenciesTo(File expandedWarDirectory) throws MojoExecutionException {
		copyArtifactByIdToDirectory("net.java.dev.jna:jna", expandedWarDirectory);
		copyArtifactByIdToDirectory("com.sun.akuma:akuma", expandedWarDirectory);
		copyArtifactByIdToDirectory("org.jvnet.hudson.winstone:winstone", expandedWarDirectory);
	}

	private void copyArtifactByIdToDirectory(final String id, File expandedWarDirectory) throws MojoExecutionException {
		final Artifact artifact = idToArtifact.get(id);
		copyArtifactToDirectory(artifact, expandedWarDirectory);
	}

	private void copyArtifactToDirectory(Artifact artifact, File expandedWarDirectory) throws MojoExecutionException {
		try {
			FileUtils.copyFile(artifact.getFile(), new File(expandedWarDirectory, artifact.getArtifactId() + ".jar"));
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Could not find file for artifact", e);
		} catch (IOException e) {
			throw new MojoExecutionException("Problems copying artifact", e);
		}
	}

	/**
	 * Selects only class files
	 */
	private static class IsClassFileSelector implements FileSelector {
		public boolean isSelected(FileInfo fileInfo) throws IOException {
			return fileInfo.isFile() && fileInfo.getName().endsWith(".class");
		}
	}
}
