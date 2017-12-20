DROP TABLE t_user;
CREATE TABLE t_user (
  id SERIAL PRIMARY KEY NOT NULL,
  username varchar(56) NOT NULL,
  age INTEGER DEFAULT NULL,
  password varchar(56) DEFAULT NULL,
  birthday date DEFAULT NULL,
  real_name varchar(56) DEFAULT NULL
);

INSERT INTO t_user (id, username, password, real_name, age)
VALUES
	(1, 'aaa', 'aaa', 'aaa', 32),
	(2, 'jack', '999', '杰克65', 29),
	(3, 'jack_up', '123556', 'aaa', 19),
	(44, 'jack', '123556', '杰克', 20),
	(45, 'jack', '123556', '杰克', 20);