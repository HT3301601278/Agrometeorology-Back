# 农业气象服务后端

## 1. 项目简介

本项目是一个基于 Spring Boot 构建的农业气象信息服务平台的后端系统。旨在为用户提供农田地块管理、实时天气信息、天气预报、历史气象数据查询、系统通知、公告发布等功能，帮助用户更好地进行农业生产决策。系统同时包含用户管理、权限控制和系统配置等后台管理功能。

## 2. 技术栈

*   **开发语言:** Java 11
*   **核心框架:** Spring Boot
*   **安全框架:** Spring Security (JWT for Authentication/Authorization)
*   **数据访问:** Spring Data JPA
*   **数据库:** MySQL
*   **构建工具:** Maven
*   **其他:** Lombok, Validation API, Java Mail Sender

## 3. 主要功能

### 3.1 用户认证与授权
*   **用户注册:** 提供用户名、密码、邮箱、昵称、手机号进行注册。
*   **用户登录:** 使用用户名和密码进行登录，成功后返回 JWT Token。
*   **密码重置:** 通过邮箱发送验证码，验证后允许重置密码。
*   **权限控制:** 基于角色的访问控制 (RBAC)，区分普通用户和管理员权限。

### 3.2 用户管理
*   **获取个人信息:** 查看当前登录用户的详细信息。
*   **更新个人信息:** 修改昵称、邮箱、手机号等。
*   **更新头像:** 上传并更换用户头像。
*   **修改密码:** 通过邮箱验证码后修改登录密码。

### 3.3 管理员功能
*   **用户列表:** 分页查看所有注册用户信息。
*   **用户状态管理:** 冻结或解冻指定用户账号。
*   **删除用户:** 删除指定用户账号。
*   **系统配置管理:**
    *   查看、新增、修改、删除系统配置项 (Key-Value 形式)。
    *   配置外部天气服务 API Key。
    *   配置天气数据自动拉取频率。
    *   配置邮件服务器信息 (Host, Port, Username, Password, Auth, StartTLS)。
*   **发送系统通知:** 向所有注册用户推送系统级别的通知消息。

### 3.4 农田地块管理
*   **创建地块:** 添加新的农田地块，关联用户，记录名称、位置（经纬度）、面积、所属分组等信息。
*   **更新地块:** 修改现有地块的信息。
*   **删除地块:** 移除不再需要的地块。
*   **查看地块:** 获取用户拥有的所有地块列表或指定地块的详细信息。
*   **按分组查看:** 获取特定地块分组下的所有地块。

### 3.5 地块分组管理
*   **创建分组:** 为用户创建地块分组，方便管理。
*   **更新分组:** 修改分组名称或描述。
*   **删除分组:** 移除地块分组（通常需要先处理组内关联的地块）。
*   **查看分组:** 获取用户的所有地块分组列表或指定分组的详细信息。

### 3.6 天气数据服务
*   **实时天气:** 根据指定地点（经纬度）获取当前天气状况。
*   **天气预报:** 获取指定地点未来几天的天气预报信息。
*   **历史天气:** 查询指定地点过去某时间段的历史气象数据。
    *   _(注意: 天气数据的获取依赖于外部 API 和相应的配置)_

### 3.7 通知中心
*   **获取通知:** 分页查看当前用户的个人通知和系统通知。
*   **未读通知:** 查看未读通知列表及数量。
*   **标记已读:** 将单条或所有通知标记为已读状态。
*   **删除通知:** 删除不再需要的通知。
*   **通知设置:** 用户可配置是否接收邮件通知、系统站内通知。

### 3.8 公告管理
*   **公共查看:** 所有用户可查看已发布的有效公告列表和详情。
*   **按类型查看:** 查看特定类型的有效公告。
*   **后台管理 (管理员):**
    *   创建、编辑、删除公告。
    *   设置公告类型、状态（草稿、已发布）、发布时间、过期时间。
    *   发布公告。
    *   分页查看所有公告，可按类型、状态筛选。

### 3.9 文件服务
*   **头像上传:** 提供用户头像上传接口，返回存储后的文件访问 URL。
    *   _(注意: 文件存储策略可配置为本地存储，配置文件中已设置上传路径和访问URL)_

## 4. API 端点

### 认证 (`/auth`)
*   `POST /register`: 用户注册
*   `POST /login`: 用户登录
*   `POST /password/forgot`: 忘记密码，验证身份并发送验证码
*   `POST /password/reset-request`: 请求密码重置验证码
*   `POST /password/reset`: 重置密码

### 用户 (`/users`)
*   `GET /me`: 获取当前用户信息
*   `PUT /me`: 更新当前用户信息
*   `PUT /me/avatar`: 更新用户头像 URL (配合 `/files/avatar` 上传)
*   `PUT /me/password`: 修改密码
*   `GET /{id}`: (Admin) 获取指定用户信息
*   `PUT /{id}/status`: (Admin) 切换用户状态 (冻结/解冻)
*   `DELETE /{id}`: (Admin) 删除用户

