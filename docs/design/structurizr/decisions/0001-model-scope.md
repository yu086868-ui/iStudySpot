# 1. 仅建模当前后端与 Android 代码范围

## 状态

已接受

## 背景

仓库中不止一个产品面，但这次架构建模的明确目标是只覆盖后端与 Android 客户端。

## 决策

Structurizr 工作区只建模以下范围：

- `backend/istudyspot-backend` 中的 Spring Boot 后端
- `frontend/Android` 中的 Android 客户端
- 这两部分代码直接可见的外部依赖

## 影响

- 模型会严格贴合本次代码阅读的实际范围
- 图中不会暗示已经覆盖管理端、小程序或更大范围的基础设施
