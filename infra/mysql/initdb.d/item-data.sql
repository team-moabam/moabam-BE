insert into item (type, category, name, awake_image, sleep_image, unlock_level, created_at)
values ('MORNING', 'SKIN', '오목눈이 알', 'https://image.moabam.com/moabam/skins/omok/default/egg.png',
        'https://image.moabam.com/moabam/skins/omok/default/egg.png', 0, current_time());

insert into item (type, category, name, awake_image, sleep_image, unlock_level, created_at)
values ('NIGHT', 'SKIN', '부엉이 알', 'https://image.moabam.com/moabam/skins/owl/default/egg.png',
        'https://image.moabam.com/moabam/skins/owl/default/egg.png', 0, current_time());

insert into item (type, category, name, awake_image, sleep_image, unlock_level, created_at)
values ('MORNING', 'SKIN', '오목눈이', 'https://image.moabam.com/moabam/skins/omok/default/eyes-opened.png',
        'https://image.moabam.com/moabam/skins/omok/default/eyes-closed.png', 1, current_time());

insert into item (type, category, name, awake_image, sleep_image, unlock_level, created_at)
values ('NIGHT', 'SKIN', '부엉이', 'https://image.moabam.com/moabam/skins/owl/default/eyes-opened.png',
        'https://image.moabam.com/moabam/skins/owl/default/eyes-closed.png', 1, current_time());

insert into item (type, category, name, awake_image, sleep_image, bug_price, golden_bug_price, unlock_level, created_at)
values ('MORNING', 'SKIN', '안경 오목눈이', 'https://image.moabam.com/moabam/skins/omok/glasses/eyes-opened.png',
        'https://image.moabam.com/moabam/skins/omok/glasses/eyes-closed.png', 10, 5, 5, current_time());

insert into item (type, category, name, awake_image, sleep_image, bug_price, golden_bug_price, unlock_level, created_at)
values ('NIGHT', 'SKIN', '안경 부엉이', 'https://image.moabam.com/moabam/skins/owl/glasses/eyes-opened.png',
        'https://image.moabam.com/moabam/skins/owl/glasses/eyes-closed.png', 10, 5, 5, current_time());

insert into item (type, category, name, awake_image, sleep_image, bug_price, golden_bug_price, unlock_level, created_at)
values ('MORNING', 'SKIN', '목도리 오목눈이', 'https://image.moabam.com/moabam/skins/omok/scarf/eyes-opened.png',
        'https://image.moabam.com/moabam/skins/omok/scarf/eyes-closed.png', 20, 10, 10, current_time());

insert into item (type, category, name, awake_image, sleep_image, bug_price, golden_bug_price, unlock_level, created_at)
values ('NIGHT', 'SKIN', '목도리 부엉이', 'https://image.moabam.com/moabam/skins/owl/scarf/eyes-opened.png',
        'https://image.moabam.com/moabam/skins/owl/scarf/eyes-closed.png', 20, 10, 10, current_time());

insert into item (type, category, name, awake_image, sleep_image, bug_price, golden_bug_price, unlock_level, created_at)
values ('MORNING', 'SKIN', '산타 오목눈이', 'https://image.moabam.com/moabam/skins/omok/santa/eyes-opened.png',
        'https://image.moabam.com/moabam/skins/omok/santa/eyes-closed.png', 30, 15, 15, current_time());

insert into item (type, category, name, awake_image, sleep_image, bug_price, golden_bug_price, unlock_level, created_at)
values ('NIGHT', 'SKIN', '산타 부엉이', 'https://image.moabam.com/moabam/skins/owl/santa/eyes-opened.png',
        'https://image.moabam.com/moabam/skins/owl/santa/eyes-closed.png', 30, 15, 15, current_time());

insert into product (type, name, price, quantity, created_at)
values ('BUG', '황금벌레 5', 3000, 5, current_time());

insert into product (type, name, price, quantity, created_at)
values ('BUG', '황금벌레 15', 7000, 15, current_time());

insert into product (type, name, price, quantity, created_at)
values ('BUG', '황금벌레 25', 9900, 25, current_time());
