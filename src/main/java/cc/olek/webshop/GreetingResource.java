package cc.olek.webshop;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/liveness")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String liveness() {
        return "Alive!";
    }
}
