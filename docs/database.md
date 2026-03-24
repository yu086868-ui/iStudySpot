# iStudySpot 数据库设计文档

## 一、数据库概览

### 1.1 数据库信息
- **数据库名**：`iseatspace`
- **字符集**：`utf8mb4`
- **排序规则**：`utf8mb4_unicode_ci`
- **存储引擎**：`InnoDB`

### 1.2 表清单

| 表名              | 说明           | 依赖              |
| ----------------- | -------------- | ----------------- |
| `study_room`      | 自习室表       | 无                |
| `area`            | 区域表         | study_room        |
| `seat`            | 座位表         | study_room, area  |
| `user`            | 用户表         | 无                |
| `price_strategy`  | 价格策略表     | study_room, area  |
| `order`           | 订单表         | user, seat        |
| `order_detail`    | 订单明细表     | order             |
| `payment_log`     | 支付流水表     | order, user       |
| `seat_status_log` | 座位状态流水表 | seat, order, user |
| `blacklist`       | 黑名单表       | user, study_room  |

### 1.3数据库ER图

![p1](https://raw.githubusercontent.com/Siangus/pictures/14e5d13e27a2d44ac27dc61659cb41eb45cc71a9/p1.png)

## 二、详细表结构

### 2.1 自习室表 (study_room)

| 字段名      | 类型          | 约束                       | 说明                |
| ----------- | ------------- | -------------------------- | ------------------- |
| id          | bigint        | PRIMARY KEY AUTO_INCREMENT | 自习室ID            |
| name        | varchar(100)  | NOT NULL                   | 自习室名称          |
| address     | varchar(200)  |                            | 地址                |
| latitude    | decimal(10,8) |                            | 纬度                |
| longitude   | decimal(11,8) |                            | 经度                |
| open_time   | time          |                            | 开门时间            |
| close_time  | time          |                            | 关门时间            |
| description | text          |                            | 描述                |
| images      | varchar(2000) |                            | 图片URL，逗号分隔   |
| status      | tinyint       | DEFAULT 1                  | 状态：0-关闭 1-营业 |
| create_time | datetime      | NOT NULL                   | 创建时间            |
| update_time | datetime      |                            | 更新时间            |

### 2.2 区域表 (area)

| 字段名      | 类型         | 约束                       | 说明         |
| ----------- | ------------ | -------------------------- | ------------ |
| id          | bigint       | PRIMARY KEY AUTO_INCREMENT | 区域ID       |
| room_id     | bigint       | FOREIGN KEY                | 所属自习室ID |
| name        | varchar(50)  | NOT NULL                   | 区域名称     |
| description | varchar(200) |                            | 区域描述     |
| sort_order  | int          | DEFAULT 0                  | 排序         |
| create_time | datetime     | NOT NULL                   | 创建时间     |
| update_time | datetime     |                            | 更新时间     |

### 2.3 座位表 (seat) - **核心表**

| 字段名      | 类型        | 约束                       | 说明                              |
| ----------- | ----------- | -------------------------- | --------------------------------- |
| id          | bigint      | PRIMARY KEY AUTO_INCREMENT | 座位ID                            |
| room_id     | bigint      | FOREIGN KEY                | 自习室ID                          |
| area_id     | bigint      | FOREIGN KEY                | 区域ID                            |
| seat_number | varchar(20) | NOT NULL                   | 座位编号（如A01）                 |
| seat_type   | tinyint     |                            | 类型：1-普通 2-沙发 3-隔间 4-包厢 |
| row_num     | int         |                            | 座位图行坐标                      |
| col_num     | int         |                            | 座位图列坐标                      |
| has_power   | tinyint(1)  | DEFAULT 0                  | 是否有电源                        |
| has_lamp    | tinyint(1)  | DEFAULT 0                  | 是否有台灯                        |
| is_window   | tinyint(1)  | DEFAULT 0                  | 是否靠窗                          |
| is_quiet    | tinyint(1)  | DEFAULT 0                  | 是否静音区                        |
| status      | tinyint     | DEFAULT 1                  | 状态：0-禁用 1-启用 2-维护中      |
| create_time | datetime    | NOT NULL                   | 创建时间                          |
| update_time | datetime    |                            | 更新时间                          |

### 2.4 用户表 (user)

| 字段名          | 类型          | 约束                       | 说明                |
| --------------- | ------------- | -------------------------- | ------------------- |
| id              | bigint        | PRIMARY KEY AUTO_INCREMENT | 用户ID              |
| openid          | varchar(64)   | UNIQUE NOT NULL            | 微信openid          |
| nickname        | varchar(100)  |                            | 昵称                |
| avatar_url      | varchar(500)  |                            | 头像URL             |
| phone           | varchar(20)   |                            | 手机号              |
| balance         | decimal(10,2) | DEFAULT 0.00               | 账户余额            |
| points          | int           | DEFAULT 0                  | 积分                |
| status          | tinyint       | DEFAULT 1                  | 状态：0-禁用 1-正常 |
| violation_count | int           | DEFAULT 0                  | 违规次数            |
| last_login_time | datetime      |                            | 最后登录时间        |
| create_time     | datetime      | NOT NULL                   | 创建时间            |
| update_time     | datetime      |                            | 更新时间            |

### 2.5 价格策略表 (price_strategy)

| 字段名      | 类型          | 约束                       | 说明                      |
| ----------- | ------------- | -------------------------- | ------------------------- |
| id          | bigint        | PRIMARY KEY AUTO_INCREMENT | 策略ID                    |
| room_id     | bigint        | FOREIGN KEY                | 自习室ID（null表示全局）  |
| area_id     | bigint        | FOREIGN KEY                | 区域ID（null表示全部）    |
| seat_type   | tinyint       |                            | 座位类型（null表示全部）  |
| week_days   | varchar(20)   |                            | 适用星期（1-7，逗号分隔） |
| start_time  | time          | NOT NULL                   | 开始时段                  |
| end_time    | time          | NOT NULL                   | 结束时段                  |
| price       | decimal(10,2) | NOT NULL                   | 每小时价格                |
| is_holiday  | tinyint(1)    | DEFAULT 0                  | 是否节假日价格            |
| priority    | int           | DEFAULT 0                  | 优先级（数字越小越优先）  |
| create_time | datetime      | NOT NULL                   | 创建时间                  |
| update_time | datetime      |                            | 更新时间                  |

### 2.6 订单表 (order) - **核心表**

| 字段名            | 类型          | 约束                       | 说明                                                         |
| ----------------- | ------------- | -------------------------- | ------------------------------------------------------------ |
| id                | bigint        | PRIMARY KEY AUTO_INCREMENT | 订单ID                                                       |
| order_no          | varchar(32)   | UNIQUE NOT NULL            | 订单号                                                       |
| user_id           | bigint        | FOREIGN KEY                | 用户ID                                                       |
| seat_id           | bigint        | FOREIGN KEY                | 座位ID                                                       |
| seat_number       | varchar(20)   |                            | 座位编号（冗余）                                             |
| room_id           | bigint        | NOT NULL                   | 自习室ID                                                     |
| room_name         | varchar(100)  |                            | 自习室名称（冗余）                                           |
| plan_start_time   | datetime      | NOT NULL                   | 计划开始时间                                                 |
| plan_end_time     | datetime      | NOT NULL                   | 计划结束时间                                                 |
| actual_start_time | datetime      |                            | 实际签到时间                                                 |
| actual_end_time   | datetime      |                            | 实际签退时间                                                 |
| total_hours       | decimal(5,1)  |                            | 总时长（小时）                                               |
| unit_price        | decimal(10,2) |                            | 平均单价                                                     |
| total_amount      | decimal(10,2) | NOT NULL                   | 总金额                                                       |
| pay_amount        | decimal(10,2) |                            | 实付金额                                                     |
| deposit           | decimal(10,2) | DEFAULT 0                  | 押金                                                         |
| status            | tinyint       | NOT NULL                   | 状态：1-待支付 2-已支付待使用 3-使用中 4-已完成 5-已取消 6-退款中 7-已退款 |
| pay_type          | tinyint       |                            | 支付方式：1-余额 2-微信 3-支付宝                             |
| pay_time          | datetime      |                            | 支付时间                                                     |
| cancel_reason     | varchar(255)  |                            | 取消原因                                                     |
| cancel_time       | datetime      |                            | 取消时间                                                     |
| create_time       | datetime      | NOT NULL                   | 创建时间                                                     |
| update_time       | datetime      |                            | 更新时间                                                     |

### 2.7 订单明细表 (order_detail)

| 字段名      | 类型          | 约束                       | 说明       |
| ----------- | ------------- | -------------------------- | ---------- |
| id          | bigint        | PRIMARY KEY AUTO_INCREMENT | 明细ID     |
| order_id    | bigint        | FOREIGN KEY                | 订单ID     |
| start_time  | datetime      | NOT NULL                   | 时段开始   |
| end_time    | datetime      | NOT NULL                   | 时段结束   |
| hours       | decimal(3,1)  | NOT NULL                   | 小时数     |
| price       | decimal(10,2) | NOT NULL                   | 当时价格   |
| amount      | decimal(10,2) | NOT NULL                   | 本时段金额 |
| create_time | datetime      | NOT NULL                   | 创建时间   |

### 2.8 支付流水表 (payment_log)

| 字段名      | 类型          | 约束                       | 说明                       |
| ----------- | ------------- | -------------------------- | -------------------------- |
| id          | bigint        | PRIMARY KEY AUTO_INCREMENT | 支付ID                     |
| order_id    | bigint        | FOREIGN KEY                | 订单ID                     |
| user_id     | bigint        | FOREIGN KEY                | 用户ID                     |
| pay_no      | varchar(64)   | UNIQUE                     | 支付平台流水号             |
| pay_type    | tinyint       | NOT NULL                   | 支付方式                   |
| amount      | decimal(10,2) | NOT NULL                   | 支付金额                   |
| status      | tinyint       | NOT NULL                   | 状态：0-失败 1-成功 2-退款 |
| pay_time    | datetime      |                            | 支付时间                   |
| refund_time | datetime      |                            | 退款时间                   |
| create_time | datetime      | NOT NULL                   | 创建时间                   |

### 2.9 座位状态流水表 (seat_status_log)

| 字段名      | 类型     | 约束                       | 说明                                  |
| ----------- | -------- | -------------------------- | ------------------------------------- |
| id          | bigint   | PRIMARY KEY AUTO_INCREMENT | 流水ID                                |
| seat_id     | bigint   | FOREIGN KEY                | 座位ID                                |
| order_id    | bigint   | FOREIGN KEY                | 关联订单ID                            |
| user_id     | bigint   | FOREIGN KEY                | 使用人ID                              |
| status      | tinyint  | NOT NULL                   | 状态：1-空闲 2-已预订 3-使用中 4-维护 |
| start_time  | datetime | NOT NULL                   | 开始时间                              |
| end_time    | datetime |                            | 结束时间                              |
| create_time | datetime | NOT NULL                   | 创建时间                              |

### 2.10 黑名单表 (blacklist)

| 字段名      | 类型         | 约束                       | 说明                     |
| ----------- | ------------ | -------------------------- | ------------------------ |
| id          | bigint       | PRIMARY KEY AUTO_INCREMENT | ID                       |
| user_id     | bigint       | FOREIGN KEY                | 用户ID                   |
| room_id     | bigint       | FOREIGN KEY                | 自习室ID（null表示全局） |
| reason      | varchar(255) |                            | 拉黑原因                 |
| expire_time | datetime     |                            | 解除时间                 |
| create_time | datetime     | NOT NULL                   | 创建时间                 |
| update_time | datetime     |                            | 更新时间                 |

---

## 三、索引设计

| 表名            | 索引名          | 字段                           | 说明           |
| --------------- | --------------- | ------------------------------ | -------------- |
| user            | idx_openid      | openid                         | 微信openid查询 |
| user            | idx_status      | status                         | 用户状态筛选   |
| order           | idx_user_id     | user_id                        | 用户订单查询   |
| order           | idx_seat_id     | seat_id                        | 座位订单查询   |
| order           | idx_status      | status                         | 订单状态筛选   |
| order           | idx_create_time | create_time                    | 时间范围查询   |
| order           | idx_plan_time   | plan_start_time, plan_end_time | 时间段查询     |
| seat            | idx_room_id     | room_id                        | 自习室座位查询 |
| seat            | idx_area_id     | area_id                        | 区域座位查询   |
| seat_status_log | idx_seat_id     | seat_id                        | 座位历史查询   |
| seat_status_log | idx_time_range  | start_time, end_time           | 时间范围查询   |
| blacklist       | idx_expire_time | expire_time                    | 过期黑名单清理 |

---

## 四、Redis缓存设计

| Key格式                          | 类型   | 说明               | 过期时间           |
| -------------------------------- | ------ | ------------------ | ------------------ |
| `seat:status:{roomId}`           | Hash   | 座位实时状态       | 永久（更新时同步） |
| `seat:lock:{seatId}:{timeSlot}`  | String | 分布式锁（防并发） | 3秒                |
| `price:{roomId}:{areaId}:{time}` | String | 价格缓存           | 1小时              |
| `user:token:{userId}`            | String | JWT token          | 7天                |
| `room:info:{roomId}`             | String | 自习室信息         | 1小时              |
| `blacklist:user:{userId}`        | Set    | 用户黑名单         | 永久               |

---

## 五、核心SQL示例

### 5.1 查询座位实时状态
```sql
SELECT 
    s.id, s.seat_number, s.seat_type,
    s.has_power, s.has_lamp, s.is_window,
    CASE 
        WHEN o.id IS NOT NULL AND o.status IN (2,3) THEN 'occupied'
        WHEN o.id IS NOT NULL AND o.status = 1 THEN 'booked'
        ELSE 'free'
    END as current_status,
    o.plan_start_time, o.plan_end_time
FROM seat s
LEFT JOIN `order` o ON s.id = o.seat_id 
    AND o.status IN (1,2,3) 
    AND NOW() BETWEEN o.plan_start_time AND o.plan_end_time
WHERE s.room_id = ? AND s.status = 1;
```

### 5.2 检查时间段是否可预订

```sql
SELECT COUNT(*) 
FROM `order` 
WHERE seat_id = ? 
    AND status IN (1,2,3)
    AND (
        (plan_start_time <= ? AND plan_end_time > ?) OR
        (plan_start_time < ? AND plan_end_time >= ?) OR
        (plan_start_time >= ? AND plan_end_time <= ?)
    );
```

### 5.3 统计自习室上座率

```sql
SELECT 
    DATE(create_time) as date,
    HOUR(create_time) as hour,
    COUNT(*) as order_count,
    SUM(total_hours) as total_hours,
    SUM(pay_amount) as revenue
FROM `order`
WHERE room_id = ? 
    AND create_time BETWEEN ? AND ?
    AND status = 4
GROUP BY DATE(create_time), HOUR(create_time)
ORDER BY date, hour;
```

---

## 六、视图

### 6.1 座位实时状态视图

```sql
CREATE VIEW v_seat_current_status AS
SELECT 
    s.id AS seat_id,
    s.seat_number,
    s.room_id,
    r.name AS room_name,
    a.name AS area_name,
    s.seat_type,
    s.has_power,
    s.has_lamp,
    s.is_window,
    s.is_quiet,
    s.row_num,
    s.col_num,
    CASE 
        WHEN o.id IS NOT NULL AND o.status = 2 THEN 'paid'
        WHEN o.id IS NOT NULL AND o.status = 3 THEN 'using'
        WHEN o.id IS NOT NULL AND o.status = 1 THEN 'booked'
        ELSE 'free'
    END AS current_status,
    o.id AS current_order_id,
    o.plan_start_time,
    o.plan_end_time,
    o.user_id
FROM seat s
JOIN study_room r ON s.room_id = r.id
LEFT JOIN area a ON s.area_id = a.id
LEFT JOIN `order` o ON s.id = o.seat_id 
    AND o.status IN (1,2,3) 
    AND NOW() BETWEEN o.plan_start_time AND o.plan_end_time
WHERE s.status = 1;
```

---

## 七、初始化数据示例

### 7.1 自习室数据

```sql
INSERT INTO `study_room` (`name`, `address`, `open_time`, `close_time`) VALUES
('iStudySpot 学习空间（五道口店）', '北京市海淀区五道口购物中心3F', '08:00:00', '23:00:00'),
('iStudySpot 静心自习室（中关村店）', '北京市海淀区中关村创业大街B2', '09:00:00', '22:00:00'),
('iStudySpot 24h自习舱（望京店）', '北京市朝阳区望京SOHO T2-3F', '00:00:00', '23:59:59');
```

### 7.2 区域数据

```sql
INSERT INTO `area` (`room_id`, `name`, `description`, `sort_order`) VALUES
(1, '沉浸学习区', '完全静音，禁止交谈', 1),
(1, '轻讨论区', '允许低声讨论', 2),
(1, 'VIP包厢', '独立空间，适合小组学习', 3);
```

### 7.3 价格策略数据

```sql
INSERT INTO `price_strategy` (`room_id`, `week_days`, `start_time`, `end_time`, `price`, `priority`) VALUES
(1, '1,2,3,4,5', '08:00:00', '18:00:00', 15.00, 1),
(1, '1,2,3,4,5', '18:00:00', '23:00:00', 12.00, 1),
(1, '6,7', '08:00:00', '23:00:00', 18.00, 1);
```

---

## 八、建表SQL（完整脚本）

完整的建表SQL脚本请见同目录下 `iStudySpot.sql` 文件。