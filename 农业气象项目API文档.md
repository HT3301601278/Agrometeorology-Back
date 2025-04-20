# 农业气象项目API文档

## 目录
1. [认证模块](#1-认证模块)
2. [用户模块](#2-用户模块)
   1. [获取当前用户信息](#21-获取当前用户信息)
   2. [更新用户信息](#22-更新用户信息)
   3. [修改密码](#23-修改密码)
   4. [上传用户头像](#24-上传用户头像)
   5. [更新用户头像](#25-更新用户头像)
   6. [获取用户信息（管理员）](#26-获取用户信息管理员)
   7. [更新用户状态（管理员）](#27-更新用户状态管理员)
   8. [删除用户（管理员）](#28-删除用户管理员)
3. [管理员模块](#3-管理员模块)
4. [公告模块](#4-公告模块)
5. [通知模块](#5-通知模块)

## 1. 认证模块
基础URL: `/auth`

### 1.1 用户注册
- **URL**: `/auth/register`
- **方法**: POST
- **描述**: 注册新用户
- **请求体**:
```json
{
  "username": "张三",
  "password": "password123",
  "email": "zhangsan@example.com",
  "nickname": "小张",
  "phone": "13800138000"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "张三",
    "email": "zhangsan@example.com",
    "nickname": "小张",
    "phone": "13800138000",
    "status": true,
    "createdAt": "2023-06-01T10:00:00"
  }
}
```
- **测试用例**:
  - 使用有效数据注册
  - 尝试使用已存在的用户名注册
  - 尝试使用已存在的邮箱注册
  - 使用无效的邮箱格式注册
  - 使用弱密码注册

### 1.2 用户登录
- **URL**: `/auth/login`
- **方法**: POST
- **描述**: 用户登录并获取令牌
- **请求体**:
```json
{
  "username": "张三",
  "password": "password123"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "张三",
      "email": "zhangsan@example.com",
      "nickname": "小张",
      "roles": ["ROLE_USER"]
    }
  }
}
```
- **测试用例**:
  - 使用正确的用户名和密码登录
  - 使用错误的密码登录
  - 使用不存在的用户名登录
  - 使用被冻结的账户登录

### 1.3 请求密码重置
- **URL**: `/auth/password/reset-request`
- **方法**: POST
- **描述**: 发送密码重置验证码到用户邮箱
- **请求参数**:
  - `email`: 用户注册的邮箱
- **响应**:
```json
{
  "success": true,
  "message": "验证码已发送，请检查您的邮箱",
  "data": null
}
```
- **测试用例**:
  - 为已注册邮箱请求重置密码
  - 为未注册邮箱请求重置密码

### 1.4 重置密码
- **URL**: `/auth/password/reset`
- **方法**: POST
- **描述**: 使用验证码重置密码
- **请求体**:
```json
{
  "email": "zhangsan@example.com",
  "code": "123456",
  "newPassword": "newPassword123"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "密码重置成功",
  "data": null
}
```
- **测试用例**:
  - 使用正确的验证码重置密码
  - 使用错误的验证码重置密码
  - 使用过期的验证码重置密码
  - 设置过于简单的新密码

## 2. 用户模块
基础URL: `/users`

### 2.1 获取当前用户信息
- **URL**: `/users/me`
- **方法**: GET
- **描述**: 获取当前登录用户的详细信息
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "张三",
    "email": "zhangsan@example.com",
    "nickname": "小张",
    "phone": "13800138000",
    "status": true,
    "createdAt": "2023-06-01T10:00:00"
  }
}
```
- **测试用例**:
  - 使用有效的令牌请求
  - 使用过期的令牌请求
  - 不提供令牌请求

### 2.2 更新用户信息
- **URL**: `/users/me`
- **方法**: PUT
- **描述**: 更新当前登录用户的个人信息
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "nickname": "张三丰",
  "phone": "13900139000",
  "email": "zhangsanfeng@example.com"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "张三",
    "email": "zhangsanfeng@example.com",
    "nickname": "张三丰",
    "phone": "13900139000",
    "status": true,
    "createdAt": "2023-06-01T10:00:00"
  }
}
```
- **测试用例**:
  - 更新单个字段
  - 更新多个字段
  - 更新为已存在的邮箱

### 2.3 修改密码
- **URL**: `/users/me/password`
- **方法**: PUT
- **描述**: 修改当前登录用户的密码
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "oldPassword": "password123",
  "newPassword": "newPassword123"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "密码修改成功",
  "data": null
}
```
- **测试用例**:
  - 使用正确的旧密码修改密码
  - 使用错误的旧密码修改密码
  - 新密码与旧密码相同

### 2.4 上传用户头像
- **URL**: `/files/avatar`
- **方法**: POST
- **描述**: 上传用户头像图片
- **请求体**: `multipart/form-data`形式
  - `file`: 图片文件（必须是图片格式）
- **响应**:
```json
{
  "success": true,
  "message": "头像上传成功",
  "data": "http://localhost:8080/api/uploads/avatar/avatar_550e8400-e29b-41d4-a716-446655440000.jpg"
}
```
- **测试用例**:
  - 上传有效的图片文件
  - 上传非图片类型的文件
  - 上传超过大小限制的文件
  - 不提供文件上传

### 2.5 更新用户头像
- **URL**: `/users/me/avatar`
- **方法**: PUT
- **描述**: 更新当前用户的头像URL
- **请求头**: Authorization: Bearer {token}
- **请求参数**:
  - `avatar`: 头像图片URL（通常是由上传接口返回的URL）
- **响应**:
```json
{
  "success": true,
  "message": "头像更新成功",
  "data": {
    "id": 1,
    "username": "张三",
    "email": "zhangsan@example.com",
    "nickname": "小张",
    "avatar": "http://localhost:8080/api/uploads/avatar/avatar_550e8400-e29b-41d4-a716-446655440000.jpg",
    "phone": "13800138000",
    "status": true,
    "createdAt": "2023-06-01T10:00:00"
  }
}
```
- **测试用例**:
  - 使用有效的图片URL更新头像
  - 使用无效的URL格式
  - 未提供授权令牌

### 2.6 获取用户信息（管理员）
- **URL**: `/users/{id}`
- **方法**: GET
- **描述**: 管理员获取指定用户的详细信息
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `id`: 用户ID
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "id": 2,
    "username": "李四",
    "email": "lisi@example.com",
    "nickname": "小李",
    "phone": "13700137000",
    "status": true,
    "createdAt": "2023-06-02T10:00:00"
  }
}
```
- **测试用例**:
  - 管理员获取存在的用户信息
  - 管理员获取不存在的用户信息
  - 普通用户尝试访问此接口

### 2.7 更新用户状态（管理员）
- **URL**: `/users/{id}/status`
- **方法**: PUT
- **描述**: 管理员冻结或解冻用户账户
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `id`: 用户ID
- **响应**:
```json
{
  "success": true,
  "message": "用户状态已启用",
  "data": {
    "id": 2,
    "username": "李四",
    "email": "lisi@example.com",
    "nickname": "小李",
    "phone": "13700137000",
    "status": true,
    "createdAt": "2023-06-02T10:00:00"
  }
}
```
- **测试用例**:
  - 管理员冻结活跃用户
  - 管理员解冻已冻结用户
  - 管理员冻结不存在的用户
  - 普通用户尝试访问此接口

### 2.8 删除用户（管理员）
- **URL**: `/users/{id}`
- **方法**: DELETE
- **描述**: 管理员删除指定用户
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `id`: 用户ID
- **响应**:
```json
{
  "success": true,
  "message": "用户删除成功",
  "data": null
}
```
- **测试用例**:
  - 管理员删除存在的用户
  - 管理员删除不存在的用户
  - 管理员尝试删除自己的账户
  - 普通用户尝试访问此接口

## 3. 管理员模块
基础URL: `/admin`

### 3.1 获取所有用户（分页）
- **URL**: `/admin/users`
- **方法**: GET
- **描述**: 获取所有用户的列表（分页）
- **请求头**: Authorization: Bearer {token}
- **查询参数**:
  - `page`: 页码（默认0）
  - `size`: 每页数量（默认20）
  - `sort`: 排序字段（默认createdAt,desc）
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "张三",
        "email": "zhangsan@example.com",
        "nickname": "小张",
        "phone": "13800138000",
        "status": true,
        "createdAt": "2023-06-01T10:00:00"
      },
      {
        "id": 2,
        "username": "李四",
        "email": "lisi@example.com",
        "nickname": "小李",
        "phone": "13700137000",
        "status": true,
        "createdAt": "2023-06-02T10:00:00"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": true,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 2,
    "size": 20,
    "number": 0,
    "empty": false
  }
}
```
- **测试用例**:
  - 管理员获取所有用户
  - 测试分页功能（获取第2页）
  - 测试排序功能（按用户名排序）
  - 普通用户尝试访问此接口

### 3.2 获取所有系统配置（分页）
- **URL**: `/admin/configs`
- **方法**: GET
- **描述**: 获取所有系统配置项（分页）
- **请求头**: Authorization: Bearer {token}
- **查询参数**:
  - `page`: 页码（默认0）
  - `size`: 每页数量（默认20）
  - `sort`: 排序字段（默认id,asc）
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "key": "API_KEY",
        "value": "sk-abcdef123456",
        "description": "第三方API密钥"
      },
      {
        "id": 2,
        "key": "DATA_FETCH_INTERVAL",
        "value": "30",
        "description": "数据拉取间隔（分钟）"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": true,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 2,
    "size": 20,
    "number": 0,
    "empty": false
  }
}
```
- **测试用例**:
  - 管理员获取所有配置
  - 测试分页功能（获取第2页）
  - 普通用户尝试访问此接口

### 3.3 保存系统配置
- **URL**: `/admin/configs`
- **方法**: POST
- **描述**: 创建或更新系统配置项
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "key": "SITE_NAME",
  "value": "农业气象系统",
  "description": "网站名称"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "配置保存成功",
  "data": {
    "id": 3,
    "key": "SITE_NAME",
    "value": "农业气象系统",
    "description": "网站名称"
  }
}
```
- **测试用例**:
  - 创建新配置
  - 更新现有配置
  - 创建没有描述的配置
  - 普通用户尝试访问此接口

### 3.4 删除系统配置
- **URL**: `/admin/configs/{key}`
- **方法**: DELETE
- **描述**: 删除系统配置项
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `key`: 配置键名
- **响应**:
```json
{
  "success": true,
  "message": "配置删除成功",
  "data": null
}
```
- **测试用例**:
  - 删除存在的配置
  - 删除不存在的配置
  - 普通用户尝试访问此接口

### 3.5 获取API Key配置
- **URL**: `/admin/configs/api-key`
- **方法**: GET
- **描述**: 获取系统中配置的API Key
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "apiKey": "sk-abcdef123456"
  }
}
```
- **测试用例**:
  - 管理员获取API Key
  - 获取不存在的API Key
  - 普通用户尝试访问此接口

### 3.6 保存API Key配置
- **URL**: `/admin/configs/api-key`
- **方法**: POST
- **描述**: 设置或更新系统的API Key
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "apiKey": "sk-abcdef123456"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "API Key已保存",
  "data": null
}
```
- **测试用例**:
  - 设置新的API Key
  - 更新现有API Key
  - 普通用户尝试访问此接口

### 3.7 获取数据拉取频率配置
- **URL**: `/admin/configs/fetch-interval`
- **方法**: GET
- **描述**: 获取系统中配置的数据拉取频率（分钟）
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "interval": 30
  }
}
```
- **测试用例**:
  - 管理员获取拉取频率
  - 获取不存在的拉取频率配置
  - 普通用户尝试访问此接口

### 3.8 保存数据拉取频率配置
- **URL**: `/admin/configs/fetch-interval`
- **方法**: POST
- **描述**: 设置或更新系统的数据拉取频率
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "interval": 30
}
```
- **响应**:
```json
{
  "success": true,
  "message": "数据拉取频率已设置",
  "data": null
}
```
- **测试用例**:
  - 设置新的拉取频率
  - 更新现有拉取频率
  - 设置无效的拉取频率（如负数）
  - 普通用户尝试访问此接口

### 3.9 获取邮件配置
- **URL**: `/admin/configs/email`
- **方法**: GET
- **描述**: 获取系统中配置的邮件服务器信息
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "host": "smtp.example.com",
    "port": 587,
    "username": "admin@example.com",
    "password": "************",
    "auth": true,
    "startTls": true
  }
}
```
- **测试用例**:
  - 管理员获取邮件配置
  - 获取不存在的邮件配置
  - 普通用户尝试访问此接口

### 3.10 保存邮件配置
- **URL**: `/admin/configs/email`
- **方法**: POST
- **描述**: 设置或更新系统的邮件服务器配置
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "host": "smtp.example.com",
  "port": 587,
  "username": "admin@example.com",
  "password": "password123",
  "auth": true,
  "startTls": true
}
```
- **响应**:
```json
{
  "success": true,
  "message": "邮件配置已保存",
  "data": null
}
```
- **测试用例**:
  - 设置新的邮件配置
  - 更新现有邮件配置
  - 使用无效的端口号
  - 普通用户尝试访问此接口

### 3.11 发送系统通知
- **URL**: `/admin/notifications`
- **方法**: POST
- **描述**: 管理员向所有用户发送系统通知
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "title": "系统维护通知",
  "content": "系统将于2023年6月10日22:00-24:00进行维护，期间可能无法访问。"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "系统通知已发送",
  "data": null
}
```
- **测试用例**:
  - 发送有效的系统通知
  - 发送没有标题的通知
  - 发送内容为空的通知
  - 普通用户尝试访问此接口

## 4. 公告模块
基础URL: `/announcements`

### 4.1 获取活动公告
- **URL**: `/announcements/public`
- **方法**: GET
- **描述**: 获取所有已发布且未过期的公告
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "系统上线通知",
      "content": "农业气象系统正式上线...",
      "type": 1,
      "status": 1,
      "publishTime": "2023-06-01T09:00:00",
      "expireTime": "2023-12-31T23:59:59",
      "createdAt": "2023-05-30T15:00:00"
    },
    {
      "id": 2,
      "title": "气象预警",
      "content": "未来三天可能有强降雨...",
      "type": 2,
      "status": 1,
      "publishTime": "2023-06-05T09:00:00",
      "expireTime": "2023-06-08T23:59:59",
      "createdAt": "2023-06-04T15:00:00"
    }
  ]
}
```
- **测试用例**:
  - 获取所有活动公告
  - 当没有活动公告时的响应

### 4.2 根据类型获取活动公告
- **URL**: `/announcements/public/type/{type}`
- **方法**: GET
- **描述**: 根据类型获取活动公告
- **路径参数**:
  - `type`: 公告类型（1-系统公告，2-预警通知）
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": [
    {
      "id": 2,
      "title": "气象预警",
      "content": "未来三天可能有强降雨...",
      "type": 2,
      "status": 1,
      "publishTime": "2023-06-05T09:00:00",
      "expireTime": "2023-06-08T23:59:59",
      "createdAt": "2023-06-04T15:00:00"
    }
  ]
}
```
- **测试用例**:
  - 获取特定类型的公告
  - 获取不存在类型的公告
  - 特定类型没有公告时的响应

### 4.3 获取公告详情
- **URL**: `/announcements/public/{id}`
- **方法**: GET
- **描述**: 获取公告的详细内容
- **路径参数**:
  - `id`: 公告ID
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "系统上线通知",
    "content": "农业气象系统正式上线，提供多种气象数据和预警信息...",
    "type": 1,
    "status": 1,
    "publishTime": "2023-06-01T09:00:00",
    "expireTime": "2023-12-31T23:59:59",
    "createdAt": "2023-05-30T15:00:00"
  }
}
```
- **测试用例**:
  - 获取存在的公告详情
  - 获取不存在的公告详情
  - 获取已过期公告的详情

### 4.4 创建公告（管理员）
- **URL**: `/announcements`
- **方法**: POST
- **描述**: 管理员创建新公告
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "title": "新功能上线",
  "content": "我们新增了历史数据查询功能...",
  "type": 1,
  "status": 0,
  "publishTime": "2023-06-15T09:00:00",
  "expireTime": "2023-07-15T23:59:59"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "公告创建成功",
  "data": {
    "id": 3,
    "title": "新功能上线",
    "content": "我们新增了历史数据查询功能...",
    "type": 1,
    "status": 0,
    "publishTime": "2023-06-15T09:00:00",
    "expireTime": "2023-07-15T23:59:59",
    "createdAt": "2023-06-10T15:00:00"
  }
}
```
- **测试用例**:
  - 创建草稿公告
  - 创建立即发布的公告
  - 创建没有过期时间的公告
  - 普通用户尝试创建公告

