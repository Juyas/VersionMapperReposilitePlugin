package org.betonquest.reposilite.mapper.restful;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.reposilite.maven.MavenFacade;
import com.reposilite.maven.infrastructure.MavenRoutes;
import com.reposilite.shared.ContextDsl;
import com.reposilite.web.api.ReposiliteRoute;
import io.javalin.community.routing.Route;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import kotlin.Unit;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.betonquest.reposilite.mapper.integration.ArtifactsVersionsCache;
import org.betonquest.reposilite.mapper.integration.PomMapperFacade;
import org.betonquest.reposilite.mapper.integration.PomVersionedEntry;
import org.betonquest.reposilite.mapper.settings.Artifact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Contains and handles all routes for the Restful API.
 */
@SuppressWarnings({"MissingJavadoc", "PMD.CommentRequired", "PMD.ShortVariable"})
public class RestfulRoutes extends MavenRoutes implements RestfulDefinitions {

    private final Gson gson = new GsonBuilder().create();

    private final PomMapperFacade baseFacade;

    @OpenApi(
            path = SERVICE_ID_PATH,
            methods = HttpMethod.GET,
            tags = "PomMapper",
            summary = "Returns all versions with their downloadable jars by internal id.",
            description = "The internal id as defined in the reposilite configuration section.",
            pathParams = @OpenApiParam(name = "id", description = "The internal id of the artifact as defined in configuration.", required = true, example = "MyCoolArtifact"),
            queryParams = {
                    @OpenApiParam(name = SERVICE_ID_QPARAM_NAME_SNAPSHOT, description = "Whether snapshot versions are listed." + SERVICE_ID_QPARAM_DEFAULT_SNAPSHOT + " by default.", example = "false", type = Boolean.class),
                    @OpenApiParam(name = SERVICE_ID_QPARAM_NAME_RELEASE, description = "Whether release versions are listed. " + SERVICE_ID_QPARAM_DEFAULT_RELEASE + " by default.", example = "false", type = Boolean.class),
                    @OpenApiParam(name = SERVICE_ID_QPARAM_NAME_LIMIT_VERSIONS, description = "The maximum amount of elements per group to return. " + SERVICE_ID_QPARAM_DEFAULT_LIMIT_VERSIONS + " by default.", example = "10", type = Integer.class),
                    @OpenApiParam(name = SERVICE_ID_QPARAM_NAME_SINCE, description = "Only return versions newer than the given version.", example = "1.2.3")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Valid result containing a list of all mapped versions with their jar paths", content = @OpenApiContent(from = String.class, type = ContentType.JSON)),
                    @OpenApiResponse(status = "204 ", description = "Valid result containing no entries"),
                    @OpenApiResponse(status = "404", description = "Internal id not found")
            }
    )
    private final ReposiliteRoute<Void> serviceAccess = new ReposiliteRoute<>(SERVICE_ID_PATH_REPOSILITE, new Route[]{Route.HEAD, Route.GET}, context -> {
        serviceAccessHandler(context);
        return Unit.INSTANCE;
    });

