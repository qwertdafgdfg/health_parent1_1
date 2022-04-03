## 01-本章内容介绍

- 在项目中应用Spring Security
- 图形报表ECharts
- 会员数量折线图



## 02-在项目中应用Spring Security（导入权限相关数据）

数据库表结构解析：

t_role角色表 ：

​	keyword:这个字段标识spring security中的role概念



t_user用户表：

​	password:这个字段是由密码经过加密之后的字符串



t_permission权限表：

​	keyword：这个字段是spring security中的权限概念



## 03-在项目中应用Spring Security（导入Spring Security环境）

第一步：在health_parent父工程的pom.xml中导入Spring Security的maven坐标

```xml
<dependency>
<groupId>org.springframework.security</groupId>
<artifactId>spring‐security‐web</artifactId>
<version>${spring.security.version}</version>
</dependency>
<dependency>
<groupId>org.springframework.security</groupId>
<artifactId>spring‐security‐config</artifactId>
<version>${spring.security.version}</version>
</dependency>
```

第二步：在health_backend工程的web.xml文件中配置用于整合Spring Security框架的
过滤器DelegatingFilterProxy

```xml
<!‐‐委派过滤器，用于整合其他框架‐‐>
<filter>
<!‐‐整合spring security时，此过滤器的名称固定springSecurityFilterChain‐‐>
<filter‐name>springSecurityFilterChain</filter‐name>
<filterclass>
org.springframework.web.filter.DelegatingFilterProxy</filter‐class>
</filter>
<filter‐mapping>
<filter‐name>springSecurityFilterChain</filter‐name>
<url‐pattern>/*</url‐pattern>
</filter‐mapping>
```

>springSecurityFilterChain名称不能更改



## 04-在项目中应用Spring Security_实现认证和授权（SpringSecurityUserService类编写）

第一步：在health_backend工程中按照Spring Security框架要求提供
SpringSecurityUserService，并且实现UserDetailsService接口

```java
package com.itheima.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import com.itheima.pojo.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SpringSecurityUserService implements UserDetailsService {
    //使用dubbo通过网络远程调用服务提供方获取数据库中的用户信息
    @Reference
    private UserService userService;

    //根据用户名查询数据库获取用户信息
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if(user == null){
            //用户名不存在
            return null;
        }

        List<GrantedAuthority> list = new ArrayList<>();

        //动态为当前用户授权
        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            //遍历角色集合，为用户授予角色
            list.add(new SimpleGrantedAuthority(role.getKeyword()));
            Set<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                //遍历权限集合，为用户授权
                list.add(new SimpleGrantedAuthority(permission.getKeyword()));
            }
        }

        org.springframework.security.core.userdetails.User securityUser = new org.springframework.security.core.userdetails.User(username,user.getPassword(),list);
        return securityUser;
    }
}

```

>与昨天的例子通过配置文件注入不同的是，这个service通过@Component注解注入到spring容器中
>
>用户信息通过dubbo接口来获取
>
>获取用户信息的时候，会同时获取到角色、权限数据



创建UserService服务接口

```java

package com.itheima.service;

import com.itheima.pojo.User;

public interface UserService {
    public User findByUsername(String username);
}

```



## 05-在项目中应用Spring Security_实现认证和授权（用户服务、DAO接口）

创建UserService服务实现类、Dao接口、Mapper映射文件等

```java
package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.PermissionDao;
import com.itheima.dao.RoleDao;
import com.itheima.dao.UserDao;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

/**
 * 用户服务
 */
@Service(interfaceClass = UserService.class)
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private PermissionDao permissionDao;
    //根据用户名查询数据库获取用户信息和关联的角色信息，同时需要查询角色关联的权限信息
    public User findByUsername(String username) {
        User user = userDao.findByUsername(username);//查询用户基本信息，不包含用户的角色
        if(user == null){
            return null;
        }
        Integer userId = user.getId();
        //根据用户ID查询对应的角色
        // SELECT *FROM  t_role WHERE id IN(SELECT role_id FROM t_user_role WHERE user_id='1')

    //SELECT	* FROM t_permission WHERE id IN (SELECT permission_id FROM t_role_permission WHERE role_id='1' )
        Set<Role> roles = roleDao.findByUserId(userId);
        user.setRoles(roles);//让用户关联角色
        for (Role role : roles) {
            Integer roleId = role.getId();
            //根据角色ID查询关联的权限
            Set<Permission> permissions = permissionDao.findByRoleId(roleId);
            role.setPermissions(permissions);//让角色关联权限
        }
        
        return user;
    }
}
```

