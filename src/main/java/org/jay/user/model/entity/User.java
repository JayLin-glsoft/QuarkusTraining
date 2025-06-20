package org.jay.user.model.entity;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Tolerate;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor // Panache 需要一個無參數的建構子
public class User extends PanacheEntity {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean isAdmin = false;

    private Instant createdTime;

    // *** 這是關鍵：建立一個帶有 Builder 的客製化建構子 ***
    @Builder
    public User(String username, String rawPassword, String email, boolean isAdmin) {
        this.username = username;
        this.password = BcryptUtil.bcryptHash(rawPassword); // 在建構時就雜湊
        this.email = email;
        this.isAdmin = isAdmin;
        this.createdTime = Instant.now();
    }

    @PrePersist // 在儲存到資料庫前執行
    protected void onCreate() {
        createdTime = Instant.now();
    }

    /**
     * 檢查提供的密碼是否與儲存的雜湊密碼相符
     */
    public boolean checkPassword(String rawPassword) {
        return BcryptUtil.matches(rawPassword, this.password);
    }

    /**
     * 將原始密碼轉換為 bcrypt 雜湊並儲存
     * @param rawPassword 原始密碼
     */
    public void setPassword(String rawPassword) {
        this.password = BcryptUtil.bcryptHash(rawPassword);
    }
}



