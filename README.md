# 快提醒 (QuickAlert)

一个优雅的 Android 短信强提醒 App，支持按关键词和发送方匹配短信，触发全屏提醒 + 铃声循环播放。

## 功能特性

- 🔔 **通知监听**: 兼容所有短信 App（通过 NotificationListenerService）
- 🎯 **智能匹配**: 支持关键词和发送方号码匹配（包含/精确/开头/正则）
- 📱 **全屏提醒**: 锁屏也能看到提醒，循环播放铃声 + 振动
- 🎨 **现代 UI**: Jetpack Compose + Material 3 Design
- 💾 **本地存储**: Room 数据库持久化规则
- ⚙️ **规则管理**: 添加/编辑/删除/启用禁用规则

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **数据库**: Room
- **设计**: Material 3
- **架构**: MVVM
- **通知监听**: NotificationListenerService
- **导航**: Navigation Compose

## 项目结构

```
app/src/main/java/com/quickalert/app/
├── QuickAlertApp.kt              # Application 类
├── MainActivity.kt                # 主 Activity
├── data/
│   ├── AlertRule.kt               # Room Entity - 提醒规则
│   ├── AlertRuleDao.kt            # Room DAO
│   └── AppDatabase.kt             # Room Database
├── service/
│   └── SmsNotificationListener.kt # 通知监听服务
├── ui/
│   ├── AlertFullScreenActivity.kt # 全屏提醒界面
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── screens/
│       ├── RuleViewModel.kt       # 规则管理 ViewModel
│       ├── RuleListScreen.kt      # 规则列表界面
│       └── RuleEditScreen.kt      # 规则编辑界面
└── navigation/
    └── NavGraph.kt                # 导航图
```

## 使用方法

### 1. 构建项目
```bash
./gradlew assembleDebug
```

### 2. 安装应用
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3. 开启通知监听权限
- 打开 App → 点击右上角通知图标
- 或前往: 设置 → 通知访问权限 → 找到"快提醒" → 开启

### 4. 添加提醒规则
- 点击右下角 + 按钮
- 设置规则名称
- 配置发送方匹配（可选）
- 配置关键词匹配（可选）
- 选择匹配方式
- 保存规则

## 匹配类型说明

| 类型 | 说明 | 示例 |
|------|------|------|
| 包含 | 文本中包含指定内容 | "验证码" 匹配 "您的验证码是1234" |
| 精确 | 完全匹配 | "10086" 只匹配 "10086" |
| 开头 | 以此开头 | "银行" 匹配 "银行通知：..." |
| 正则 | 正则表达式 | `\d{6}` 匹配任何6位数字 |

## 支持的短信 App

- Android 原生短信 (com.android.mms)
- Google Messages (com.google.android.apps.messaging)
- 三星短信 (com.samsung.android.messaging)
- 小米短信 (com.miui.sms)
- 华为短信 (com.huawei.android.mms)
- OPPO 短信 (com.coloros.mms)
- 以及其他所有短信 App

## 权限说明

- `POST_NOTIFICATIONS`: 发送提醒通知
- `VIBRATE`: 振动提醒
- `BIND_NOTIFICATION_LISTENER_SERVICE`: 监听通知（需用户手动开启）

## License

MIT