>这里没有通过关联关系查，而是分步骤进行查询：
>
>查出user数据
>
>根据userId查询角色信息
>
>遍历角色信息，获取权限信息

```
权限表关系?
	ueser: 增删该
	menu : 菜单 增删改 
	角色 : 菜单 增删改 不能写死
	权限:  
			1) 权限表开发阶段定义的,写死代码用的,一般不变
			2) 角色是权限的集合
```



UserDao:

```java
package com.itheima.dao;

import com.itheima.pojo.User;

public interface UserDao {
    public User findByUsername(String username);
}

```



RoleDao:

```java
package com.itheima.dao;

import com.itheima.pojo.Role;

import java.util.Set;

public interface RoleDao {
    public Set<Role> findByUserId(Integer userId);
}

```



PermissionDao:

```java
package com.itheima.dao;

import com.itheima.pojo.Permission;

import java.util.Set;

public interface PermissionDao {
    public Set<Permission> findByRoleId(Integer roleId);
}

```

>角色-用户 是多对多关系，所以返回数据用set来包装
>
>同理角色和权限也是



## 06-在项目中应用Spring Security_实现认证和授权（Mapper映射文件编写）

UserDao.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.itheima.dao.UserDao">
    <select id="findByUsername" parameterType="string" resultType="com.itheima.pojo.User">
        select * from t_user where username = #{username}
    </select>
</mapper>
```



RoleDao.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.itheima.dao.RoleDao">
    <!--根据用户ID查询关联的角色-->
    <select id="findByUserId" parameterType="int" resultType="com.itheima.pojo.Role">
        select r.*
          from t_role r,t_user_role ur
          where r.id = ur.role_id and ur.user_id = #{user_id}
    </select>
</mapper>
```

>此sql写法与inner join效果一样



PermissionDao.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.itheima.dao.PermissionDao">
    <!--根据角色ID查询关联的权限-->
    <select id="findByRoleId" parameterType="int" resultType="com.itheima.pojo.Permission">
        select p.*
          from t_permission p,t_role_permission rp
          where p.id = rp.permission_id and rp.role_id = #{role_id}
    </select>
</mapper>
```



## 07-在项目中应用Spring Security_实现认证和授权（修改dubbo包扫描）

修改health_backend工程中的springmvc.xml文件，修改dubbo批量扫描的包
路径

```xml
<!--批量扫描-->
<dubbo:annotation package="com.itheima" />


<dubbo:annotation package="com.itheima.controller" />
<dubbo:annotation package="com.itheima.service" />
```

> 注意：此处原来扫描的包为com.itheima.controller，现在改为com.itheima包的目的是
> 需要将我们上面定义的SpringSecurityUserService也扫描到，因为在
> SpringSecurityUserService的loadUserByUsername方法中需要通过dubbo远程调用名
> 称为UserService的服务。



## 08-在项目中应用Spring Security_实现认证和授权（提供Spring Security相关xml配置）

在springmvc.xml中添加

```xml
 <import resource="spring-security.xml"></import>
