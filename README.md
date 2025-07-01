# quarkus-training

根據[Quarkus 四週學習計畫](https://webglsoft.atlassian.net/wiki/spaces/TC/pages/368607247/Quarkus)的練習專案

## 第一週：基礎入門 & RESTful API 開發

### 開發兩隻 API
* GET /api/youbike/all：YouBike 即時 JSON 資料。
> ![week1-all.png](image/week1-all.png)
* GET /api/youbike/station/{id}：查詢指定站點資訊。
> ![week1-select.png](image/week1-select.png)

## 第二週：MongoDB 與 Redis 實務

### 開發三隻 API
* POST /api/youbike/import：一次匯入多筆站點資料進 MongoDB。
> ![week2-import.png](image/week2-import.png)
> ![week2-import2.png](image/week2-import2.png)
* GET /api/youbike/all：從 MongoDB 撈取資料。
> ![week2-all.png](image/week2-all.png)
* GET /api/youbike/station/{id}：自動切換快取與DB來源。
> ![week2-select.png](image/week2-select.png)
> ![week2-select2.png](image/week2-select2.png)
> ![week2-select3.png](image/week2-select3.png)

## 第三週：MySQL（SQL資料庫）CRUD 與 API驗證

### 開發五隻 API
* POST /api/users/register：新增用戶（密碼記得雜湊加密）。 
> ![week3-register.png](image/week3-register.png)
* POST /api/users/login：檢查帳密，成功則回傳 JWT Token。 
> ![week3-login.png](image/week3-login.png)
* GET /api/users/me：驗證 JWT，取得目前用戶資訊。
> ![week3-me.png](image/week3-me.png)
* PUT /api/users/{id}：更新用戶資料（需驗證 JWT）。 
> ![week3-update.png](image/week3-update.png)
* DELETE /api/users/{id}：刪除用戶（需驗證 JWT）。
> ![week3-delete.png](image/week3-delete.png)
* Token 無效
> ![week3-invalid-token.png](image/week3-invalid-token.png)
### 實作 Swagger 自動產生API文件。
> [/q/swagger-ui/](http://localhost:8080/q/swagger-ui/)
> ![week3-swagger.png](image/week3-swagger.png)

## 第四週：Kafka 訊息系統、整合多種資料來源

### Kafka in and out
> ![week4-kafka-in-out.png](image/week4-kafka-in-out.png)

### 開發一隻 API
* GET /api/youbike/changes/recent：回傳最近 10 筆異動記錄（讀 Redis，無則查 MySQL）。
> ![week4-changes.png](image/week4-changes.png)