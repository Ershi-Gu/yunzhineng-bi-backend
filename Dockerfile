# 指定 JDK 基础镜像
FROM openjdk:8
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 拷贝项目 jar 包
COPY bibackend-0.0.1.jar /bibackend-0.0.1.jar
# 入口
ENTRYPOINT ["java", "-jar", "/bibackend-0.0.1.jar", "--spring.profiles.active=prod"]