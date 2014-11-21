文件存储(FDS)Java SDK使用介绍
=============================
##### 编译安装sdk的jar到本地或maven仓库

    mvn clean package install

##### 把jar文件放到项目的classpath中。如果项目使用maven进行依赖管理，在项目的pom.xml文件中加入galaxy-fds-sdk-java依赖：
	
	<dependency>
    	<groupId>com.xiaomi.infra.galaxy</groupId>
    	<artifactId>galaxy-fds-sdk-java</artifactId>
    	<version>1.4-SNAPSHOT</version>
	</dependency>


FDS Java SDK User Guide
========================
##### Build from source and install the jars into local maven repository or deploy to a central repository

    mvn clean package install

##### Import the above jar into the project classpath or add the following dependency if your project is managed with maven:

    <dependency>
      <groupId>com.xiaomi.infra.galaxy</groupId>
      <artifactId>galaxy-fds-sdk-java</artifactId>
      <version>1.4-SNAPSHOT</version>
    </dependency>