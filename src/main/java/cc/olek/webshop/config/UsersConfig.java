package cc.olek.webshop.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "usersystem")
public interface UsersConfig {
    int maxSessionsPerIp();
    int maxSessionsPerUser();
}
