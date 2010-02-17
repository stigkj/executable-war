package org.apache.maven.plugin.executablewar;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * Unit test of {@link ExecutableWarMojo}
 *
 * @author Stig Kleppe-Jorgensen, 2010.02.16
 */
public class ExecutableWarMojoTest extends AbstractMojoTestCase {
	public void test_mojo_is_loaded() throws Exception {
		final File testPom = new File(getBasedir(), "src/test/resources/empty-mojo.xml");

		ExecutableWarMojo mojo = (ExecutableWarMojo) lookupMojo("war", testPom);

		assertNotNull(mojo);
	}
}