    @OpenApi(
            path = SERVICE_REPOSITORY_PATH,
            methods = HttpMethod.GET,
            tags = "PomMapper",
            summary = "Returns all versions with their downloadable jars by their gav.",
            description = "Not using the accessor can cause delay if the versions have not been cached.",
            pathParams = {
                    @OpenApiParam(name = "repository", description = "Destination repository", required = true),
                    @OpenApiParam(name = "gav", description = "Artifact path qualifier", required = true, allowEmptyValue = true)
            },
            queryParams = {
                    @OpenApiParam(name = SERVICE_REPOSITORY_QPARAM_NAME_SNAPSHOT, description = "Whether snapshot versions are listed." + SERVICE_REPOSITORY_QPARAM_DEFAULT_SNAPSHOT + " by default.", example = "false", type = Boolean.class),
                    @OpenApiParam(name = SERVICE_REPOSITORY_QPARAM_NAME_RELEASE, description = "Whether release versions are listed. " + SERVICE_REPOSITORY_QPARAM_DEFAULT_RELEASE + " by default.", example = "false", type = Boolean.class),
                    @OpenApiParam(name = SERVICE_REPOSITORY_QPARAM_NAME_LIMIT_VERSIONS, description = "The maximum amount of elements per group to return. " + SERVICE_REPOSITORY_QPARAM_DEFAULT_LIMIT_VERSIONS + " by default.", example = "10", type = Integer.class),
                    @OpenApiParam(name = SERVICE_REPOSITORY_QPARAM_NAME_SINCE, description = "Only return versions newer than the given version.", example = "1.2.3")
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Valid result containing a list of all mapped versions with their jar paths", content = @OpenApiContent(from = String.class, type = ContentType.JSON)),
                    @OpenApiResponse(status = "204 ", description = "Valid result containing no entries"),
                    @OpenApiResponse(status = "404", description = "Target not found")
            }
    )
    private final ReposiliteRoute<Void> serviceDirect = new ReposiliteRoute<>(SERVICE_REPOSITORY_PATH_REPOSILITE, new Route[]{Route.HEAD, Route.GET}, context -> {
        serviceDirectHandler(context);
        return Unit.INSTANCE;
    });

    /**
     * Default Constructor.
     *
     * @param mavenFacade the maven facade to use
     * @param baseFacade  the PomMapperFacade to use
     */
    public RestfulRoutes(final MavenFacade mavenFacade, final PomMapperFacade baseFacade) {
        super(mavenFacade);
        this.baseFacade = baseFacade;
    }

    private void debug(final String message) {
        baseFacade.getPlugin().debug("RestAPI > " + message);
    }

    private void serviceDirectHandler(final ContextDsl<Void> context) {
        context.accessed(token -> {
            requireGav(context, gav -> {
                final String repository = context.requireParameter("repository");
                final Context ctx = context.getCtx();
                final Artifact artifact = baseFacade.findArtifact(repository, gav);
                if (artifact == null) {
                    ctx.status(HttpStatus.NOT_FOUND);
                    debug("Artifact not found for gav \"" + gav + "\" in repository \"" + repository + "\".");
                    return Unit.INSTANCE;
                }
                debug("Trying to redirect to accessor for gav \"" + gav + "\" in repository \"" + repository + "\".");
                String query = ctx.queryString();
                if (query == null || query.isBlank()) {
                    query = "";
                } else {
                    query = "?" + query;
                }
                ctx.redirect(SERVICE_ID_PATH.replace("{id}", artifact.id()) + query, HttpStatus.TEMPORARY_REDIRECT);
                return Unit.INSTANCE;
            });
            return null;
        });
    }

