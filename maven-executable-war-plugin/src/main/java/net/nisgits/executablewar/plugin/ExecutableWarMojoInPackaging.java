package net.nisgits.executablewar.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginManager;

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
 * TODO make a JDK v1.4 version too with that maven plugin
 *
 * @goal war
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class ExecutableWarMojoInPackaging extends ExecutableWarMojoCommon {
	/**
	 * The Maven Session object
	 *
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	private MavenSession session;
	/**
	 * The Maven PluginManager object
	 *
	 * @component
	 * @required
	 */
	private PluginManager pluginManager;

	/**
	 * Executes the standard war plugin after extracting the helper classes and libraries into the root directory of
	 * where the war is built. This means these classes and libraries will be included in the war file at its root
	 * level.
	 *
	 * @throws MojoExecutionException
	 */
	protected void extraSteps() throws MojoExecutionException {
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
										element(name("mainClass"), "org.apache.maven.executablewar.Main")
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

	@Override
	protected String correctPackaging() {
		return "executable-war";
	}
}