### 4.5 更新公告（管理员）
- **URL**: `/announcements/{id}`
- **方法**: PUT
- **描述**: 管理员更新现有公告
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `id`: 公告ID
- **请求体**:
```json
{
  "title": "新功能上线（更新）",
  "content": "我们新增了历史数据查询和导出功能...",
  "type": 1,
  "status": 0,
  "publishTime": "2023-06-20T09:00:00",
  "expireTime": "2023-07-20T23:59:59"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "公告更新成功",
  "data": {
    "id": 3,
    "title": "新功能上线（更新）",
    "content": "我们新增了历史数据查询和导出功能...",
    "type": 1,
    "status": 0,
    "publishTime": "2023-06-20T09:00:00",
    "expireTime": "2023-07-20T23:59:59",
    "createdAt": "2023-06-10T15:00:00"
  }
}
```
- **测试用例**:
  - 更新草稿公告
  - 更新已发布公告
  - 更新已过期公告
  - 更新不存在的公告
  - 普通用户尝试更新公告

### 4.6 发布公告（管理员）
- **URL**: `/announcements/{id}/publish`
- **方法**: PUT
- **描述**: 管理员将草稿公告发布上线
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `id`: 公告ID
- **响应**:
```json
{
  "success": true,
  "message": "公告已发布",
  "data": {
    "id": 3,
    "title": "新功能上线（更新）",
    "content": "我们新增了历史数据查询和导出功能...",
    "type": 1,
    "status": 1,
    "publishTime": "2023-06-20T09:00:00",
    "expireTime": "2023-07-20T23:59:59",
    "createdAt": "2023-06-10T15:00:00"
  }
}
```
- **测试用例**:
  - 发布草稿公告
  - 发布已经发布的公告
  - 发布不存在的公告
  - 普通用户尝试发布公告

