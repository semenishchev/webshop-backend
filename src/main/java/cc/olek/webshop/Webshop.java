package cc.olek.webshop;

import cc.olek.webshop.service.TwoFactorService;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.io.Console;

@ApplicationScoped
public class Webshop {
    public static final ObjectMapper JSON = new ObjectMapper(new JsonFactory());

    @Inject
    UserService userService;

    @Inject
    TwoFactorService twoFactorService;

    void onStart(@Observes StartupEvent event) {
        if(userService.getTotalUsers() != 0) return;
        Console console = System.console();
        if(console == null) return;
        Log.info("Looks like you are starting application for the first time\nWould you like to create a new superuser?\nEnter 'n' to skip\n");
        String email = console.readLine("Superuser email: ");
        if(email == null || email.equalsIgnoreCase("n")) return;
        System.out.print("Superuser password: ");
        String password = new String(console.readPassword());
        User user = userService.createUser(email, password);
        user.setSuperuser(true);
        userService.saveUser(user);
        Log.info("Superuser created. Would you like to setup TOPT for the superuser (Recommended), y/n: ");
        if(!console.readLine().equals("y")) return;

    }
}