```

在health_backend工程中提供spring-security.xml配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:security="http://www.springframework.org/schema/security"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
						http://www.springframework.org/schema/beans/spring-beans.xsd
						http://www.springframework.org/schema/mvc
						http://www.springframework.org/schema/mvc/spring-mvc.xsd
						http://code.alibabatech.com/schema/dubbo
						http://code.alibabatech.com/schema/dubbo/dubbo.xsd
						http://www.springframework.org/schema/context
						http://www.springframework.org/schema/context/spring-context.xsd
                     http://www.springframework.org/schema/security
                     http://www.springframework.org/schema/security/spring-security.xsd">

    <!--配置哪些资源匿名可以访问（不登录也可以访问）-->
    <!--<security:http security="none" pattern="/pages/a.html"></security:http>
    <security:http security="none" pattern="/pages/b.html"></security:http>-->
    <!--<security:http security="none" pattern="/pages/**"></security:http>-->
    <security:http security="none" pattern="/login.html"></security:http>
    <security:http security="none" pattern="/css/**"></security:http>
    <security:http security="none" pattern="/img/**"></security:http>
    <security:http security="none" pattern="/js/**"></security:http>
    <security:http security="none" pattern="/plugins/**"></security:http>
    <!--
        auto-config:自动配置，如果设置为true，表示自动应用一些默认配置，比如框架会提供一个默认的登录页面
        use-expressions:是否使用spring security提供的表达式来描述权限
    -->
    <security:http auto-config="true" use-expressions="true">
        <security:headers>
            <!--设置在页面可以通过iframe访问受保护的页面，默认为不允许访问-->
            <security:frame-options policy="SAMEORIGIN"></security:frame-options>
        </security:headers>
        <!--配置拦截规则，/** 表示拦截所有请求-->
        <!--
            pattern:描述拦截规则
            asscess:指定所需的访问角色或者访问权限
        -->
        <!--只要认证通过就可以访问-->
        <security:intercept-url pattern="/pages/**"  access="isAuthenticated()" />

        <!--如果我们要使用自己指定的页面作为登录页面，必须配置登录表单.页面提交的登录表单请求是由框架负责处理-->
        <!--
            login-page:指定登录页面访问URL
        -->
        <security:form-login
                login-page="/login.html"
                username-parameter="username"
                password-parameter="password"
                login-processing-url="/login.do"
                default-target-url="/pages/main.html"
                authentication-failure-url="/login.html"></security:form-login>

        <!--
          csrf：对应CsrfFilter过滤器
          disabled：是否启用CsrfFilter过滤器，如果使用自定义登录页面需要关闭此项，否则登录操作会被禁用（403）
        -->
        <security:csrf disabled="true"></security:csrf>

        <!--
          logout：退出登录
          logout-url：退出登录操作对应的请求路径
          logout-success-url：退出登录后的跳转页面
        -->
        <security:logout logout-url="/logout.do"
                         logout-success-url="/login.html" invalidate-session="true"/>

    </security:http>

    <!--配置认证管理器-->
    <security:authentication-manager>
        <!--配置认证提供者-->
        <security:authentication-provider user-service-ref="springSecurityUserService">
            <!--
                    配置一个具体的用户，后期需要从数据库查询用户
            <security:user-service>
                <security:user name="admin" password="{noop}1234" authorities="ROLE_ADMIN"/>
            </security:user-service>
            -->
            <!--指定度密码进行加密的对象-->
            <security:password-encoder ref="passwordEncoder"></security:password-encoder>
        </security:authentication-provider>
    </security:authentication-manager>

    <!--配置密码加密对象-->
    <bean id="passwordEncoder"
          class="org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder" />

    <!--开启注解方式权限控制-->
    <security:global-method-security pre-post-annotations="enabled" />
</beans>
```

>以下资源需要放行：
>
>```xml
>	<security:http security="none" pattern="/login.html"></security:http>
>    <security:http security="none" pattern="/css/**"></security:http>
>    <security:http security="none" pattern="/img/**"></security:http>
>    <security:http security="none" pattern="/js/**"></security:http>
>    <security:http security="none" pattern="/plugins/**"></security:http>
>```

所有页面认证通过就能访问，细粒度的权限控制在接口层controller中通过注解方式做：

```xml
		 <!--只要认证通过就可以访问-->
        <security:intercept-url pattern="/pages/**"  access="isAuthenticated()" />
```

登录完之后的首页更改为：

```xml
/pages/main.html
```

认证管理器就不需要注入bean了，只需要进行应用，名称为类名首字母小写

```xml
 <security:authentication-provider user-service-ref="springSecurityUserService">
```



## 09-在项目中应用Spring Security_实现认证和授权（配置允许通过iframe访问受保护的页面）

数据库中的密码有误，admin密码应该为：

admin/admin

>$2a$10$QjWSVpt35GMwq9sPSb6beu2Ctp5o5iQyZ/kbpHJlgkA.rpkEqtfLS



在<security:http auto-config="true" use-expressions="true">中添加

```xml
<security:headers>
            <!--设置在页面可以通过iframe访问受保护的页面，默认为不允许访问-->
            <security:frame-options policy="SAMEORIGIN"></security:frame-options>
        </security:headers>
```

>由于页面框架右边是使用iframe进行内嵌页面展示，需要添加策略，默认为DENY
>
> DENY：浏览器拒绝当前页面加载任何Frame页面
>
> SAMEORIGIN：frame页面的地址只能为同源域名下的页面
>
> ALLOW-FROM：origin为允许frame加载的页面地址。必须配置strategy属性和value属性。否则项目启动报错。
>
>关闭策略：
>
> <security:frame-options disabled="true"/>



## 10-在项目中应用Spring Security_实现认证和授权（为Controller方法加入权限注解实现方法级别权限控制）

在CheckItemController中更改代码：

```java
    //删除检查项
    @PreAuthorize("hasAuthority('CHECKITEM_DELETE')")//权限校验
    @RequestMapping("/delete")
    public Result delete(Integer id){
        try{
            checkItemService.deleteById(id);
        }catch (Exception e){
            e.printStackTrace();
            //服务调用失败
            return new Result(false, MessageConstant.DELETE_CHECKITEM_FAIL);
        }
        return  new Result(true, MessageConstant.DELETE_CHECKITEM_SUCCESS);
    }
```



在数据库中将t_role_permission表中role_id是2，permission_id是2的数据删除

使用admin/admin登录之后，删除检查项时不会报没有权限的提示

使用xiaoming/1234登录，删除检查项会没有提示，因为我们还没有把没有权限的错误返回到前端进行展示



## 11-在项目中应用Spring Security_实现认证和授权（调整页面显示权限不足提示信息）

返回非200的返回码时，需要进行catch处理

```javascript
             // 删除
                handleDelete(row) {//row其实是一个json对象，json对象的结构为{"age":"0-100","attention":"无","code":"0011","id":38,"name":"白细胞计数","price":10.0,"remark":"白细胞计数","sex":"0","type":"2"}
                    //alert(row.id);
                    this.$confirm("你确定要删除当前数据吗？","提示",{//确认框
                        type:'warning'
                    }).then(()=>{
                        //用户点击确定按钮，发送ajax请求，将检查项ID提交到Controller进行处理
                        axios.get("/checkitem/delete.do?id=" + row.id).then((res) => {
                            if(res.data.flag){
                                //执行成功
                                //弹出成功提示信息
                                this.$message({
                                    type:'success',
                                    message:res.data.message
                                });
                                //重新进行分页查询
                                this.findPage();
                            }else{
                                //执行失败
                                this.$message.error(res.data.message);
                            }
                        }).catch((r)=>{
                            this.showMessage(r);
                        });
                    }).catch(()=>{
                        this.$message({
                            type:'info',
                            message:'操作已取消'
                        });
                    });
                }
```



我们可以封装一个showMessage方法进行复用

```javascript
 				showMessage(r){
                    if(r == 'Error: Request failed with status code 403'){
                        //权限不足
                        this.$message.error('无访问权限');
                        return;
                    }else{
                        this.$message.error('未知错误');
                        return;
                    }
                },
```



备注：其实这种方式不是特别规范，如果将来spring security框架返回的信息变化了，就无法触发无访问权限。

```javascript


showMessage(r){
                    if(r.response.status == 403){
                        //权限不足
                        this.$message.error('无访问权限');
                        return;
                    }else{
                        this.$message.error('未知错误');
                        return;
                    }
                },
```

使用http返回码进行判断，如果是403就代表无访问权限



## 12-在项目中应用Spring Security_实现认证和授权（登录成功后在页面展示用户名）

如果用户认证成功后需要在页面展示当前用户的用户名。Spring Security在认证成功后会将用户信息保存到框架提供的上下文对象中，所以此处我们就可以调用Spring Security框架提供的API获取当前用户的username并展
示到页面上。

第一步：在main.html页面中修改，定义username模型数据基于VUE的数据绑定展示用
户名，发送ajax请求获取username

```java
		created(){
            //发送ajax请求，请求Controller获取当前登录用户的用户名，展示到页面中
            axios.get("/user/getUsername.do").then((res) => {
                if(res.data.flag){
                    this.username = res.data.data;
                }
            });
        }
```

```html
<div class="avatar‐wrapper">
<img src="../img/user2‐160x160.jpg" class="user‐avatar">
<!‐‐展示用户名‐‐>
{{username}}
</div>
```



第二步：创建UserController并提供getUsername方法

```java
package com.itheima.controller;

import com.itheima.constant.MessageConstant;
import com.itheima.entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户操作
 */
@RestController
@RequestMapping("/user")
public class UserController {
    //获得当前登录用户的用户名
    @RequestMapping("/getUsername")
    public Result getUsername(){
        //当Spring security完成认证后，会将当前用户信息保存到框架提供的上下文对象
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(user);
        if(user != null){
            String username = user.getUsername();
            
            return new Result(true, MessageConstant.GET_USERNAME_SUCCESS,username);
        }

        return new Result(false, MessageConstant.GET_USERNAME_FAIL);
    }
}

```

>通过SecurityContextHolder.getContext().getAuthentication().getPrincipal()可以获取到spring security的user对象，里边包含了登录用户的用户名和权限



## 13-在项目中应用Spring Security_实现认证和授权（用户退出）

第一步：在main.html中提供的退出菜单上加入超链接

```html
<el‐dropdown‐item divided>
<span style="display:block;"><a href="/logout.do">退出</a></span>
</el‐dropdown‐item>
```

第二步：在spring-security.xml文件中配置

```xml
<!‐‐
logout：退出登录
logout‐url：退出登录操作对应的请求路径
logout‐success‐url：退出登录后的跳转页面
‐‐>
<security:logout logout‐url="/logout.do"
logout‐success‐url="/login.html" invalidatesession="
true"/>
```



## 14-ECharts简介

ECharts缩写来自Enterprise Charts，商业级数据图表，是百度的一个开源的使用
JavaScript实现的数据可视化工具，可以流畅的运行在 PC 和移动设备上，兼容当前绝大
部分浏览器（IE8/9/10/11，Chrome，Firefox，Safari等），底层依赖轻量级的矢量图
形库 ZRender，提供直观、交互丰富、可高度个性化定制的数据可视化图表。

官网：https://echarts.baidu.com/
下载地址：https://echarts.baidu.com/download.html



下载完成可以得到如下文件：
解压zip文件，我们只需要将dist目录下的echarts.js文件引入到页面上就可以使用了



## 15-ECharts入门案例

```html
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
		<script src="../js/echarts/echarts.js" type="text/javascript" charset="utf-8"></script>
	</head>
	<body>
		<div id="main" style="height: 600px;width: 400px;">
			
		</div>
		<script type="text/javascript">
			var mycharts = echarts.init(document.getElementById('main'))
			// 指定图表的配置项和数据
			        var option = {
			            title: {
			                text: 'ECharts 入门示例'
			            },
			            tooltip: {},
			            legend: {
			                data:['销量']
			            },
			            xAxis: {
			                data: ["衬衫","羊毛衫","雪纺衫","裤子","高跟鞋","袜子"]
			            },
			            yAxis: {},
			            series: [{
			                name: '销量',
			                type: 'bar',
			                data: [5, 20, 36, 10, 10, 20]
			            }]
			        };
			
			        // 使用刚指定的配置项和数据显示图表。
			        mycharts.setOption(option);

		</script>
	</body>
</html>

```

>1.引入echarts.js文件
>
>2.编写一个div用于echarts展示
>
>3.生成echarts对象，并且传入我们的div的dom元素
>
>4.指定图标的配置项和数据
>
>5.使用刚指定的配置项和数据显示图表



echarts教程：

https://www.echartsjs.com/zh/tutorial.html#5%20%E5%88%86%E9%92%9F%E4%B8%8A%E6%89%8B%20ECharts



## 16-查看ECharts官方实例

ECharts提供了很多官方实例，我们可以通过这些官方实例来查看展示效果和使用方法。
官方实例地址：https://www.echartsjs.com/examples/

