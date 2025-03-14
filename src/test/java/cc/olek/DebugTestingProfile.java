package cc.olek;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class DebugTestingProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "quarkus.http.debug-enabled", "true",
            "quarkus.datasource.db-kind", "h2",
            "quarkus.datasource.username", "sa",
            "quarkus.datasource.password", "sa",
            "quarkus.hibernate-orm.database.generation", "drop-and-create",
            "quarkus.datasource.jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            "quarkus.hibernate-orm.second-level-caching.default-cache", "read-only"
        );
    }
}
