package org.apache.maven.plugin.executablewar;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.war.WarMojo;

/**
 * Build an executable WAR file.
 *
 * @author <a href="from.executable-war@apache.org">Stig Kleppe-J�rgensen</a>
 * @version $Id: $
 * @goal executable-war
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class ExecutableWarMojo extends WarMojo {
	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("RUNNING");
		super.execute();
	}
}
