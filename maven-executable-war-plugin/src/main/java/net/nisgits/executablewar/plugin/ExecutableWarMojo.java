package net.nisgits.executablewar.plugin;

/**
 * Build an executable WAR file.
 *
 * @author <a href="from.executable-war@nisgits.net">Stig Kleppe-Jørgensen</a>
 * @version $Id: $
 * TODO make a JDK v1.4 version too with that maven plugin
 * TODO need to check if we are in a war packaging and only run then...or?? What about other war-packaging variants. Have an argument to the plugin?
 * TODO improve name of goal
 *
 * @goal add-exec-resources
 * @phase process-resources
 */
public class ExecutableWarMojo extends ExecutableWarMojoCommon {

	protected String correctPackaging() {
		return "war";
	}
}
