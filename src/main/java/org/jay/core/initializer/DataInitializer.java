package org.jay.core.initializer;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jay.user.repository.UserRepository;
import org.jboss.logging.Logger;

@ApplicationScoped // 讓它成為一個 CDI Bean
public class DataInitializer {

    private static final Logger LOG = Logger.getLogger(DataInitializer.class);

    @Inject
    UserRepository userRepository; // 注入您的 UserRepository

    /**
     * 這個方法會在 Quarkus 應用程式啟動時被呼叫
     * @param event 啟動事件物件
     */
    @Transactional // 因為我們要寫入資料庫，所以需要交易
    void onStart(@Observes StartupEvent event) {
        LOG.info("應用程式啟動，開始檢查預設資料...");

        // 1. 檢查管理者帳號是否已存在，避免重複建立
        if (userRepository.findByIsAdmin() == null) {
            LOG.info("未找到預設管理者 'admin'，開始建立...");
            // 使用您在 UserRepository 中的 add 方法來建立使用者
            // 這個方法已經包含了密碼加密的邏輯
            userRepository.add("admin", "admin123", "admin@example.com", true);
            LOG.info("預設管理者 'admin' 建立成功。");
        } else {
            LOG.info("預設管理者 'admin' 已存在，無需建立。");
        }
    }
}