在实际应用中，我们要展示的数据往往存储在数据库中，所以我们可以发送ajax请求获取
数据库中的数据并转为图表所需的数据即可。



## 17-会员数量折线图_需求分析

会员信息是体检机构的核心数据，其会员数量和增长数量可以反映出机构的部分运营情
况。通过折线图可以直观的反映出会员数量的增长趋势。本章节我们需要展示过去一年
时间内每个月的会员总数据量。

>需要统计在每个月用户数量是多少



## 18-会员数量折线图_页面调整

第一步：将echarts.js文件复制到health_backend工程的plugins目录下

第二步：在report_member.html页面引入echarts.js文件

```javascript
<script src="../plugins/echarts/echarts.js"></script>
```

页面代码：

```html
<!DOCTYPE html>
<html>
    <head>
        <!-- 页面meta -->
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>传智健康</title>
        <meta name="description" content="传智健康">
        <meta name="keywords" content="传智健康">
        <meta content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no" name="viewport">
        <!-- 引入样式 -->
        <link rel="stylesheet" href="../css/style.css">
        <script src="../plugins/echarts/echarts.js"></script>
    </head>
    <body class="hold-transition">
        <div id="app">
            <div class="content-header">
                <h1>统计分析<small>会员数量</small></h1>
                <el-breadcrumb separator-class="el-icon-arrow-right" class="breadcrumb">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item>统计分析</el-breadcrumb-item>
                    <el-breadcrumb-item>会员数量</el-breadcrumb-item>
                </el-breadcrumb>
            </div>
            <div class="app-container">
                <div class="box">
                    <!-- 为 ECharts 准备一个具备大小（宽高）的 DOM -->
                    <div id="chart1" style="height:600px;"></div>
                </div>
            </div>
        </div>
    </body>
    <!-- 引入组件库 -->
    <script src="../js/vue.js"></script>
    <script src="../js/axios-0.18.0.js"></script>
    <script type="text/javascript">
        // 基于准备好的dom，初始化echarts实例
        var myChart1 = echarts.init(document.getElementById('chart1'));

        // 使用刚指定的配置项和数据显示图表。
        //myChart.setOption(option);

        axios.get("/report/getMemberReport.do").then((res)=>{
            myChart1.setOption(
                                {
                                    title: {
                                        text: '会员数量'
                                    },
                                    tooltip: {},
                                    legend: {
                                        data:['会员数量']
                                    },
                                    xAxis: {
                                        data: res.data.data.months//动态数据
                                    },
                                    yAxis: {
                                        type:'value'
                                    },
                                    series: [{
                                        name: '会员数量',
                                        type: 'line',
                                        data: res.data.data.memberCount
                                    }]
                                });
        });
    </script>
</html>

```

>x轴显示月份
>
>y轴显示会员数量



根据折线图对数据格式的要求，我们发送ajax请求，服务端需要返回如下格式的数据：

```javascript
{
"data":{
"months":["2019.01","2019.02","2019.03","2019.04"],
"memberCount":[3,4,8,10]
},
"flag":true,
"message":"获取会员统计数据成功"
}
```



## 19-会员数量折线图_后台代码（Controller中提供模拟数据）

在health_backend工程中创建ReportController并提供getMemberReport方法

本章节中使用模拟数据

```java
package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.Result;
import com.itheima.service.MemberService;
import com.itheima.utils.DateUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 报表操作
 */
@RestController
@RequestMapping("/report")
public class ReportController {
    @Reference
    private MemberService memberService;
    //会员数量折线图数据
    @RequestMapping("/getMemberReport")
    public Result getMemberReport(){
        Map<String,Object> map = new HashMap<>();
        List<String> months = new ArrayList();
       	months.add("2019.01");
        months.add("2019.02");
        months.add("2019.03");
        months.add("2019.04");
        map.put("months",months);

        List<Integer> memberCount = new ArrayList();
        memberCount.add(3);
        memberCount.add(4);
        memberCount.add(8);
        memberCount.add(10);
        map.put("memberCount",memberCount);
        return new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_SUCCESS,map);
    }
}


```



