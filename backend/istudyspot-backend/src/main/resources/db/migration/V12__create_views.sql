-- 座位实时状态视图
CREATE OR REPLACE VIEW v_seat_current_status AS
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

-- 自习室上座率统计视图
CREATE OR REPLACE VIEW v_room_occupancy AS
SELECT
    o.room_id,
    r.name AS room_name,
    DATE(o.create_time) AS date,
    HOUR(o.create_time) AS hour,
    COUNT(*) AS order_count,
    SUM(o.total_hours) AS total_hours,
    SUM(o.pay_amount) AS revenue
FROM `order` o
    JOIN study_room r ON o.room_id = r.id
WHERE o.status = 4
GROUP BY o.room_id, DATE(o.create_time), HOUR(o.create_time);