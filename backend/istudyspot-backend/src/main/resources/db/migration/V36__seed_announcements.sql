INSERT INTO `announcement` (`id`, `title`, `content`, `type`, `priority`, `status`, `author`, `publish_time`)
VALUES
    (1, '自习室开放时间调整', '本周起图书馆三层东侧自习区工作日开放至 22:30，请以现场安排为准。', 'notice', 'medium', 'published', '系统管理员', '2026-06-16 09:20:00'),
    (2, '预约成功', '你已成功预约一号自习室 A12，使用时间为 06-16 14:00 至 17:00。', 'notice', 'low', 'published', '系统管理员', '2026-06-16 08:45:00'),
    (3, '签到提醒', '你今天的预约将在 15 分钟后开始，请提前到场并完成签到。', 'notice', 'medium', 'published', '系统管理员', '2026-06-16 08:10:00'),
    (4, '场馆入口临时调整', '受天气影响，今晚 19:00 后二号自习室请从南门进入。', 'notice', 'medium', 'published', '系统管理员', '2026-06-15 17:40:00'),
    (5, '周末高峰提醒', '周末 14:00 至 18:00 为使用高峰，建议提前一天完成座位预约。', 'notice', 'low', 'published', '系统管理员', '2026-06-14 11:30:00')
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `content` = VALUES(`content`),
    `type` = VALUES(`type`),
    `priority` = VALUES(`priority`),
    `status` = VALUES(`status`),
    `author` = VALUES(`author`),
    `publish_time` = VALUES(`publish_time`);
