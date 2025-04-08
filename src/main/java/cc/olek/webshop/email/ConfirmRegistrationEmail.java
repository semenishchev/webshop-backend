package cc.olek.webshop.email;

import io.quarkus.mailer.MailTemplate;

public record ConfirmRegistrationEmail(String link) implements MailTemplate.MailTemplateInstance {
}