### 管理员 (`/admin`)
*   `GET /users`: (Admin) 获取所有用户 (分页)
*   `GET /configs`: (Admin) 获取所有系统配置 (分页)
*   `POST /configs`: (Admin) 保存/更新系统配置
*   `DELETE /configs/{key}`: (Admin) 删除系统配置
*   `GET /configs/api-key`: (Admin) 获取天气 API Key
*   `POST /configs/api-key`: (Admin) 保存天气 API Key
*   `GET /configs/fetch-interval`: (Admin) 获取数据拉取频率
*   `POST /configs/fetch-interval`: (Admin) 保存数据拉取频率
*   `GET /configs/email`: (Admin) 获取邮件配置
*   `POST /configs/email`: (Admin) 保存邮件配置
*   `POST /notifications`: (Admin) 发送系统通知给所有用户

### 地块 (`/fields`)
*   `POST /`: 创建地块
*   `PUT /{id}`: 更新地块
*   `DELETE /{id}`: 删除地块
*   `GET /`: 获取当前用户地块列表
*   `GET /group/{groupId}`: 获取指定分组下的地块列表
*   `GET /{id}`: 获取地块详情

### 地块分组 (`/field-groups`)
*   `POST /`: 创建地块分组
*   `PUT /{id}`: 更新地块分组
*   `DELETE /{id}`: 删除地块分组
*   `GET /`: 获取当前用户地块分组列表
*   `GET /{id}`: 获取地块分组详情

### 天气 (`/weather`)
*   `POST /current`: 获取实时天气 (请求体包含地点信息)
*   `POST /forecast`: 获取天气预报 (请求体包含地点信息)
*   `POST /historical`: 获取历史天气 (请求体包含地点信息)

### 通知 (`/notifications`)
*   `GET /`: 获取当前用户通知 (分页)
*   `GET /unread`: 获取未读通知列表
*   `GET /unread/count`: 获取未读通知数量
*   `PUT /{id}/read`: 标记通知为已读
*   `PUT /read-all`: 标记所有通知为已读
*   `DELETE /{id}`: 删除通知
*   `GET /settings`: 获取通知设置
*   `PUT /settings`: 更新通知设置

### 公告 (`/announcements`)
*   `GET /public`: 获取活动公告列表
*   `GET /public/type/{type}`: 按类型获取活动公告
*   `GET /public/{id}`: 获取公告详情
*   `POST /`: (Admin) 创建公告
*   `PUT /{id}`: (Admin) 更新公告
*   `PUT /{id}/publish`: (Admin) 发布公告
*   `DELETE /{id}`: (Admin) 删除公告
*   `GET /`: (Admin) 获取所有公告 (分页)
*   `GET /type/{type}`: (Admin) 按类型获取公告 (分页)
*   `GET /status/{status}`: (Admin) 按状态获取公告 (分页)

### 文件 (`/files`)
*   `POST /avatar`: 上传用户头像文件

## 5. 环境准备

### 5.1 前提条件
*   JDK 11 或更高版本
*   Maven 3.6 或更高版本
*   MySQL 8.0 或更高版本
*   邮件服务器用于发送邮件通知和密码重置
*   外部天气服务 API Key (如OpenWeatherMap)

### 5.2 配置
主要的配置文件是 `src/main/resources/application.yml` 。已配置了以下关键项：

*   **数据库连接:**
    *   `spring.datasource.url`: jdbc:mysql://localhost:3305/agrometeorology?useSSL=false&serverTimezone=Asia/Shanghai
    *   `spring.datasource.username`: root
    *   `spring.datasource.password`: 123456
    *   `spring.jpa.hibernate.ddl-auto`: update (开发环境)

*   **JWT 配置:**
    *   `jwt.secret`: (已设置一个安全密钥)
    *   `jwt.expirationMs`: 86400000 (24小时)

*   **文件上传配置:**
    *   `file.upload.path`: uploads (文件上传路径)
    *   `file.access.url`: http://localhost:8080/api/uploads/ (文件访问URL)

*   **OpenWeatherMap API配置:**
    *   已配置相关API端点
    *   API密钥通过管理员后台配置

*   **日志配置:**
    *   已配置合适的日志级别

## 6. 启动应用

1. 确保已安装JDK和Maven
2. 确保MySQL数据库已启动并创建好对应数据库
3. 克隆或下载项目代码
4. 进入项目根目录，执行以下命令构建并运行项目:

```bash
mvn clean install
mvn spring-boot:run
```

或者直接使用IDE（如IntelliJ IDEA或Eclipse）导入项目后运行AgrometeorologyApplication类
