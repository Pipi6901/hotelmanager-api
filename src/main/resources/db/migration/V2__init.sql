insert into roles (name)
values
    ('ROLE_MANAGER');

insert into users (username, password, email)
values
    ('manager', '$2a$04$Fx/SX9.BAvtPlMyIIqqFx.hLY2Xp8nnhpzvEEVINvVpwIPbA3v/.i', 'man@gmail.com'); -- manager (100)

insert into users_roles (user_id, role_id)
values
    (3, 3);