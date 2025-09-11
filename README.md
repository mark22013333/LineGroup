# LineGroup 多功能智慧助理

這是一個基於 **Spring Boot 3.4.5** 和 **Java 17** 的企業級多功能後端應用程式，主要作為一個與 LINE 平台深度整合的智慧助理。專案採用前後端分離架構，整合了 AI 對話、地理位置服務、圖片處理、權限管理以及一個完整的 React 管理後台。

## 🚀 主要功能

### 🤖 LINE Bot 核心整合
- 完整的 LINE Webhook 事件處理系統 (訊息、加入群組、關注等)
- 智慧關鍵字回覆機制與自訂回覆行為
- LINE 使用者與群組資訊管理
- 支援多種訊息類型 (文字、圖片、位置等)

### 🧠 AI 智慧助理
- 整合 **Spring AI** 框架與 **OpenAI** API
- 支援智慧對話與上下文理解
- 函式呼叫 (Function Calling) 功能，可執行外部操作
- 完整的 AI 對話歷史記錄與分析

### 💻 React 管理後台
- 基於 **React + Webpack** 的現代化單頁應用程式 (SPA)
- **RBAC 權限管理系統**: 完整的使用者、角色和權限管理
- **系統設定中心**: 動態配置應用程式參數
- **LINE Notify 管理**: 權杖管理與推播設定
- **API 測試工具**: 內建 API 測試介面

### 🗺️ Google Maps 地理位置服務
- 整合 **Google Maps API**，提供豐富的地理資訊查詢
- **YummyQuest** 美食探索功能，智慧推薦附近餐廳
- 位置資訊處理與地圖視覺化

### 📱 LINE Notify 推播服務
- 完整的 **OAuth 2.0** 授權流程
- 使用者 LINE 帳號綁定與管理
- 支援主動推播訊息與通知服務

### 🖼️ 圖片處理系統
- 使用 **Thumbnailator** 進行圖片縮放與最佳化
- **JHLabs Filters** 提供進階圖片濾鏡效果
- 圖片上傳、處理與管理功能

### 🔒 系統監控與安全
- **AOP 日誌系統**: 全面記錄 API 請求與回應
- **Jasypt 加密**: 敏感配置資訊加密保護
- **Spring Security**: JWT 認證與授權機制
- **多環境配置**: 支援 local/dev/prod 環境切換

## 🛠️ 技術棧

### 後端技術
- **核心框架**: Spring Boot 3.4.5
- **程式語言**: Java 17
- **資料庫**: MySQL 8.3.0 + Spring Data JPA
- **快取系統**: Redis + Spring Cache
- **安全框架**: Spring Security + JWT (Auth0)
- **建置工具**: Maven 3.6+
- **API 文件**: Swagger/OpenAPI 3 (SpringDoc)
- **HTTP 客戶端**: OkHttp 4.12.0

### 前端技術 (管理後台)
- **核心框架**: React 18
- **模組打包**: Webpack 5
- **轉譯工具**: Babel
- **UI 樣式**: 自訂 CSS + Bootstrap
- **開發伺服器**: Webpack Dev Server

### AI 與機器學習
- **AI 框架**: Spring AI 1.0.0-M3
- **語言模型**: OpenAI GPT API
- **對話管理**: 自訂對話上下文處理

### 資料處理與工具
- **圖片處理**: Thumbnailator 0.4.20 + JHLabs Filters
- **資料格式**: Jackson (JSON), Apache Commons CSV
- **加密工具**: Jasypt 3.0.5
- **工具函式庫**: Google Guava, Apache Commons
- **日誌框架**: Logback + SLF4J

### 外部服務整合
- **LINE Platform**: LINE Bot SDK
- **Google Services**: Google Maps API
- **推播服務**: LINE Notify OAuth 2.0

## 🚀 快速開始

### 📋 環境需求

- **Java**: JDK 17 或以上版本
- **Maven**: 3.6+ 版本
- **Node.js**: 16+ 版本 (用於 React 管理後台)
- **Redis**: 6.0+ 版本
- **MySQL**: 8.0+ 版本
- **作業系統**: Windows/macOS/Linux

