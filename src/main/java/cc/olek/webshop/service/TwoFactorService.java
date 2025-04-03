package cc.olek.webshop.service;

import cc.olek.webshop.user.User;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TwoFactorService {
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA256, 6);
    private final CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator(128);
    private final RecoveryCodeGenerator recoveryCodeGenerator = new RecoveryCodeGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    public boolean isValid(User user, String code) {
        String secret = user.getTwoFactorSecret();
        if(secret == null) return false;
        return verifier.isValidCode(secret, code);
    }

    public boolean isValidRaw(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }

    private String generateNewSecret() {
        return secretGenerator.generate();
    }

    private QrData createQr(User user, String secret) {
        if(user.getTwoFactorSecret() != null) {
            // looks like abuse
            return null;
        }
        return new QrData.Builder()
            .secret(secret)
            .algorithm(HashingAlgorithm.SHA256)
            .issuer("Olek:)")
            .label(user.getEmail())
            .digits(6)
            .period(30)
            .build();
    }

    public InitiationData createInitiationData(User user) throws QrGenerationException {
        String secret = generateNewSecret();
        QrData qr = createQr(user, secret);

        String qrData = Utils.getDataUriForImage(qrGenerator.generate(qr), qrGenerator.getImageMimeType());
        return new InitiationData(secret, qrData);
    }

    public List<String> generateRecoveryCodes() {
        return List.of(recoveryCodeGenerator.generateCodes(16));
    }

    public record InitiationData(String secret, String qrCode) {

    }
}
