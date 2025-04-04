package cc.olek.webshop;

import cc.olek.webshop.auth.AuthenticationService;
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
    AuthenticationService authenticationService;

    @Inject
    TwoFactorService twoFactorService;

    void onStart(@Observes StartupEvent event) {
        if(!System.getenv().containsKey("DO_SETUP")) return;
        Console console = System.console();
        if(console == null) return;
        checker: {
            Log.info("Entered setup mode\nWould you like to create a new superuser?\nEnter 'n' to skip\n");
            String email = console.readLine("Superuser email: ");
            if(email == null || !email.equalsIgnoreCase("y")) break checker;
            String password;
            while(true) {
                System.out.print("Superuser password: ");
                password = new String(console.readPassword());
                System.out.print("Superuser confirm password: ");
                String confirmPassword = new String(console.readPassword());
                if(!password.equals(confirmPassword)) {
                    Log.info("Passwords do not match");
                    String response = console.readLine("Try again? (y/n): ");
                    if(!response.equalsIgnoreCase("y")) break checker;
                    continue;
                }
                break;
            }
            User user = userService.createUser(email, password);
            user.setSuperuser(true);
            userService.saveUser(user);
            Log.info("(Recommended) Superuser created. Would you like to setup 2FA for the superuser? (y/n): ");
            if(!console.readLine().equals("y")) break checker;
            TwoFactorService.InitiationData data = authenticationService.initiateTwoFactorAuthentication(user);
            Log.infof("2FA secret. Enter it into your 2FA app: %s", data.secret());
            while(true) {
                String input = console.readLine("Enter 2FA code which is showing up: ");
                if(!twoFactorService.isValidRaw(data.secret(), input)) {
                    input = console.readLine("Code doesn't match. Try again? (y/n): ");
                    if(!input.equalsIgnoreCase("y")) break;
                    continue;
                }
                user.setTwoFactorSecret(data.secret());
                userService.saveUser(user);
                break;
            }
        }
        Log.info("Done setup");
    }
}