### 🔧 後端啟動

1. **Clone 專案**:
   ```bash
   git clone <your-repository-url>
   cd LineGroup
   ```

2. **環境配置**:
   - 複製並修改對應環境的配置檔案：
     - `application-local.yml` (本地開發)
     - `application-dev.yml` (開發環境)
     - `application-prod.yml` (生產環境)
   
3. **設定必要參數**:
   ```yaml
   # 資料庫連線
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/linegroup
       username: your-username
       password: ENC(encrypted-password)
   
   # Redis 配置
   spring:
     data:
       redis:
         host: localhost
         port: 6379
   
   # LINE Bot 配置
   line:
     bot:
       channel-secret: your-channel-secret
       channel-token: your-channel-token
   
   # Google Maps API
   google:
     maps:
       api-key: ENC(encrypted-api-key)
   ```

4. **設定 Jasypt 加密金鑰**:
   ```bash
   export JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
   # 或使用 JVM 參數
   -Djasypt.encryptor.password=your-secret-key
   ```

5. **執行應用程式**:
   ```bash
   # 本地環境
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   
   # 開發環境
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   
   # 或直接執行 JAR
   mvn clean package
   java -jar target/LineGroup.war --spring.profiles.active=local
   ```

### 💻 前端 (管理後台) 啟動

1. **進入 React 專案目錄**:
   ```bash
   cd src/main/resources/static/react-app
   ```

2. **安裝依賴**:
   ```bash
   npm install
   ```

3. **啟動開發伺服器**:
   ```bash
   # 開發模式
   npm start
   
   # 或使用 Webpack 開發伺服器
   npx webpack serve --config webpack.dev.js
   ```
   
4. **建置生產版本**:
   ```bash
   npm run build
   ```

管理後台將在 `http://localhost:3000` 運行，並自動代理 API 請求到後端服務。

## 📚 API 文件

本專案使用 **SpringDoc OpenAPI 3** 自動產生 API 文件。應用程式啟動後，可透過以下網址存取：