### 4.7 删除公告（管理员）
- **URL**: `/announcements/{id}`
- **方法**: DELETE
- **描述**: 管理员删除公告
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `id`: 公告ID
- **响应**:
```json
{
  "success": true,
  "message": "公告已删除",
  "data": null
}
```
- **测试用例**:
  - 删除草稿公告
  - 删除已发布公告
  - 删除不存在的公告
  - 普通用户尝试删除公告

### 4.8 获取所有公告（分页，管理员）
- **URL**: `/announcements`
- **方法**: GET
- **描述**: 管理员获取所有公告（包括草稿、已发布、已过期）
- **请求头**: Authorization: Bearer {token}
- **查询参数**:
  - `page`: 页码（默认0）
  - `size`: 每页数量（默认20）
  - `sort`: 排序字段（默认createdAt,desc）
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 3,
        "title": "新功能上线",
        "content": "我们新增了历史数据查询功能...",
        "type": 1,
        "status": 0,
        "publishTime": "2023-06-15T09:00:00",
        "expireTime": "2023-07-15T23:59:59",
        "createdAt": "2023-06-10T15:00:00"
      },
      {
        "id": 2,
        "title": "气象预警",
        "content": "未来三天可能有强降雨...",
        "type": 2,
        "status": 1,
        "publishTime": "2023-06-05T09:00:00",
        "expireTime": "2023-06-08T23:59:59",
        "createdAt": "2023-06-04T15:00:00"
      },
      {
        "id": 1,
        "title": "系统上线通知",
        "content": "农业气象系统正式上线...",
        "type": 1,
        "status": 1,
        "publishTime": "2023-06-01T09:00:00",
        "expireTime": "2023-12-31T23:59:59",
        "createdAt": "2023-05-30T15:00:00"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 3,
    "totalPages": 1,
    "last": true,
    "first": true,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 3,
    "size": 20,
    "number": 0,
    "empty": false
  }
}
```
- **测试用例**:
  - 管理员获取所有公告
  - 测试分页功能（获取第2页）
  - 测试排序功能（按发布时间排序）
  - 普通用户尝试访问此接口

### 4.9 根据类型获取公告（分页，管理员）
- **URL**: `/announcements/type/{type}`
- **方法**: GET
- **描述**: 管理员根据类型获取公告
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `type`: 公告类型（1-系统公告，2-预警通知）
- **查询参数**:
  - `page`: 页码（默认0）
  - `size`: 每页数量（默认20）
  - `sort`: 排序字段（默认createdAt,desc）
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 2,
        "title": "气象预警",
        "content": "未来三天可能有强降雨...",
        "type": 2,
        "status": 1,
        "publishTime": "2023-06-05T09:00:00",
        "expireTime": "2023-06-08T23:59:59",
        "createdAt": "2023-06-04T15:00:00"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 1,
    "size": 20,
    "number": 0,
    "empty": false
  }
}
```
- **测试用例**:
  - 获取特定类型的公告
  - 获取不存在类型的公告
  - 特定类型没有公告时的响应
  - 普通用户尝试访问此接口

