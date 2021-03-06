package io.fabric8.maven.docker;/*
 * 
 * Copyright 2015 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

import io.fabric8.maven.docker.config.BuildImageConfiguration;
import io.fabric8.maven.docker.util.MojoParameters;
import io.fabric8.maven.docker.access.DockerAccessException;
import io.fabric8.maven.docker.config.ImageConfiguration;
import io.fabric8.maven.docker.service.ServiceHub;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Mojo for attaching one more source docker tar file to an artifact.
 *
 * @author roland
 * @since 25/10/15
 */
@Mojo(name = "source", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES)
public class SourceMojo extends  AbstractBuildSupportMojo {

    @Component
    private MavenProjectHelper projectHelper;

    @Override
    protected void executeInternal(ServiceHub hub) throws DockerAccessException, MojoExecutionException {
        MojoParameters params = createMojoParameters();
        for (ImageConfiguration imageConfig : getImages()) {
            BuildImageConfiguration buildConfig = imageConfig.getBuildConfiguration();
            if (buildConfig != null) {
                if (buildConfig.skip()) {
                    log.info(imageConfig.getDescription() + ": Skipped creating source");
                } else {
                    File dockerTar =
                            hub.getArchiveService().createDockerBuildArchive(imageConfig, params);
                    String alias = imageConfig.getAlias();
                    if (alias == null) {
                        throw new IllegalArgumentException(
                                "Image " + imageConfig.getDescription() + " must have an 'alias' configured to be " +
                                "used as a classifier for attaching a docker build tar as source to the maven build");
                    }
                    projectHelper.attachArtifact(project, buildConfig.getCompression().getFileSuffix(),"docker-" + alias, dockerTar);
                }
            }
        }
    }

    @Override
    protected boolean isDockerAccessRequired() {
        // dont need a running docker host for creating the docker tar
        return false;
    }
}
