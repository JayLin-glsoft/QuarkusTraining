package org.jay.user.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jay.user.model.entity.User;

@ApplicationScoped // 讓這個 Repository 成為一個可被注入的 CDI Bean
public class UserRepository implements PanacheRepository<User> {

    // 將原本在 User class 的 findByUsername 邏輯搬到這裡
    public User findByUsername(String username) {
        return find("username", username).firstResult();
    }

    // 新增使用者的邏輯也可以移到這裡，讓 Resource 層更乾淨
    // 注意：這裡不再是 static 方法
    public User add(String username, String rawPassword, String email) {
        User user = new User();
        user.username = username;
        user.setPassword(rawPassword); // 假設 User class 有一個 setPassword 的方法會做 hash
        user.email = email;
        persist(user); // 使用 PanacheRepository 提供的 persist 方法
        return user;
    }
}