## 20-会员数量折线图_后台代码（Controller中动态计算月份数据）

```java
package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.Result;
import com.itheima.service.MemberService;
import com.itheima.utils.DateUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 报表操作
 */
@RestController
@RequestMapping("/report")
public class ReportController {
    @Reference
    private MemberService memberService;
    //会员数量折线图数据
    @RequestMapping("/getMemberReport")
    public Result getMemberReport(){
        Map<String,Object> map = new HashMap<>();
        List<String> months = new ArrayList();
        Calendar calendar = Calendar.getInstance();//获得日历对象，模拟时间就是当前时间
        //计算过去一年的12个月
        calendar.add(Calendar.MONTH,-12);//获得当前时间往前推12个月的时间
        //2018年10月26日
        for(int i=0;i<12;i++){
            //2018年10月开始
            calendar.add(Calendar.MONTH,1);//获得当前时间往后推一个月日期
            Date date = calendar.getTime();
            //2018.10
            months.add(new SimpleDateFormat("yyyy.MM").format(date));
        }
        //2019年10月
        
        map.put("months",months);

        List<Integer> memberCount = memberService.findMemberCountByMonths(months);
        map.put("memberCount",memberCount);
        return new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_SUCCESS,map);
    }
}

```



main.html中修改菜单

```javascript
{
                    "path": "5",     //菜单项所对应的路由路径
                    "title": "统计分析",     //菜单项名称
                    "icon":"fa-heartbeat",
                    "children":[//是否有子菜单，若没有，则为[]
                        {
                            "path": "/5-1",
                            "title": "会员数量统计",
                            "linkUrl":"report_member.html",
                            "children":[]
                        }
                    ]
                }
```



在MemberService服务接口中扩展方法findMemberCountByMonth

```java
public List<Integer> findMemberCountByMonth(List<String> month);
```



## 21-会员数量折线图_后台代码（服务实现类）

在MemberServiceImpl服务实现类中实现findMemberCountByMonth方法

```java
//根据月份统计会员数量
public List<Integer> findMemberCountByMonth(List<String> month) {
List<Integer> list = new ArrayList<>();
for(String m : month){
	m = m + ".31";//格式：2019.04.31
	Integer count = memberDao.findMemberCountBeforeDate(m);
	list.add(count);
}
return list;
}
```



在MemberDao接口中扩展方法findMemberCountBeforeDate

```java
public Integer findMemberCountBeforeDate(String date);
```

在MemberDao.xml映射文件中提供SQL语句

```xml
<!‐‐根据日期统计会员数，统计指定日期之前的会员数‐‐>
<select id="findMemberCountBeforeDate" parameterType="string"
resultType="int">
select count(id) from t_member where regTime &lt;= #{value}
</select>
```

```sql
SELECT * FROM t_member  WHERE regTime <='2018-05-31'
SELECT COUNT(*) FROM t_member  WHERE regTime <='2018-05-31'
			SELECT COUNT(NAME) FROM t_member  WHERE regTime <='2018-05-31'
SELECT COUNT(id) FROM t_member  WHERE regTime <='2018-05-31';

-- 如果一个表有主键  COUNT(*) 实际上是统计的 主键字段  ==COUNT(id)
-- 如果一个表没有主键  COUNT(*) 实际上是统计的 是所有字段  ,效率较低
```



>传入的日期格式是2019.04.31
>
>20190431
>
>2019-04-31
>
>...
>
>这类都可以用于比较时间，且结果都正确



备注：

 记录以下mybatis中的转义字符，方便以后自己看一下

| \&lt;   | <    | 小于   |
| ------- | ---- | ------ |
| \&gt;   | >    | 大于   |
| \&amp;  | &    | 与     |
| \&apos; | '    | 单引号 |
| \&quot; | "    | 双引号 |

需要注意的是分号是必不可少的。 比如 a > b 我们就写成  a \&gt; b

 

当然啦， 我们也可以用另外一种，就是<![CDATA[ ]]>符号。 在mybatis中这种符号将不会解析。 比如

```
<![CDATA[ when min(starttime)<='12:00' and max(endtime)<='12:00' ]]>    
```

 

## 22-会员数量折线图_测试

