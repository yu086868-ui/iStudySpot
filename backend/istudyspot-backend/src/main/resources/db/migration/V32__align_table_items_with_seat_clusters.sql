-- Align table blocks with existing seat clusters.
-- Seats themselves stay unchanged; only table geometry is adjusted.

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 1, `width_units` = 4, `height_units` = 2
WHERE `room_id` = 2 AND `item_key` = 'room2-table-a';

UPDATE `seat_layout_item`
SET `row_num` = 5, `col_num` = 7, `width_units` = 6, `height_units` = 3
WHERE `room_id` = 2 AND `item_key` = 'room2-table-c';

UPDATE `seat_layout_item`
SET `row_num` = 8, `col_num` = 6, `width_units` = 6, `height_units` = 2
WHERE `room_id` = 2 AND `item_key` = 'room2-table-d';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 1, `width_units` = 4, `height_units` = 3
WHERE `room_id` = 3 AND `item_key` = 'room3-table-a';

UPDATE `seat_layout_item`
SET `row_num` = 7, `col_num` = 7, `width_units` = 4, `height_units` = 3
WHERE `room_id` = 3 AND `item_key` = 'room3-table-c';

UPDATE `seat_layout_item`
SET `row_num` = 10, `col_num` = 7, `width_units` = 2, `height_units` = 1
WHERE `room_id` = 3 AND `item_key` = 'room3-table-pc';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 1, `width_units` = 2, `height_units` = 8
WHERE `room_id` = 4 AND `item_key` = 'room4-table-a';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 6, `width_units` = 2, `height_units` = 7
WHERE `room_id` = 4 AND `item_key` = 'room4-table-b';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 1, `width_units` = 2, `height_units` = 5
WHERE `room_id` = 5 AND `item_key` = 'room5-table-a';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 6, `width_units` = 2, `height_units` = 7
WHERE `room_id` = 5 AND `item_key` = 'room5-table-b';

UPDATE `seat_layout_item`
SET `row_num` = 8, `col_num` = 1, `width_units` = 2, `height_units` = 3
WHERE `room_id` = 5 AND `item_key` = 'room5-table-c';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 1, `width_units` = 2, `height_units` = 4
WHERE `room_id` = 6 AND `item_key` = 'room6-table-a';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 6, `width_units` = 2, `height_units` = 4
WHERE `room_id` = 6 AND `item_key` = 'room6-table-b';

UPDATE `seat_layout_item`
SET `row_num` = 8, `col_num` = 1, `width_units` = 2, `height_units` = 3
WHERE `room_id` = 6 AND `item_key` = 'room6-table-c';

UPDATE `seat_layout_item`
SET `row_num` = 8, `col_num` = 6, `width_units` = 2, `height_units` = 2
WHERE `room_id` = 6 AND `item_key` = 'room6-table-pc';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 1, `width_units` = 2, `height_units` = 8
WHERE `room_id` = 7 AND `item_key` = 'room7-table-a';

UPDATE `seat_layout_item`
SET `row_num` = 3, `col_num` = 6, `width_units` = 2, `height_units` = 4
WHERE `room_id` = 7 AND `item_key` = 'room7-table-b';

UPDATE `seat_layout_item`
SET `row_num` = 8, `col_num` = 6, `width_units` = 2, `height_units` = 2
WHERE `room_id` = 7 AND `item_key` = 'room7-table-group';