- **Swagger UI**: [http://localhost:8080/apps/swagger-ui.html](http://localhost:8080/apps/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/apps/v3/api-docs](http://localhost:8080/apps/v3/api-docs)

> 💡 **提示**: 專案使用 `/apps` 作為 context path，請注意 URL 路徑。

## 🏗️ 專案結構概覽

```
LineGroup/
├── src/main/java/com/cheng/linegroup/
│   ├── LineGroupApplication.java          # 應用程式主入口
│   ├── ServletInitializer.java           # WAR 部署初始化
│   ├── annotation/                       # 自訂註解
│   ├── api/                             # 對外 API 介面層
│   ├── aspect/                          # AOP 切面程式 (日誌記錄)
│   ├── common/                          # 通用工具與常數
│   ├── config/                          # 配置類別
│   │   ├── SecurityConfig.java          # Spring Security 配置
│   │   ├── RedisConfig.java            # Redis 配置
│   │   ├── AiConfig.java               # AI 服務配置
│   │   ├── JasyptConfig.java           # 加密配置
│   │   └── SwaggerConfig.java          # API 文件配置
│   ├── controller/                      # REST API 控制器
│   │   ├── AuthController.java         # 認證相關 API
│   │   ├── LineController.java         # LINE Bot Webhook
│   │   ├── GoogleMapsController.java   # 地圖服務 API
│   │   ├── AiController.java           # AI 對話 API
│   │   └── SystemSettingController.java # 系統設定 API
│   ├── dao/                            # 資料存取層
│   ├── dto/                            # 資料傳輸物件
│   ├── entity/                         # JPA 實體類別
│   ├── enums/                          # 列舉定義
│   ├── events/                         # LINE 事件處理
│   │   ├── handler/                    # 各類事件處理器
│   │   └── message/                    # 訊息處理邏輯
│   ├── exception/                      # 例外處理
│   ├── filter/                         # HTTP 過濾器
│   ├── repository/                     # Spring Data JPA Repository
│   ├── services/                       # 業務邏輯服務層
│   │   ├── impl/                       # 服務實作
│   │   ├── ai/                         # AI 相關服務
│   │   └── security/                   # 安全相關服務
│   └── utils/                          # 工具類別
├── src/main/resources/
│   ├── application.yml                  # 主配置檔案
│   ├── application-local.yml           # 本地環境配置
│   ├── application-dev.yml             # 開發環境配置
│   ├── application-prod.yml            # 生產環境配置
│   ├── line-config-*.yml              # LINE Bot 專用配置
│   ├── logback-spring.xml              # 日誌配置
│   ├── static/                         # 靜態資源
│   │   ├── css/                        # 樣式檔案
│   │   ├── js/                         # JavaScript 檔案
│   │   └── react-app/                  # React 管理後台
│   │       ├── src/                    # React 原始碼
│   │       ├── public/                 # 公共資源
│   │       ├── package.json            # Node.js 依賴
│   │       ├── webpack.config.js       # Webpack 配置
│   │       └── webpack.dev.js          # 開發環境 Webpack
│   ├── templates/                      # Thymeleaf 模板
│   └── mock/                           # 模擬資料
├── pom.xml                             # Maven 專案配置
├── README.md                           # 專案說明文件
└── LICENSE                             # 授權條款
```

## 🔧 核心功能模組說明

### 🎯 事件處理系統 (`events/`)
- **EventHandler**: 統一事件處理介面
- **EventHandlerRegistry**: 事件處理器註冊中心
- **handler/**: 各類 LINE 事件處理器 (訊息、加入群組、關注等)
- **message/**: 訊息類型處理與回覆邏輯

### 🤖 AI 對話系統 (`services/ai/`)
- 整合 Spring AI 框架與 OpenAI API
- 支援上下文對話管理
- 函式呼叫 (Function Calling) 功能
- 對話歷史記錄與分析

### 🔐 權限管理系統 (`services/security/`)
- RBAC (角色基礎存取控制) 實作
- JWT Token 認證與授權
- 使用者、角色、權限三層架構
- 細粒度 API 權限控制

### 📍 地理位置服務 (`GoogleMapsController`)
- Google Maps API 整合
- YummyQuest 美食探索功能
- 位置資訊處理與視覺化
- 地點搜尋與推薦算法

### 🖼️ 圖片處理系統 (`utils/`)
- 圖片上傳、縮放、壓縮
- 多種濾鏡效果處理
- 圖片格式轉換
- 批次處理功能

## 🚀 部署與維運

### 📦 打包部署
```bash
# 建置 WAR 檔案
mvn clean package -Dmaven.test.skip=true

# 部署到 Tomcat
cp target/LineGroup.war $TOMCAT_HOME/webapps/

# 或直接執行
java -jar target/LineGroup.war --spring.profiles.active=prod
```

### 🔍 監控與日誌
- **應用程式日誌**: `logs/` 目錄下的日誌檔案
- **API 請求日誌**: 透過 AOP 記錄所有 API 呼叫
- **系統監控**: 可整合 Spring Boot Actuator
- **效能監控**: 支援 JVM 監控與分析

### 🔒 安全性考量
- 所有敏感配置使用 Jasypt 加密
- API 端點採用 JWT 認證
- 支援 CORS 跨域請求控制
- SQL 注入防護 (JPA 參數化查詢)
- XSS 攻擊防護

## 🤝 貢獻指南

1. **Fork** 此專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 **Pull Request**

## 📄 授權條款

此專案採用 [MIT License](LICENSE) 授權條款。

## 📞 技術支援

如有任何問題或建議，歡迎透過以下方式聯繫：

- 📧 **Email**: [your-email@example.com]
- 🐛 **Issue Tracker**: [GitHub Issues](https://github.com/your-username/LineGroup/issues)
- 📖 **Wiki**: [專案 Wiki](https://github.com/your-username/LineGroup/wiki)

---

⭐ 如果這個專案對你有幫助，請給個 Star 支持一下！