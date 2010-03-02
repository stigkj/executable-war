package net.nisgits.executablewar.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author Stig Kleppe-Jorgensen, 2010.03.01
 * @fixme add description
 */
public abstract class ExecutableWarMojoCommon extends AbstractMojo {
	/**
	 * The Maven Project Object
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;
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
	 * The directory for the generated WAR.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String buildDirectory;
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

	protected void extractExecWarClassesTo(final File expandedWarDirectory) {
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

	public void execute() throws MojoExecutionException, MojoFailureException {
		verifyCorrectPackaging(project.getPackaging());

		idToArtifact = mapIdToArtifact();
		final File expandedWarDirectory = new File(buildDirectory, warName);

		extractExecWarClassesTo(expandedWarDirectory);
		copyDependenciesTo(expandedWarDirectory);

		extraSteps();
	}

	/**
	 * Should be implemented by subclasses that need to do extra stuff when plugin is executing
	 *
	 * @throws MojoExecutionException when something goes wrong
	 */
	protected void extraSteps() throws MojoExecutionException {
		// Should be implemented by subclasses that need to do extra stuff when plugin is executing
	}

	private void verifyCorrectPackaging(final String packaging) throws MojoFailureException {
		final String correctPackaging = correctPackaging();

		if (!packaging.equals(correctPackaging)) {
			throw new MojoFailureException(
					"Can only be run within a project with '" + correctPackaging +
							"' packaging, that is, when building a web application");
		}
	}

	protected abstract String correctPackaging();

	private Map<String, Artifact> mapIdToArtifact() {
		return Maps.uniqueIndex(pluginArtifacts, new Function<Artifact, String>() {
			public String apply(final Artifact from) {
				return from.getGroupId() + ':' + from.getArtifactId();
			}
		});
	}

	private void copyDependenciesTo(final File expandedWarDirectory) throws MojoExecutionException {
		copyArtifactByIdToDirectory("net.java.dev.jna:jna", expandedWarDirectory);
		copyArtifactByIdToDirectory("com.sun.akuma:akuma", expandedWarDirectory);
		copyArtifactByIdToDirectory("net.sourceforge.winstone:winstone", expandedWarDirectory);
	}

	private void copyArtifactByIdToDirectory(final String id, final File expandedWarDirectory)
			throws MojoExecutionException {
		final Artifact artifact = idToArtifact.get(id);
		copyArtifactToDirectory(artifact, expandedWarDirectory);
	}

	private void copyArtifactToDirectory(final Artifact artifact, final File expandedWarDirectory)
			throws MojoExecutionException {
		try {
			FileUtils.copyFile(artifact.getFile(), new File(expandedWarDirectory, artifact.getArtifactId() + ".jar"));
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Could not find file for artifact", e);
		} catch (IOException e) {
			throw new MojoExecutionException("Problems copying artifact", e);
		}
	}

	/**
	 * File selector that selects only class files
	 *
	 * @see FileSelector
	 */
	private static class IsClassFileSelector implements FileSelector {
		public boolean isSelected(FileInfo fileInfo) throws IOException {
			return fileInfo.isFile() && fileInfo.getName().endsWith(".class");
		}
	}
}
