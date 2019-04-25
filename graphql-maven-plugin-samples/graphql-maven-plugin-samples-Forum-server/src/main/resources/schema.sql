
create table member (
	id varchar(255) not null,
	name varchar(255) not null,
	alias varchar(255),
	email varchar(255) not null,
	type varchar(255),
	primary key (id)
);

create table board (
	id varchar(255) not null,
	name varchar(255) not null,
	publiclyAvailable boolean,
	primary key (id)
);

create table topic (
	id varchar(255) not null,
	board_id varchar(255) not null,
	date datetime not null,
	author_id varchar(255),
	publiclyAvailable boolean,
	nbPost int,
	title varchar(255) not null,
	content varchar(255) not null,
	primary key (id)
);

create table post (
	id varchar(255) not null,
	date datetime not null,
	author_id varchar(255),
	publiclyAvailable boolean,
	title varchar(255) not null,
	content varchar(255) not null,
	primary key (id)
);

alter table topic 
   add constraint topic_fk_board
   foreign key (board_id) 
   references board
;
alter table topic 
   add constraint topic_fk_member
   foreign key (author_id) 
   references member
;
alter table post 
   add constraint post_fk_member
   foreign key (author_id) 
   references member
;
