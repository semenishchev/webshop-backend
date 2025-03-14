package cc.olek.webshop.auth;

import java.lang.annotation.*;

/**
 * Represents an exact permission string to access an endpoint.
 * For example: profile.write.self, profile.read.any
 * Then, if a user or user's group has a permission of profile.*, it will match that permission
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {
    String value();
}
