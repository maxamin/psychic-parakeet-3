create sequence users_id_seq start 1;
create table users (
  id integer primary key default nextval('users_id_seq'),
  username varchar(20),
  password varchar(40),
  pic_url varchar(255),
  is_admin boolean
);

create sequence posts_id_seq start 1;
create table posts(
  id integer primary key default nextval('posts_id_seq'),
  user_id integer not null,
  content text
);

insert into users (username, password, pic_url, is_admin) values (
  'cats',
  md5('meow'),
  'http://ah.novartis.com.au/verve/_resources/Companion_cat_thumbnail.gif',
  true
),(
  'dogs',
  md5('woof'),
  'http://upload.wikimedia.org/wikipedia/en/f/f9/Papillon-ears.jpg',
  false
);

insert into posts (user_id, content) values
  (1, 'cats are better than dogs, look, we even made this website!'),
  (2, 'dogs are better than cats... we will show you');
