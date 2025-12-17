package org.betonquest.reposilite.mapper.restful;

/**
 * Contains all paths and query parameter definitions for the restful API.
 */
@SuppressWarnings("PMD.ConstantsInInterface")
public interface RestfulDefinitions {

    /**
     * The root path of the restful API for all services.
     */
    String ROOT = "/api/pommapper/";

    // ------------------- Service: direct -------------------

    /**
     * The path of the direct repository service.
     */
    String SERVICE_REPOSITORY_PREFIXED = ROOT + "repo/";

    /**
     * The path of the direct repository service.
     */
    String SERVICE_REPOSITORY_PATH = SERVICE_REPOSITORY_PREFIXED + "{repository}/{gav}";

    /**
     * The path of the direct repository service with open api syntax.
     */
    String SERVICE_REPOSITORY_PATH_REPOSILITE = SERVICE_REPOSITORY_PREFIXED + "{repository}/<gav>";

    /**
     * The query parameter for snapshot versions of the direct repository service.
     */
    String SERVICE_REPOSITORY_QPARAM_NAME_SNAPSHOT = "snapshots";

    /**
     * The default value for snapshot versions of the direct repository service.
     */
    boolean SERVICE_REPOSITORY_QPARAM_DEFAULT_SNAPSHOT = true;

    /**
     * The query parameter for release versions of the direct repository service.
     */
    String SERVICE_REPOSITORY_QPARAM_NAME_RELEASE = "releases";

    /**
     * The default value for release versions of the direct repository service.
     */
    boolean SERVICE_REPOSITORY_QPARAM_DEFAULT_RELEASE = true;

    /**
     * The query parameter for the limit of the direct repository service.
     */
    String SERVICE_REPOSITORY_QPARAM_NAME_LIMIT_VERSIONS = "limit";

    /**
     * The default value for the limit of the direct repository service.
     */
    int SERVICE_REPOSITORY_QPARAM_DEFAULT_LIMIT_VERSIONS = -1;

    /**
     * The query parameter for the "since" of the id service.
     */
    String SERVICE_REPOSITORY_QPARAM_NAME_SINCE = "since";

    /**
     * The default value of the "since" query parameter for the id service.
     */
    String SERVICE_REPOSITORY_QPARAM_DEFAULT_SINCE = "0.0.1";

    // ------------------- Service: accessor -------------------

    /**
     * The path prefix of the id service.
     */
    String SERVICE_ID_PREFIXED = ROOT + "id/";

    /**
     * The full path of the id service.
     */
    String SERVICE_ID_PATH = SERVICE_ID_PREFIXED + "{id}";

    /**
     * The path of the id service with open api syntax.
     */
    String SERVICE_ID_PATH_REPOSILITE = SERVICE_ID_PATH;

    /**
     * The name of the snapshot query parameter for the id service.
     */
    String SERVICE_ID_QPARAM_NAME_SNAPSHOT = "snapshots";

    /**
     * The default value of the snapshot query parameter for the id service.
     */
    boolean SERVICE_ID_QPARAM_DEFAULT_SNAPSHOT = true;

    /**
     * The default value of the release query parameter for the id service.
     */
    String SERVICE_ID_QPARAM_NAME_RELEASE = "releases";

    /**
     * The default value of the release query parameter for the id service.
     */
    boolean SERVICE_ID_QPARAM_DEFAULT_RELEASE = true;

    /**
     * The query parameter for the limit of the id service.
     */
    String SERVICE_ID_QPARAM_NAME_LIMIT_VERSIONS = "limit";

    /**
     * The default value of the limit query parameter for the id service.
     */
    int SERVICE_ID_QPARAM_DEFAULT_LIMIT_VERSIONS = -1;

    /**
     * The query parameter for the "since" of the id service.
     */
    String SERVICE_ID_QPARAM_NAME_SINCE = "since";

    /**
     * The default value of the "since" query parameter for the id service.
     */
    String SERVICE_ID_QPARAM_DEFAULT_SINCE = "0.0.1";

    // ------------------- Rest API Results -------------------

    /**
     * The key for the maven version in the JSON result.
     */
    String RESULT_JSON_KEY_MVN_VERSION = "version";

    /**
     * The key for the artifact's version list in the JSON result.
     */
    String RESULT_JSON_KEY_VERSIONS = "versions";

    /**
     * The key for the artifact's jar path in the JSON result.
     */
    String RESULT_JSON_KEY_JAR_PATH = "jar";

    /**
     * The key for all entries filtered using xPaths from the artifact's pom.xml in the JSON result.
     */
    String RESULT_JSON_KEY_ENTRIES = "entries";

    /**
     * The key for the artifact's maven version group in the JSON result.
     */
    String RESULT_JSON_KEY_GROUP = "group";
}
