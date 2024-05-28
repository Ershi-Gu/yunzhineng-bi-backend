# 云智能 BI 数据分析平台
一款集成 AI 的智能数据分析平台，皆在帮助更多的人获得数据分析的能力。

![image](https://github.com/Ershi-Gu/yunzhineng-bi-backend/assets/102850729/7b101e49-c7a3-41d6-b00e-92e11c831011)


## 项目导航
快速体验地址：http://bi.guershi.cn </br>

开发文档：https://www.yuque.com/yuqueyonghu8cmbhk/ub4dfv/kcg42e4tr20brf2x?singleDoc#

## 项目介绍
相较于传统的数据分析平台，云智能 BI 可以为你提供快速便捷的数据分析。 </br>

您只需要输入分析目标 => 选择图表类型 => 上传原始数据，最后等待即可获得分析结果与相应图表

## 系统架构
项目使用消息队列完成任务处理，异步的操作使得用户体验感大大增强. </br>

![image](https://github.com/Ershi-Gu/yunzhineng-bi-backend/assets/102850729/be476911-1125-427e-9425-23934e824d2c)

## 后端技术选型
1. Spring Boot（万用 Java 后端项目模板，快速搭建基础框架）
2. Mysql 数据库
3. MyBatis Plus
4. 消息队列（RabbitMQ）
5. AI 能力（Open AI 接口开发 / 星球提供现成的 AI 接口）
6. Excel 的上传和数据分析（Easy Excel）
7. Swagger + Knife4j 项目接口文档
8. Hutool 工具库