    private void serviceAccessHandler(final ContextDsl<Void> context) {
        context.accessed(token -> {
            final Context ctx = context.getCtx();
            final String id = context.requireParameter("id");
            final ArtifactsVersionsCache artifactsVersionsCache = baseFacade.getArtifactsVersionsCache();

            if (!artifactsVersionsCache.hasEntry(id)) {
                ctx.status(HttpStatus.NOT_FOUND);
                debug("Artifact not found for id \"" + id + "\"");
                return null;
            }

            final List<PomVersionedEntry> entries = artifactsVersionsCache.getVersions(id);
            if (entries.isEmpty()) {
                ctx.status(HttpStatus.NO_CONTENT).result("No entries found.");
                debug("No entries found for id \"" + id + "\"");
                return null;
            }

            final boolean considerSnapshots = readOptionalQuery(ctx, SERVICE_ID_QPARAM_NAME_SNAPSHOT, Boolean.class, SERVICE_ID_QPARAM_DEFAULT_SNAPSHOT);
            final boolean considerReleases = readOptionalQuery(ctx, SERVICE_ID_QPARAM_NAME_RELEASE, Boolean.class, SERVICE_ID_QPARAM_DEFAULT_RELEASE);
            final int limit = readOptionalQuery(ctx, SERVICE_ID_QPARAM_NAME_LIMIT_VERSIONS, Integer.class, SERVICE_ID_QPARAM_DEFAULT_LIMIT_VERSIONS);
            final String since = readOptionalQuery(ctx, SERVICE_ID_QPARAM_NAME_SINCE, String.class, SERVICE_ID_QPARAM_DEFAULT_SINCE);

            debug("Found " + entries.size() + " entries for id \"" + id + "\"");
            debug("filter with: snapshots=\"" + considerSnapshots + "\", releases=\"" + considerReleases + "\", limit=\"" + limit + "\", since=\"" + since + "\"");

            final Predicate<PomVersionedEntry> filterTypes = version ->
                    considerSnapshots && version.isSnapshot() || considerReleases && !version.isSnapshot();
            final Predicate<PomVersionedEntry> filterSince = version -> version.isNewerThan(since);

            final JsonArray result = resolve(entries, filterTypes.and(filterSince), limit);

            ctx.status(HttpStatus.OK).result(gson.toJson(result));
            return null;
        });
    }

    private <T> T readOptionalQuery(final Context ctx, final String param, final Class<T> result, final T defaultValue) {
        return ctx.queryParamAsClass(param, result).getOrDefault(defaultValue);
    }

    private JsonArray resolve(final List<PomVersionedEntry> versions, final Predicate<PomVersionedEntry> queryParamFilter, final int limit) {
        final Map<String, List<Map.Entry<DefaultArtifactVersion, JsonObject>>> groups = new HashMap<>();
        extractGroups(versions, queryParamFilter).forEach(group -> groups.put(group, new ArrayList<>()));
        versions.stream().filter(queryParamFilter).forEach(version ->
                groups.get(version.group()).add(Map.entry(new DefaultArtifactVersion(version.maven()), buildPomEntries(version))));

        debug("Resolved " + groups.size() + " maven version groups.");

        final Comparator<Map.Entry<DefaultArtifactVersion, JsonObject>> comp = Map.Entry.comparingByKey();
        final List<Map.Entry<DefaultArtifactVersion, JsonObject>> objects = new ArrayList<>();
        groups.forEach((tag, entries) -> {
            final JsonObject group = new JsonObject();
            entries.sort(comp.reversed());
            if (limit > 0 && limit < entries.size()) {
                entries.subList(limit, entries.size()).clear();
            }
            group.addProperty(RESULT_JSON_KEY_GROUP, tag);
            group.add(RESULT_JSON_KEY_VERSIONS, entries.stream().map(Map.Entry::getValue).collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
            objects.add(Map.entry(new DefaultArtifactVersion(tag), group));
        });
        objects.sort(comp.reversed());

        final JsonArray parent = new JsonArray();
        objects.forEach(pair -> parent.add(pair.getValue()));
        return parent;
    }

    private List<String> extractGroups(final List<PomVersionedEntry> versions, final Predicate<PomVersionedEntry> queryParamFilter) {
        return versions.stream()
                .filter(queryParamFilter)
                .map(PomVersionedEntry::group)
                .toList();
    }

    private JsonObject buildPomEntries(final PomVersionedEntry entry) {
        final JsonObject parent = new JsonObject();
        final JsonObject pomVersions = new JsonObject();
        entry.pom().forEach(pomVersions::addProperty);
        parent.addProperty(RESULT_JSON_KEY_MVN_VERSION, entry.maven());
        parent.addProperty(RESULT_JSON_KEY_JAR_PATH, entry.jarLocation().toString());
        parent.add(RESULT_JSON_KEY_ENTRIES, pomVersions);
        return parent;
    }

    @Override
    public Set<ReposiliteRoute<?>> getRoutes() {
        return Set.of(serviceDirect, serviceAccess);
    }
}