### 4.10 根据状态获取公告（分页，管理员）
- **URL**: `/announcements/status/{status}`
- **方法**: GET
- **描述**: 管理员根据状态获取公告
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `status`: 公告状态（0-草稿，1-已发布）
- **查询参数**:
  - `page`: 页码（默认0）
  - `size`: 每页数量（默认20）
  - `sort`: 排序字段（默认createdAt,desc）
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 3,
        "title": "新功能上线",
        "content": "我们新增了历史数据查询功能...",
        "type": 1,
        "status": 0,
        "publishTime": "2023-06-15T09:00:00",
        "expireTime": "2023-07-15T23:59:59",
        "createdAt": "2023-06-10T15:00:00"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 1,
    "size": 20,
    "number": 0,
    "empty": false
  }
}
```
- **测试用例**:
  - 获取草稿状态的公告
  - 获取已发布状态的公告
  - 获取不存在状态的公告
  - 普通用户尝试访问此接口

## 5. 通知模块
基础URL: `/notifications`

### 5.1 获取当前用户通知（分页）
- **URL**: `/notifications`
- **方法**: GET
- **描述**: 获取当前登录用户的所有通知
- **请求头**: Authorization: Bearer {token}
- **查询参数**:
  - `page`: 页码（默认0）
  - `size`: 每页数量（默认20）
  - `sort`: 排序字段（默认createdAt,desc）
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "系统通知",
        "content": "欢迎使用农业气象系统",
        "type": 1,
        "isRead": true,
        "userId": 1,
        "createdAt": "2023-06-01T10:30:00"
      },
      {
        "id": 2,
        "title": "气象预警",
        "content": "您关注的地区未来24小时有强降雨",
        "type": 2,
        "isRead": false,
        "userId": 1,
        "createdAt": "2023-06-05T09:30:00"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": true,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 2,
    "size": 20,
    "number": 0,
    "empty": false
  }
}
```
- **测试用例**:
  - 获取当前用户的所有通知
  - 测试分页功能（获取第2页）
  - 未登录用户尝试访问此接口

