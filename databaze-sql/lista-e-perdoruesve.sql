SELECT * FROM online_server_chat.users;
DELETE FROM `online_server_chat`.`users` WHERE (`id` = '5');


INSERT INTO online_server_chat.users (username, password, first_name, last_name, avatar_path, role, status, join_time, ip_address)
VALUES ('lis.gjergji', 'Lis@2024', 'Lis', 'Gjergji', 'avatars/default.png', 'user', 'offline', NOW(), NULL);

INSERT INTO online_server_chat.users 
(username, password, first_name, last_name, avatar_path, role, status, join_time, ip_address) VALUES
('zgjimi7', 'Zgjim!22', 'Zgjim', 'Kelmendi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 10 SECOND, NULL),
('bardh.rugova', 'Bardh*7A', 'Bardh', 'Rugova', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 20 SECOND, NULL),
('arbnorx', 'Arbnor/9K', 'Arbnor', 'Krasniqi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 30 SECOND, NULL),
('kreshnik.h', 'Kresh@88', 'Kreshnik', 'Hoti', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 40 SECOND, NULL),
('dardan.berisha', 'Dard!5X', 'Dardan', 'Berisha', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 50 SECOND, NULL),
('ilirian_', 'Ilir*12M', 'Ilir', 'Morina', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 1 MINUTE, NULL),
('taulant.sh', 'Taul@6Hh', 'Taulant', 'Shala', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 1 MINUTE + INTERVAL 10 SECOND, NULL),
('rron.gashi', 'Rron,31M', 'Rron', 'Gashi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 1 MINUTE + INTERVAL 20 SECOND, NULL),
('drinii__', 'Drin!77Q', 'Drin', 'Bytyçi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 1 MINUTE + INTERVAL 30 SECOND, NULL),
('fortesa.p', 'Festa*19', 'Fortesë', 'Palokaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 1 MINUTE + INTERVAL 40 SECOND, NULL),
('teuta.nik', 'Teuta!3B', 'Teuta', 'Gegaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 1 MINUTE + INTERVAL 50 SECOND, NULL),
('rozafa.k', 'Roza@5T', 'Rozafa', 'Kolaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 2 MINUTE, NULL),
('art.pr', 'Art/21R', 'Art', 'Prenkaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 2 MINUTE + INTERVAL 15 SECOND, NULL),
('rrita.gjergji', 'Rrita@21L', 'Rrita', 'Gjergji', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 2 MINUTE + INTERVAL 30 SECOND, NULL),
('furtuna.g', 'Furtuna!14', 'Furtuna', 'Gashi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 2 MINUTE + INTERVAL 45 SECOND, NULL),
('natyra_k', 'Natyra@8F', 'Natyra', 'Kurtaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 3 MINUTE, NULL),
('vese.istogu', 'Vese*12K', 'Vesë', 'Istogu', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 3 MINUTE + INTERVAL 20 SECOND, NULL),
('arberesha.j', 'Arb!90', 'Arbëresha', 'Jashari', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 3 MINUTE + INTERVAL 40 SECOND, NULL),
('lumbardh.k', 'Lumbi!7D', 'Lumbardh', 'Kastrati', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 4 MINUTE, NULL),
('krenar.u', 'Kren@15Q', 'Krenar', 'Uka', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 4 MINUTE + INTERVAL 15 SECOND, NULL),
('diell.l', 'Diell!2M', 'Diell', 'Lajçi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 4 MINUTE + INTERVAL 30 SECOND, NULL),
('rreze.k', 'rreze*11', 'Rrezarta', 'Kelmendi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 4 MINUTE + INTERVAL 45 SECOND, NULL),
('dren.b', 'dren@18', 'Dren', 'Berisha', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 5 MINUTE, NULL),
('dedvukaj.mark', 'Mark*88C', 'Mark', 'Dedvukaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 5 MINUTE + INTERVAL 15 SECOND, NULL),
('fjolla.gjergji', 'Fjolla/3X', 'Fjolla', 'Gjinaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 5 MINUTE + INTERVAL 30 SECOND, NULL),
('shkodran.n', 'Arbesa,7N', 'Shkodran', 'Nikçi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 6 MINUTE, NULL),
('era.geci', 'Era!9Y', 'Era', 'Geci', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 6 MINUTE + INTERVAL 20 SECOND, NULL),
('mark.ceta', 'Mark!11K', 'Mark', 'Çeta', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 6 MINUTE + INTERVAL 40 SECOND, NULL),
('drita.pr', 'Drita@6Z', 'Drita', 'Prelvukaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 7 MINUTE, NULL),
('bardha.l', 'Bardh!3A', 'Bardha', 'Lekaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 7 MINUTE + INTERVAL 20 SECOND, NULL),
('monun.d', 'Monun*5G', 'Monun', 'Dushaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 8 MINUTE, NULL),
('mal.kelmendi', 'Mmali/12', 'Mal', 'Kelmendi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 8 MINUTE + INTERVAL 20 SECOND, NULL),
('glauk.m', 'Glauk@88', 'Glauk', 'Marku', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 9 MINUTE, NULL),
('luan.dushku', 'Luan!77', 'Luan', 'Dushku', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 9 MINUTE + INTERVAL 30 SECOND, NULL),
('flutura.z', 'Flut*44', 'Fluturë', 'Zefi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 10 MINUTE, NULL),
('ereza.k', 'Ereza!99', 'Erëza', 'Krasniqi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 10 MINUTE + INTERVAL 30 SECOND, NULL),
('arber.gj', 'Arbër,18', 'Arbër', 'Gjonbalaj', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 11 MINUTE, NULL),
('ilirian.sh', 'Ilir@55', 'Ilirian', 'Shoshi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 11 MINUTE + INTERVAL 30 SECOND, NULL),
('yll.kelmendi', 'Yll@92Z', 'Yll', 'Kelmendi', 'avatars/default.png', 'user', 'offline', NOW() + INTERVAL 12 MINUTE, NULL);




