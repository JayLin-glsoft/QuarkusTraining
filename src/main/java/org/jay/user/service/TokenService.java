package org.jay.user.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@ApplicationScoped
public class TokenService {

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer") // 從 application.yaml 注入 issuer
    String issuer;

    @Inject
    @ConfigProperty(name = "app.jwt.duration-in-seconds") // 注入自訂的過期時間
    long durationInSeconds;

    /**
     * 產生一個 JWT
     * @param username 使用者名稱
     * @param roles 使用者的角色 (此範例中我們簡化為 "user")
     * @return 產生的 JWT 字串
     */
    public String generateToken(String username, Set<String> roles) {

        return Jwt.issuer(issuer) // 簽發者，需與 application.yml 中設定的 mp.jwt.verify.issuer 相同
                .upn(username) // User Principal Name，通常是使用者名稱或 email
                .groups(roles) // 使用者的角色群組
                .expiresIn(durationInSeconds)
                .sign();
    }
}
