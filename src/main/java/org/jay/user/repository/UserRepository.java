package org.jay.user.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jay.user.model.entity.User;

@ApplicationScoped // 讓這個 Repository 成為一個可被注入的 CDI Bean
public class UserRepository implements PanacheRepository<User> {

    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public  User findByIsAdmin() {
        return find("isAdmin", true).firstResult();
    }

    public User add(String username, String rawPassword, String email, boolean isAdmin) {
        User user = User.builder()
                .username(username)
                .rawPassword(rawPassword)
                .email(email)
                .isAdmin(isAdmin)
                .build();
        persist(user); // 使用 PanacheRepository 提供的 persist 方法
        return user;
    }
}
