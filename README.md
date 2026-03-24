# iStudySpot-自习室预订系统

## 团队成员

| 姓名  | 学号         | 分工                            |
| :-- | :--------- | :---------------------------- |
| 余逸晨 | 2312190113 | 后端开发、数据库设计、核心业务逻辑（预订/计费/并发处理） |
| 黄益政 | 2312190331 | Android App开发、管理端功能实现、数据统计大屏  |
| 贺祥宇 | 2312190107 | 微信小程序开发、座位图可视化、用户端交互实现        |

## 项目简介

iStudySpot是一款面向付费自习室的在线预订系统，主要服务于学习学生、考研党、考证族和远程办公人群。用户可通过微信小程序实时查看自习室3D座位图，按小时/天预订座位，系统自动计算价格并支持在线支付。到店扫码签到后自动计时，离店一键签退结算。针对自习室管理者，提供可视化座位配置、灵活的价格策略（忙/闲时定价）、上座率统计和用户黑名单管理。系统通过Redis分布式锁解决高峰期热门座位并发抢座问题，确保数据一致性，同时通过定时任务自动释放超时未签到订单，提升座位利用率。

***
  
---

## 技术栈（初步规划）

- 前端（微信小程序）：微信原生框架 + Vant Weapp（UI组件）+ ECharts（数据可视化）+ Canvas（座位图绘制）
- 前端（Android）：Kotlin + Jetpack（ViewModel/LiveData）+ Retrofit（网络请求）+ MPAndroidChart（统计图表）+ Custom View（座位图）
- 后端：SpringBoot、SpringMVC、MyBatis、JWT（登录鉴权）、WebSocket（实时状态推送）、Spring Task（定时任务）
- 数据库：MySQL（业务数据存储）、Redis（缓存座位状态、分布式锁、计数器）

<br />

## Figma 链接

小程序：<https://www.figma.com/proto/8NU6ppFuycu9ck0OQhlcx6/miniprogram?node-id=2-1173&p=f&t=xpHscwXPSmeIUJy7-1&scaling=scale-down&content-scaling=fixed&page-id=0%3A1&starting-point-node-id=2%3A1173>
