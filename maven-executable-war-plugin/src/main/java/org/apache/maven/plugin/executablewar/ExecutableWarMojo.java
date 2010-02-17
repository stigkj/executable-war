package org.apache.maven.plugin.executablewar;

import java.io.File;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
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
	private String outputDirectory;

	/**
	 * To look up Archiver/UnArchiver implementations
	 *
	 * @component
	 */
	protected ArchiverManager archiverManager;

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

	public void execute() throws MojoExecutionException, MojoFailureException {
		extractExecWarHeader();
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
										element(name("mainClass"), "org.apache.maven.plugins.executablewar.Main")
								)
						)
				),
				executionEnvironment(
						project,
						session,
						pluginManager
				)
		);

		System.out.println("RUNNING");
	}

	private void extractExecWarHeader() {
		final Artifact artifact = Iterables.find(pluginArtifacts, new Predicate<Artifact>() {
			public boolean apply(Artifact input) {
				return input.getGroupId().equals("org.apache.maven.plugins") &&
						input.getArtifactId().equals("maven-executable-war-library");
			}
		});
		final File artifactFile = artifact.getFile();

		try {
			final UnArchiver unArchiver = archiverManager.getUnArchiver(artifactFile);
			unArchiver.setSourceFile(artifactFile);

			final File outputDir = new File(outputDirectory, warName);
			outputDir.mkdirs();
			unArchiver.setFileSelectors(new FileSelector[]{new IsClassFileSelector()});
			unArchiver.setDestDirectory(outputDir);
			unArchiver.extract();
		} catch (NoSuchArchiverException e) {
			throw new IllegalStateException("Could not get unarchiver for " + artifactFile, e);
		} catch (ArchiverException e) {
			throw new IllegalStateException("Could not extract " + artifactFile, e);
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
