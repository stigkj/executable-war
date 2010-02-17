package org.apache.maven.plugin.executablewar;

import java.io.File;

import com.google.common.collect.Lists;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;

/**
 * Unit test of {@link ExecutableWarMojo}
 *
 * @author Stig Kleppe-Jorgensen, 2010.02.16
 */
public class ExecutableWarMojoTest extends AbstractMojoTestCase {
	// TODO disabled for now; easier to just use integration tests 
	public void test_mojo_is_loaded() throws Exception {
		final String pathToConfig = getClass().getResource("/empty-mojo.xml").getPath();

		ExecutableWarMojo mojo = (ExecutableWarMojo) lookupMojo("war", new File(pathToConfig));
		assertNotNull(mojo);

		this.setVariableValueToObject(mojo, "pluginArtifacts", Lists.newArrayList(createArtifact()));
//		mojo.execute();
	}

	private ArtifactStub createArtifact() {
		final ArtifactStub artifact = new ArtifactStub();
		artifact.setArtifactId("maven-executable-war-library");
		artifact.setGroupId("org.apache.maven.plugins");
		
		return artifact;
	}
}
