package cc.olek.webshop.util;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.Response;

import java.util.Map;

public class JResponse {
    public static Response json(int code, String message) {
        return Response.status(code)
            .entity(new JsonObject(Map.of("success", code >= 200 && code < 400, "message", message)))
            .build();
    }
}
