#### 这是一个简单的微信商城web项目
主要使用SpringBoot+Shiro+MySQL+Redis来完成

1.添加依赖
```xml
<!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.21</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.mybatis.spring.boot/mybatis-spring-boot-starter -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.3</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.springframework/spring-jdbc -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.2.8.RELEASE</version>
        </dependency> 
```
2.数据库
```xml
       <plugin>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-maven-plugin</artifactId>
            <version>6.5.5</version>
       </plugin>
```
docker方式启动数据库
```dockerfile
docker run -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=wxshop -p 3306:3306 -d mysql
```

参照官方文档的使用https://flywaydb.org/documentation/maven/ 
```xml
<configuration>
        <user>myUser</user>
        <password>mySecretPwd</password>
        <url>jdbc:mysql://localhost:3306/wxshop?useSSL=false&amp;allowPublicKeyRetrieval=true</url>
</configuration>
```
创建文件夹 classpath:xxx/src/main/resources/db/migration/V1__CreateUser.sql 
注意：是两个下划线__
```sql
CREATE TABLE `USER`(
    `ID`            BIGINT   PRIMARY KEY  AUTO_INCREMENT ,
    `NAME`          VARCHAR(100) ,
    `TEL`           VARCHAR(20) UNIQUE ,
    `AVATAR_URL`    VARCHAR(1024) ,
    `ADDRESS`       VARCHAR(1024) ,
    `CREATED_AT`    TIMESTAMP NOT NULL  DEFAULT NOW() ,
    `UPDATED_AT`    TIMESTAMP NOT NULL  DEFAULT NOW()
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
```
然后使用命令行：mvn flyway:migrate 创建第一张User用户表

