package cc.olek.webshop.service;

import cc.olek.webshop.email.ConfirmRegistrationEmail;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailService {
    @Inject
    Mailer mailer;
    @ConfigProperty(name = "email.domain", defaultValue = "olek.cc")
    String domain;

    public void sendEmailVerification(String to, String link) {
        new ConfirmRegistrationEmail(link)
            .to(to)
            .from("noreply@" + domain)
            .subject("Verify your E-Mail")
            .sendAndAwait();
    }
}
