# LineGroup 多功能智慧助理

這是一個基於 Java Spring Boot 的多功能後端應用程式，主要作為一個與 LINE 平台深度整合的智慧助理。它不僅能處理來自 LINE 的訊息和事件，還內建了 AI 對話、地理位置服務、權限管理以及一個獨立的 React 前端管理後台。

## 主要功能

- **LINE Bot 核心整合**:
  - 接收並處理 LINE Webhook 事件 (如訊息、加入群組、關注等)。
  - 根據關鍵字或指令回覆不同類型的訊息。
  - 管理 LINE 使用者和群組資訊。

- **AI 智慧助理（未完成）**:
  - 整合大型語言模型 (LLM)，提供智慧對話功能。
  - 支援函式呼叫 (Function Calling)，能執行如查詢目前時間等外部操作。
  - 記錄 AI 對話歷史，方便追蹤與分析。

- **React 管理後台**:
  - 提供一個基於 React 的單頁應用程式 (SPA) 作為管理介面。
  - **使用者與權限管理**: 管理系統後台的使用者、角色和權限 (RBAC)。
  - **系統設定**: 動態設定和調整應用程式參數。
  - **LINE Notify 管理**: 設定和管理 LINE Notify 的權杖與推播。

- **Google Maps 地理位置服務**:
  - 整合 Google Maps API，提供地理資訊查詢等功能。
  - 實現了 `YummyQuest` 功能，一個簡單的美食搜尋或推薦的服務。

- **LINE Notify 推播服務**:
  - 支援 OAuth 2.0 流程，讓使用者可以綁定自己的 LINE 帳號以接收通知。
  - 提供 API 讓系統可以主動推播訊息給已綁定的使用者。

- **系統監控與安全**:
  - **API 請求日誌**: 透過 AOP (Aspect-Oriented Programming) 記錄所有 API 的請求與回應。
  - **設定加密**: 使用 Jasypt 加密 `application.yml` 中的敏感資訊 (如資料庫密碼、API 金鑰)。
  - **安全性**: 使用 Spring Security 進行身分驗證和授權。

## 技術棧

- **後端**:
  - **框架**: Spring Boot
  - **語言**: Java
  - **資料庫**: Spring Data JPA (推測使用 MySQL/PostgreSQL 等關聯式資料庫)
  - **快取**: Redis
  - **安全性**: Spring Security
  - **建置工具**: Maven
  - **API 文件**: Swagger (OpenAPI)

- **前端 (管理後台)**:
  - **框架**: React
  - **UI 函式庫**: Bootstrap
  - **JavaScript 加密**: JSEncrypt
  - **打包工具**: Webpack

- **AI**:
  - 整合外部大型語言模型服務 (例如 Google Gemini)。

- **其他**:
  - **設定加密**: Jasypt
  - **排程任務**: Spring Task Scheduler (例如定時清理圖片紀錄)

## 快速開始

### 環境需求

- Java (JDK) 17 或以上版本
- Maven 3.6+
- Node.js and npm (用於啟動 React 管理後台)
- Redis
- 一個關聯式資料庫 (例如 MySQL, PostgreSQL)

### 後端啟動

1.  **Clone 專案**:
    ```bash
    git clone <your-repository-url>
    cd LineGroup
    ```

2.  **設定 `application-dev.yml`**:
    - 複製 `application-dev.yml` 或建立新的設定檔。
    - 填寫您的資料庫連線資訊、Redis 位址。
    - 設定 LINE Channel Secret, Channel Access Token。
    - 設定 Google Maps API Key。
    - 設定 Jasypt 的加密金鑰 (通常是透過環境變數或 JVM 參數傳遞)，例如：
      ```bash
      export JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
      ```

3.  **執行應用程式**:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ```

### 前端 (管理後台) 啟動

1.  **進入 React 專案目錄**:
    ```bash
    cd src/main/resources/static/react-app
    ```

2.  **安裝依賴**:
    ```bash
    npm install
    ```

3.  **啟動開發伺服器**:
    ```bash
    npm start
    ```
    管理後台將會在 `http://localhost:3000` (或設定的埠號) 上運行。

## API 文件

本專案使用 Swagger 產生 API 文件。當應用程式啟動後，您可以造訪以下網址查看所有可用的 API 端點：

[http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

## 專案結構概覽

- `src/main/java/com/cheng/linegroup`: Java 原始碼主目錄。
  - `api`: 對外開放的 API 相關邏輯。
  - `controller`: Spring MVC 控制器。
  - `service`: 業務邏輯層。
  - `entity`: JPA 資料庫實體。
  - `repository`: Spring Data JPA Repository。
  - `config`: 各種設定類別 (Spring Security, Redis, AI, etc.)。
  - `events`: LINE 事件處理相關邏輯。
  - `ai`: AI 相關功能。
- `src/main/resources`: 資源檔目錄。
  - `application.yml`: Spring Boot 主要設定檔。
  - `static/react-app`: React 管理後台的前端專案。
  - `templates`: 伺服器端渲染的模板 (Thymeleaf)。