### 5.2 获取未读通知
- **URL**: `/notifications/unread`
- **方法**: GET
- **描述**: 获取当前登录用户的所有未读通知
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "count": 1,
    "notifications": [
      {
        "id": 2,
        "title": "气象预警",
        "content": "您关注的地区未来24小时有强降雨",
        "type": 2,
        "isRead": false,
        "userId": 1,
        "createdAt": "2023-06-05T09:30:00"
      }
    ]
  }
}
```
- **测试用例**:
  - 获取有未读通知的用户的未读通知
  - 获取没有未读通知的用户的未读通知
  - 未登录用户尝试访问此接口

### 5.3 获取未读通知数量
- **URL**: `/notifications/unread/count`
- **方法**: GET
- **描述**: 获取当前登录用户的未读通知数量
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "count": 1
  }
}
```
- **测试用例**:
  - 获取有未读通知的用户的未读通知数量
  - 获取没有未读通知的用户的未读通知数量
  - 未登录用户尝试访问此接口

### 5.4 将通知标记为已读
- **URL**: `/notifications/{id}/read`
- **方法**: PUT
- **描述**: 将指定通知标记为已读
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `id`: 通知ID
- **响应**:
```json
{
  "success": true,
  "message": "标记已读成功",
  "data": null
}
```
- **测试用例**:
  - 将未读通知标记为已读
  - 将已读通知标记为已读
  - 标记不存在的通知
  - 标记不属于当前用户的通知
  - 未登录用户尝试访问此接口

