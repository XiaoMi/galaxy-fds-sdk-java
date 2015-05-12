# Galaxy FDS Java SDK User Guide

## Installing

1. Download source codes from github ,build and install the jars into local maven repository (or deploy to a central Maven repository).
github link: [https://github.com/XiaoMi/galaxy-fds-sdk-java.git]()

`mvn clean package install`

2. If use Maven to management, users can download the latest jar for maven central repository.The latest version is 1.5.0.

## Usage

1. Import the above jars into the project classpath or add the following dependency if your project is managed with Maven.

```
    <dependency>
      	<groupId>com.xiaomi.infra.galaxy</groupId>
    		<artifactId>galaxy-fds-sdk-java</artifactId>
    		<version>1.5.0</version>
    </dependency>
```

2. Add the jar to classpath.

