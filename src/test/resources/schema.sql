create table brand (
	id 		int primary key,
	name	varchar(50) not null unique
);

create table car (
  id 		bigint auto_increment(1,1) primary key,
  brand		int not null references brand (id),
  name		varchar(255)
);

create table service (
	idcar		bigint,
	servicedate	date,
	primary key(idcar, servicedate)
	
);