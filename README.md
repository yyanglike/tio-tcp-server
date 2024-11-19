mvn archetype:generate -DgroupId=com.example -DartifactId=tio-tcp-server -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false




https://www.tiocloud.com/1/blog/1415476075424784384?type=screen-category


 java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar target/tio-tcp-server-1.0-SNAPSHOT.jar 
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar your-application.jar

mvn clean package -Pnative


查询端口号被占用的进程
lsof -i TCP:8080
ss -ltnp | grep 8080


## 使用ChronicleMap的时候需要增加下面的启动选项。
--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED
--add-exports=java.base/sun.nio.ch=ALL-UNNAMED
--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
--add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED
--add-opens=java.base/java.io=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED