#ORM
Pretty Android ORM
-----------

### 前言
我一直想找一个简单的ORM库, 一直没有找到, 于是有了这[豆豆电话本](http://www.lightim.net/dou/dou.apk)项目的副产品. 
先说说feature:
> - 自动创建表, 无需注册, 无需配置.  仅需要一行初始化Orm.init(context, dbname, dbversion);
> - 自动增加列. model类增加的成员, 自动alter数据库表.  (删除的仍然保存)
> -  无需继承任何类, 只需要使用@PrimaryKey声明一个主键即可, 主键可以是任何类型, 支持整型自增.  也可以继承一个预定义的包含整型id的类. 
> - 所有列, 默认会映射到数据库表, 也可以使用transient关键字或@Exclude排除.
> - 支持复杂字段自定义序列化, 默认支持Date, JSONObject, UUID, 等常见类的序列化支持
> - 单表查询:　Person person = Orm.findPk(Person.class, 123);
> - 性能, 复杂数据结构 每秒插入1000左右, 简单数据结构每秒插入4000左右.

### 示例

使用前, 先执行初始化工作, 一般是在Application.onCreate中
`Orm.init(context, 1);//1是版本号`
1. 模型
```
public class Person{
	@PrimaryKey(autoIncrease=true)
	public long id;
	@Column(index=true)
	public String name;
	public int age;
	JSONObject attrs;
}
```
2. 保存
```
	Person person = new Person();
	person.name="豆豆电话本";
	person.age = 5;
	person.attrs = new JSONObject();
	person.attrs.put("address", "济南");
	person.save();
	xlog.d("ID=", person.id);//1
```
3. 查询
```
	Person person = Orm.findPk(Person.class, 1);
	person.age = 10;
	person.save();
```

`	Person person = Orm.findOneEq(Person.class, "name", "豆豆电话本");`
`	Person person = Orm.findOne(Person.class, "age=?", 5);`
`	List< Person > = Orm.findAll(Person.class) ;`
`	List< Person > = Orm.findAllEq(Person.class, "name", "豆豆电话本");`
```
List< Person > = Orm.select(Person.class)
	.whereGe("age", 5).asc("name).limit(20).findAll()
```
4. 删除
`	Orm.deletePk(Person.class, 1);`
`	Orm.deleteEq(Person.class, "age", 5);`

5. 变更
如果哪天, 我们又个Person 添加了一个新字段 
` String fatherName `
没关系, 在我们使用这个类的时候, 会自动ALTER 这个表, 将fatherName字段添加进去. 是不是很性感呢.
6. 其他
预定义了IdModel类, 里面声明了一个主键_id, 和插入,更新操作. 这样, 一些简单的类就可以只需要继承IdModel即可进行增删改查了. 
####也可以使用原始的sql语句来查询.####
