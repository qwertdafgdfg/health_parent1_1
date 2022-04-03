# health_parent1_1
如有侵权请联系删除
## 本项目基于SSM+Zookeeper+Dubbo+Spring Security技术栈
![image](https://user-images.githubusercontent.com/32232106/161420988-e3167b91-28b5-414e-b90c-6cbc84f871c5.png)

## 项目结构
* 项目代码的解构
* 1)health_common：通用模块，打包方式为jar，存放项目中使用到的一些工具类、实体类、返回结果和常量类
* 
* 2)health_interface：打包方式为jar，存放服务接口
* 
* 3)health_service_provider：Dubbo服务模块，打包方式为war，存放服务实现类、Dao接口、Mapper映射文件等，作为服务提供方，需要部署到tomcat运行
* 
* 4)health_backend：传智健康管理后台，打包方式为war，作为Dubbo服务消费方，存放Controller、HTML页面、js、css、spring配置文件等，需要部署到tomcat运行
* 
* 5) health_mobile：移动端前台，打包方式为war，作为Dubbo服务消费方，存放Controller、HTML页面、js、css、spring配置文件等，需要部署到tomcat运

总结:
	两个服务消费者,一个服务提供者,一个公共模块,一个公共接口
*  maven项目搭建
*   创建health_parent，父工程，打包方式为pom，用于统一管理依赖版本
* 	注: 父工程的意义在于1)约束子工程的jar 版本  (dependencyManagement 只是约束,不导包)
* 	    			2) 可以对所有子工程进行统一编译打包