### 5.5 将所有通知标记为已读
- **URL**: `/notifications/read-all`
- **方法**: PUT
- **描述**: 将当前登录用户的所有未读通知标记为已读
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "success": true,
  "message": "已将 1 条通知标记为已读",
  "data": null
}
```
- **测试用例**:
  - 将多条未读通知标记为已读
  - 当没有未读通知时调用此接口
  - 未登录用户尝试访问此接口

### 5.6 删除通知
- **URL**: `/notifications/{id}`
- **方法**: DELETE
- **描述**: 删除指定通知
- **请求头**: Authorization: Bearer {token}
- **路径参数**:
  - `id`: 通知ID
- **响应**:
```json
{
  "success": true,
  "message": "删除成功",
  "data": null
}
```
- **测试用例**:
  - 删除已读通知
  - 删除未读通知
  - 删除不存在的通知
  - 删除不属于当前用户的通知
  - 未登录用户尝试访问此接口

### 5.7 获取通知设置
- **URL**: `/notifications/settings`
- **方法**: GET
- **描述**: 获取当前登录用户的通知设置
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "emailNotify": true,
    "systemNotify": true,
    "createdAt": "2023-06-01T10:00:00",
    "updatedAt": "2023-06-01T10:00:00"
  }
}
```
- **测试用例**:
  - 获取已有设置的用户的通知设置
  - 获取没有设置的用户的通知设置（默认值）
  - 未登录用户尝试访问此接口

### 5.8 更新通知设置
- **URL**: `/notifications/settings`
- **方法**: PUT
- **描述**: 更新当前登录用户的通知设置
- **请求头**: Authorization: Bearer {token}
- **请求体**:
```json
{
  "emailNotify": false,
  "systemNotify": true
}
```
- **响应**:
```json
{
  "success": true,
  "message": "设置已更新",
  "data": {
    "id": 1,
    "userId": 1,
    "emailNotify": false,
    "systemNotify": true,
    "createdAt": "2023-06-01T10:00:00",
    "updatedAt": "2023-06-05T15:30:00"
  }
}
```
- **测试用例**:
  - 更新单个设置
  - 更新多个设置
  - 未登录用户尝试访问此接口