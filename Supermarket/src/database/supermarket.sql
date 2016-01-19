/* create if necessary */
create database supermarket;

/* use if necessary */
use supermarket;

/* drop if needed */
/*
drop table trade;
drop table employee;
drop table user;
drop table good;
*/

/* customer table */
CREATE TABLE user(
id VARCHAR(8) NOT NULL PRIMARY KEY,
level int(10) NOT NULL
);

/* Employee table */
CREATE TABLE employee(
id VARCHAR(8) NOT NULL PRIMARY KEY,
password VARCHAR(20) NOT NULL,
level int(10) NOT NULL,
online int(1) NOT NULL
);

/* Trade table */
CREATE TABLE Trade(
id VARCHAR(8) NOT NULL REFERENCES user(id),
time VARCHAR(50) NOT NULL,
money decimal(10,2),
PRIMARY KEY(id,time)
);

/* Goods table */
CREATE TABLE good(
id VARCHAR(16) NOT NULL,
name VARCHAR(200),
count int(10) NOT NULL,
price decimal(10,2),
PRIMARY KEY(id)
);

/* insert values */
insert into good values('1009', '你猜什么名字',20, 20.70);
insert into good values('1111', '娃哈哈乳饮料',981, 1.50);
insert into good values('1234', '张君雅小妹妹巧克力甜甜圈',23, 6.90);
insert into good values('1834', '西兰固体清香剂（兰花香型）', 25, 4.70);
insert into good values('2037', '好奇宝宝清爽洁净婴儿湿巾', 22, 3.20);
insert into good values('2220', '光明纯牛奶250ml', 24, 3.00);
insert into good values('2333', '云南白药牙膏（留兰香型）', 50, 25.70);
insert into good values('2345', '奥利奥饼干巧克棒威化巧克力味', 74, 19.90);
insert into good values('2463', '超级太空机械战警', 78, 78.20);
insert into good values('2982', '迪孚婴儿摇铃', 15, 10.10);
insert into good values('2993', '汇乐校园巴士', 38, 21.70);
insert into good values('3333', '好吃的东西', 66, 8.90);
insert into good values('4567', '德国进口牧牌全脂纯牛奶', 48, 99.90);
insert into good values('4985', '迪孚小熊不倒翁', 15, 222.90);
insert into good values('5818', '软件工程 - 实践者的研究方法', 7, 79.00);
insert into good values('6897', '汤达人酸酸辣辣豚骨面', 29, 4.90);
insert into good values('6980', '迪孚多功能浴椅', 13, 21.40);
insert into good values('7078', '曼可顿原味松软蛋糕', 48, 2.80);
insert into good values('7420', '六神喷雾花露水', 30, 15.60);
insert into good values('8095', '亲亲宝贝套铃', 21, 15.80);
insert into good values('9716', '妙脆角（甜辣双番味）', 33, 3.50);
insert into good values('9829', '职人咖啡', 12, 3.50);

insert into employee values('101', '101', 1, 0);
insert into employee values('102', '102', 1, 0);
insert into employee values('103', '103', 1, 0);
insert into employee values('201', '201', 2, 0);
insert into employee values('202', '202', 2, 0);
insert into employee values('301', '301', 3, 0);

insert into user values('801', 10);
insert into user values('802', 8);
insert into user values('803', 9);
insert into user values('804', 10);

/* test 
select * from good;
*/