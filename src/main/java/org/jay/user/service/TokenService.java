package org.jay.user.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jay.user.model.entity.User;

import java.util.HashSet;
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
     * 根據使用者資訊產生一個 JWT
     * @param user 使用者實體物件
     * @return 產生的 JWT 字串
     */
    public String generateToken(User user) {
        Set<String> roles = new HashSet<>();
        roles.add("user"); // 所有使用者都至少有 'user' 角色

        // *** 如果是管理者，額外加入 'admin' 角色 ***
        if (user.isAdmin()) {
            roles.add("admin");
        }

        return Jwt.issuer(issuer) // 簽發者，需與 application.yml 中設定的 mp.jwt.verify.issuer 相同
                .upn(user.getEmail()) // User Principal Name，通常是使用者名稱或 email
                .groups(roles) // 使用者的角色群組
                .expiresIn(durationInSeconds)
                .sign();
    }
}
