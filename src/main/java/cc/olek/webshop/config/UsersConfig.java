package cc.olek.webshop.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "usersystem")
public interface UsersConfig {
    @WithDefault("3")
    int maxSessionsPerIp();
    @WithDefault("5")
    int maxSessionsPerUser();
}
