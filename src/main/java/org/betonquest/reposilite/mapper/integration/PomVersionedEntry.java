package org.betonquest.reposilite.mapper.integration;

import com.reposilite.storage.api.Location;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.betonquest.reposilite.mapper.settings.Artifact;

import java.util.Map;

/**
 * Represents a pom versioned entry in the maven repository.
 *
 * @param group       the group of the artifact
 * @param artifact    the versioned artifact
 * @param maven       the maven version
 * @param pom         the versions defined in the pom and extracted from the artifact by xpaths
 * @param jarLocation the location of the jar file related to the pom
 */
public record PomVersionedEntry(Artifact artifact, String group, String maven, Map<String, String> pom,
                                Location jarLocation) {

    /**
     * Checks if the group version is a snapshot version.
     *
     * @return true if the group version ends with "-SNAPSHOT", false otherwise
     */
    public boolean isSnapshot() {
        return group.endsWith("-SNAPSHOT");
    }

    /**
     * Checks if the given version is newer than the current group version
     * using {@link org.apache.maven.artifact.versioning.ArtifactVersion}.
     *
     * @param version the version to compare to
     * @return true if the given version is newer, false otherwise
     */
    public boolean isNewerThan(final String version) {
        final DefaultArtifactVersion foreignVersion = new DefaultArtifactVersion(version);
        final DefaultArtifactVersion localVersion = new DefaultArtifactVersion(maven);
        return localVersion.compareTo(foreignVersion) > 0;
    }
}
