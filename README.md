### 这是一个简单的微信商城web项目
以下是这个项目完整的构建过程(自己太懒了，以后每次提交都要带着README,这里立个Flag ⛽️ 💪 )

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
---
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

---
3.Mybatis Generator

依照官方文档：https://mybatis.org/generator/running/runningWithMaven.html
```xml
        <plugin>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-maven-plugin</artifactId>
            <version>1.4.0</version>
        </plugin>   
```
创建application.yml配置文件和generatorConfig.xml。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
    <context id="wxshop" targetRuntime="MyBatis3">
        <jdbcConnection driverClass="com.mysql.cj.jdbc.Driver"
                        connectionURL="jdbc:mysql://localhost:3306/wxshop?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;characterEncoding=utf-8"
                        userId="root"
                        password="root">
        </jdbcConnection>

        <javaModelGenerator targetPackage="com.github.kb.wxshop.generate" targetProject="src/main/java">
            <property name="enableSubPackages" value="true" />
            <property name="trimStrings" value="true" />
        </javaModelGenerator>

        <sqlMapGenerator targetPackage="db.mybatis"  targetProject="src/main/resources">
            <property name="enableSubPackages" value="true" />
        </sqlMapGenerator>

        <javaClientGenerator type="XMLMAPPER" targetPackage="com.github.kb.wxshop.generate"  targetProject="src/main/java">
            <property name="enableSubPackages" value="true" />
        </javaClientGenerator>

        <table schema="wxshop" tableName="USER" domainObjectName="User" >
            <property name="useActualColumnNames" value="false"/>
            <generatedKey column="ID" sqlStatement="MySql" identity="true" />
            <columnOverride column="AVATAR_URL" property="avatarUrl" />
            <columnOverride column="CREATED_AT" jdbcType="timestamp" />
            <columnOverride column="UPDATED_AT" jdbcType="timestamp" />
        </table>

    </context>
</generatorConfiguration>
```

```xml
Failed to execute goal org.mybatis.generator:mybatis-generator-maven-plugin:1.4.0:generate (default-cli) on project wxshop:
Execution default-cli of goal org.mybatis.generator:mybatis-generator-maven-plugin:1.4.0:generate failed: 
Exception getting JDBC Driver: com.mysql.cj.jdbc.Driver -> [Help 1]
```
因为插件中并没有mysql的依赖，pom中的依赖是给项目用的，并没有起作用到插件中，所以要单独给mybatis-generator添加mysql依赖
```xml
        <plugin>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-maven-plugin</artifactId>
            <version>1.4.0</version>
              <dependencies>
                 <dependency>
                     <groupId>mysql</groupId>
                     <artifactId>mysql-connector-java</artifactId>
                     <version>8.0.21</version>
                 </dependency>
              </dependencies>
        </plugin>
```
命令: mvn mybatis-generator:generate
