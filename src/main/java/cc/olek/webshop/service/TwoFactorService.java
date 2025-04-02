package cc.olek.webshop.service;

import cc.olek.webshop.user.User;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TwoFactorService {
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA256, 6);
    private final CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator(128);
    private final RecoveryCodeGenerator recoveryCodeGenerator = new RecoveryCodeGenerator();
    public boolean isValid(User user, String code) {
        String secret = user.getTwoFactorSecret();
        if(secret == null) return false;
        return verifier.isValidCode(secret, code);
    }

    public String generateNewSecret() {
        return secretGenerator.generate();
    }

    public QrData createDataForUser(User user) {
        if(user.getTwoFactorSecret() != null) {
            // looks like abuse
            return null;
        }
        return new QrData.Builder()
            .secret(generateNewSecret())
            .algorithm(HashingAlgorithm.SHA256)
            .issuer("Olek:)")
            .label(user.getEmail())
            .digits(6)
            .period(30)
            .build();
    }

    public List<String> generateRecoveryCodes() {
        return List.of(recoveryCodeGenerator.generateCodes(16));
    }
}
