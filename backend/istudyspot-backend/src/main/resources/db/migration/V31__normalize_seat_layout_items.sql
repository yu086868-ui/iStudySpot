-- Reposition non-seat layout items so they no longer overlap seat cells.
-- Seats and their coordinates stay untouched.

UPDATE `seat_layout_item`
SET `row_num` = 6,
    `col_num` = 7,
    `width_units` = 4,
    `height_units` = 1
WHERE `room_id` = 3
  AND `item_key` = 'room3-table-c';

UPDATE `seat_layout_item`
SET `row_num` = 2,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 2
WHERE `room_id` = 4
  AND `item_key` = 'room4-table-b';

UPDATE `seat_layout_item`
SET `row_num` = 5,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 1
WHERE `room_id` = 4
  AND `item_key` = 'room4-zone-vip2';

UPDATE `seat_layout_item`
SET `row_num` = 8,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 1
WHERE `room_id` = 4
  AND `item_key` = 'room4-zone-booth';

UPDATE `seat_layout_item`
SET `row_num` = 2,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 2
WHERE `room_id` = 5
  AND `item_key` = 'room5-table-b';

UPDATE `seat_layout_item`
SET `row_num` = 5,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 1
WHERE `room_id` = 5
  AND `item_key` = 'room5-zone-vip2';

UPDATE `seat_layout_item`
SET `row_num` = 9,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 1
WHERE `room_id` = 5
  AND `item_key` = 'room5-window-bottom';

UPDATE `seat_layout_item`
SET `row_num` = 2,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 2
WHERE `room_id` = 6
  AND `item_key` = 'room6-table-b';

UPDATE `seat_layout_item`
SET `row_num` = 8,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 1
WHERE `room_id` = 6
  AND `item_key` = 'room6-zone-rest';

UPDATE `seat_layout_item`
SET `row_num` = 2,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 2
WHERE `room_id` = 7
  AND `item_key` = 'room7-table-b';

UPDATE `seat_layout_item`
SET `row_num` = 5,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 1
WHERE `room_id` = 7
  AND `item_key` = 'room7-zone-vip';

UPDATE `seat_layout_item`
SET `row_num` = 8,
    `col_num` = 8,
    `width_units` = 2,
    `height_units` = 1
WHERE `room_id` = 7
  AND `item_key` = 'room7-lounge';
