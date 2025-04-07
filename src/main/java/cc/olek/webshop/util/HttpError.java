package cc.olek.webshop.util;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.Response;

import java.util.Map;

public class HttpError {
    public static Response json(int code, String message) {
        return Response.status(code)
            .entity(new JsonObject(Map.of("success", false, "error-message", message)))
            .build();
    }
}
