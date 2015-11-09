#ORM
Pretty Android ORM
-----------

### 前言
我一直想找一个简单的ORM库, 一直没有找到, 于是有了这个项目. 先说说feature:
> - 自动创建表, 无需注册, 无需配置.  仅需要一行初始化Orm.init(context, dbname, dbversion);
> - 自动增加列. model类增加的成员, 自动alter数据库表.  (删除的仍然保存)
> -  无需继承任何类, 只需要使用@PrimaryKey声明一个主键即可, 主键可以是任何类型, 支持整型自增.  也可以继承一个预定义的包含整型id的类. 
> - 所有列, 默认会映射到数据库表, 也可以使用transient关键字或@Exclude排除.
> - 支持复杂字段自定义序列化, 默认支持Date, JSONObject, UUID, 等常见类的序列化支持
> - 单表查询:　Person person = Orm.findPk(Person.class, 123);
> - 性能, 复杂数据结构 每秒插入1000左右, 简单数据结构每秒插入4000左右.

