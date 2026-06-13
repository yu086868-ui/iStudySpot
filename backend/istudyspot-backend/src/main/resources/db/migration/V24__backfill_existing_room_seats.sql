SET @room2 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 静心自习室（中关村店）' LIMIT 1);
SET @room3 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 24h自习舱（望京店）' LIMIT 1);

SET @room2_area1 := (SELECT `id` FROM `area` WHERE `room_id` = @room2 AND `name` = '阳光窗景区' LIMIT 1);
SET @room2_area2 := (SELECT `id` FROM `area` WHERE `room_id` = @room2 AND `name` = '标准学习区' LIMIT 1);
SET @room3_area1 := (SELECT `id` FROM `area` WHERE `room_id` = @room3 AND `name` = '深夜加油区' LIMIT 1);
SET @room3_area2 := (SELECT `id` FROM `area` WHERE `room_id` = @room3 AND `name` = '休息充电区' LIMIT 1);

INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`, `status`, `price_per_hour`) VALUES
    (@room2, @room2_area1, 'A01', 1, 1, 1, 1, 1, 1, 1, 1, 15.00),
    (@room2, @room2_area1, 'A02', 1, 1, 2, 1, 1, 1, 1, 1, 15.00),
    (@room2, @room2_area1, 'A03', 1, 1, 3, 1, 1, 1, 1, 1, 15.00),
    (@room2, @room2_area1, 'A04', 1, 1, 4, 1, 1, 1, 1, 1, 15.00),
    (@room2, @room2_area1, 'A05', 1, 2, 1, 1, 1, 1, 1, 1, 15.00),
    (@room2, @room2_area1, 'A06', 1, 2, 2, 1, 1, 1, 1, 1, 15.00),
    (@room2, @room2_area1, 'A07', 1, 2, 3, 1, 1, 1, 1, 1, 15.00),
    (@room2, @room2_area1, 'A08', 1, 2, 4, 1, 1, 1, 1, 1, 15.00),
    (@room2, @room2_area2, 'B01', 1, 3, 1, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'B02', 1, 3, 2, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'B03', 1, 3, 3, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'B04', 1, 3, 4, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'B05', 1, 4, 1, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'B06', 1, 4, 2, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'B07', 1, 4, 3, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'B08', 1, 4, 4, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area1, 'A01', 1, 1, 1, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area1, 'A02', 1, 1, 2, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area1, 'A03', 1, 1, 3, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area1, 'A04', 1, 1, 4, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area1, 'A05', 1, 2, 1, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area1, 'A06', 1, 2, 2, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area1, 'A07', 1, 2, 3, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area1, 'A08', 1, 2, 4, 1, 1, 0, 1, 1, 15.00),
    (@room3, @room3_area2, 'B01', 2, 3, 1, 1, 0, 0, 0, 1, 30.00),
    (@room3, @room3_area2, 'B02', 2, 3, 2, 1, 0, 0, 0, 1, 30.00),
    (@room3, @room3_area2, 'B03', 2, 3, 3, 1, 0, 0, 0, 1, 30.00),
    (@room3, @room3_area2, 'B04', 2, 3, 4, 1, 0, 0, 0, 1, 30.00),
    (@room3, @room3_area2, 'B05', 2, 4, 1, 1, 0, 0, 0, 1, 30.00),
    (@room3, @room3_area2, 'B06', 2, 4, 2, 1, 0, 0, 0, 1, 30.00),
    (@room3, @room3_area2, 'B07', 2, 4, 3, 1, 0, 0, 0, 1, 30.00),
    (@room3, @room3_area2, 'B08', 2, 4, 4, 1, 0, 0, 0, 1, 30.00);

DELETE s1
FROM `seat` s1
JOIN `seat` s2
  ON s1.`room_id` = s2.`room_id`
 AND s1.`seat_number` = s2.`seat_number`
 AND s1.`id` > s2.`id`
WHERE s1.`room_id` IN (@room2, @room3);
