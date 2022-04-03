## 01-本章内容介绍

1.套餐预约占比饼图

2.运营数据统计

3.运营数据统计报表导出



## 02-套餐预约占比饼形图_完善页面

套餐预约占比饼形图对应的页面为/pages/report_setmeal.html。

**导入ECharts库**

```javascript
<script src="../plugins/echarts/echarts.js"></script>
```

**参照官方实例导入饼形图**

```html
				<div class="box">
                    <!-- 为 ECharts 准备一个具备大小（宽高）的 DOM -->
                    <div id="chart1" style="height:600px;"></div>
                </div>
```



```javascript
<script type="text/javascript">
        // 基于准备好的dom，初始化echarts实例
        var myChart1 = echarts.init(document.getElementById('chart1'));

        // 使用刚指定的配置项和数据显示图表。
        //myChart.setOption(option);

        axios.get("/report/getSetmealReport.do").then((res)=>{
            myChart1.setOption({
                                    title : {
                                        text: '套餐预约占比',
                                        subtext: '',
                                        x:'center'
                                    },
                                    tooltip : {//提示框组件
                                        trigger: 'item',//触发类型，在饼形图中为item
                                        formatter: "{a} <br/>{b} : {c} ({d}%)"//提示内容格式
                                    },
                                    legend: {//图例
                                        orient: 'vertical',
                                        left: 'left',
                                        data: res.data.data.setmealNames
                                    },
                                    series : [//数据系列
                                        {
                                            name: '套餐预约占比',
                                            type: 'pie',//饼形图
                                            radius : '55%',
                                            center: ['50%', '60%'],
                                            data:res.data.data.setmealCount,
                                            itemStyle: {
                                                emphasis: {
                                                    shadowBlur: 10,
                                                    shadowOffsetX: 0,
                                                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                                                }
                                            }
                                        }
                                    ]
                                });
        });
    </script>
```

>legend.data饼状图中的图例名需要替换
>
>series.data饼状图中的数据需要替换

根据饼形图对数据格式的要求，我们发送ajax请求，服务端需要返回如下格式的数据：

```javascript
{
"data":{
	"setmealNames":["套餐1","套餐2","套餐3"],
	"setmealCount":[
		{"name":"套餐1","value":10},
		{"name":"套餐2","value":30},
		{"name":"套餐3","value":25}
		]
	},
"flag":true,
"message":"获取套餐统计数据成功"
}
```



## 03-套餐预约占比饼形图_后台代码（Controller中测试模拟数据）

```java
    //套餐预约占比饼形图
    @RequestMapping("/getSetmealReport")
    public Result getSetmealReport(){
        //使用模拟数据测试使用什么样的java对象转为饼形图所需的json数据格式
        //{setmealNames:[],setmealCount:[{name,value}]}
     
        Map<String,Object> data = new HashMap<>();

        List<String> setmealNames = new ArrayList<>();
        setmealNames.add("套餐1");
        setmealNames.add("套餐2");
        data.put("setmealNames",setmealNames);

        List<Map<String,Object>> setmealCount = new ArrayList<>();
        Map<String,Object> data1 = new HashMap<>();
        data1.put("name","套餐1");
        data1.put("value",10);
        setmealCount.add(data1);

        Map<String,Object> data2 = new HashMap<>();
        data2.put("name","套餐2");
        data2.put("value",30);
        setmealCount.add(data2);
        

        data.put("setmealCount",setmealCount);
        
        for(Map<String,Object> setmeal : setmealCount){
            setmealNames.add((String)setmeal.get("name"));
        }

        return new Result(true,MessageConstant.GET_SETMEAL_COUNT_REPORT_SUCCESS,data);
    }
```



## 04-套餐预约占比饼形图_后台代码（Controller方法）

在health_backend工程的ReportController中提供getSetmealReport方法

```java
    @RequestMapping("/getSetmealReport")
    public Result getSetmealReport(){
        //使用模拟数据测试使用什么样的java对象转为饼形图所需的json数据格式
        Map<String,Object> data = new HashMap<>();

        try{
            List<Map<String,Object>> setmealCount = setmealService.findSetmealCount();
            List<Map<String,Object>> setmealCount2 = new List<>();
            data.put("setmealCount",setmealCount2);

            List<String> setmealNames = new ArrayList<>();
            for (Map<String, Object> map : setmealCount) {
                if((Integer)map.get("value") > 0){
                    String name = (String) map.get("name");//套餐名称
                	setmealNames.add(name);
                    setmealCount2.add(map);
                }
            }

            data.put("setmealNames",setmealNames);
            return new Result(true,MessageConstant.GET_SETMEAL_COUNT_REPORT_SUCCESS,data);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,MessageConstant.GET_SETMEAL_COUNT_REPORT_FAIL);
        }
    }
```

>由于套餐名称重复，所以在取得数据之后，遍历数据将名称放入setmealNames中



## 05-套餐预约占比饼形图_后台代码（服务实现类、DAO）

在SetmealService服务接口中扩展方法findSetmealCount

```java
public List<Map<String,Object>> findSetmealCount();
```



在SetmealServiceImpl服务实现类中实现findSetmealCount方法

```java
public List<Map<String, Object>> findSetmealCount() {
	return setmealDao.findSetmealCount();
}
```



在SetmealDao.xml映射文件中提供SQL语句

```xml
<select id="findSetmealCount" resultType="map">
    select s.name,count(o.id) as value
    from t_order o ,t_setmeal s
    where o.setmeal_id = s.id
    group by s.name
</select>
```

> group by 建议对s.id处理，避免名称重复



备注：

上述的处理方式sql相当于做了内连接，会把没有订单的数据过滤掉，可以使用left join进行优化

```sql
    SELECT s.name,COUNT(o.id) value
    FROM t_setmeal s LEFT JOIN t_order o ON s.id = o.setmeal_id
    GROUP BY s.id
```

然后在controller里进行数据为0的过滤即可，这样的sql就具有复用性



## 06-套餐预约占比饼形图_测试

## 07-运营数据统计_需求分析

通过运营数据统计可以展示出体检机构的运营情况，包括会员数据、预约到诊数据、热
门套餐等信息。本章节就是要通过一个表格的形式来展示这些运营数据。效果如下图：



![1](img\img10\1.png)



## 08-运营数据统计_完善页面

定义数据模型，通过VUE的数据绑定展示数据

```javascript
<script>
var vue = new Vue({
el: '#app',
    data:{
        reportData:{
        reportDate:null,
        todayNewMember :0,
        totalMember :0,
        thisWeekNewMember :0,
        thisMonthNewMember :0,
        todayOrderNumber :0,
        todayVisitsNumber :0,
        thisWeekOrderNumber :0,
        thisWeekVisitsNumber :0,
        thisMonthOrderNumber :0,
        thisMonthVisitsNumber :0,
        hotSetmeal :[
            		    {setmeal 数据}
                    ]
    }
  }
})
</script>
```



```html
                <div class="box" style="height: 900px">
                    <div class="excelTitle" >
                        <el-button @click="exportExcel">导出Excel</el-button>运营数据统计
                    </div>
                    <div class="excelTime">日期：{{reportData.reportDate}}</div>
                    <table class="exceTable" cellspacing="0" cellpadding="0">
                        <tr>
                            <td colspan="4" class="headBody">会员数据统计</td>
                        </tr>
                        <tr>
                            <td width='20%' class="tabletrBg">新增会员数</td>
                            <td width='30%'>{{reportData.todayNewMember}}</td>
                            <td width='20%' class="tabletrBg">总会员数</td>
                            <td width='30%'>{{reportData.totalMember}}</td>
                        </tr>
                        <tr>
                            <td class="tabletrBg">本周新增会员数</td>
                            <td>{{reportData.thisWeekNewMember}}</td>
                            <td class="tabletrBg">本月新增会员数</td>
                            <td>{{reportData.thisMonthNewMember}}</td>
                        </tr>
                        <tr>
                            <td colspan="4" class="headBody">预约到诊数据统计</td>
                        </tr>
                        <tr>
                            <td class="tabletrBg">今日预约数</td>
                            <td>{{reportData.todayOrderNumber}}</td>
                            <td class="tabletrBg">今日到诊数</td>
                            <td>{{reportData.todayVisitsNumber}}</td>
                        </tr>
                        <tr>
                            <td class="tabletrBg">本周预约数</td>
                            <td>{{reportData.thisWeekOrderNumber}}</td>
                            <td class="tabletrBg">本周到诊数</td>
                            <td>{{reportData.thisWeekVisitsNumber}}</td>
                        </tr>
                        <tr>
                            <td class="tabletrBg">本月预约数</td>
                            <td>{{reportData.thisMonthOrderNumber}}</td>
                            <td class="tabletrBg">本月到诊数</td>
                            <td>{{reportData.thisMonthVisitsNumber}}</td>
                        </tr>
                        <tr>
                            <td colspan="4" class="headBody">热门套餐</td>
                        </tr>
                        <tr class="tabletrBg textCenter">
                            <td>套餐名称</td>
                            <td>预约数量</td>
                            <td>占比</td>
                            <td>备注</td>
                        </tr>
                        <tr v-for="s in reportData.hotSetmeal">
                            <td>{{s.name}}</td>
                            <td>{{s.setmeal_count}}</td>
                            <td>{{s.proportion}}</td>
                            <td></td>
                        </tr>
                    </table>
                </div>
```



在VUE的钩子函数中发送ajax请求获取动态数据，通过VUE的数据绑定将数据展示到页面

```javascript
created() {
                axios.get("/report/getBusinessReportData.do").then((res)=>{
                    if(res.data.flag){
                        this.reportData = res.data.data;
                    }else{
                        this.$message.error(res.data.message);
                    }
                });
            },
```



## 09-运营数据统计_后台代码（Controller方法提供模拟数据）

```java
    //运营数据统计
    @RequestMapping("/getBusinessReportData")
    public Result getBusinessReportData(){
        Map<String,Object> result = new HashMap<>();
        //取出返回结果数据，准备将报表数据写入到Excel文件中
        result.put("reportDate","2019.01.01");
        result.put("todayNewMember",12);
        result.put("totalMember",20);
        result.put("thisWeekNewMember",50);
        result.put("thisMonthNewMember",40);
        result.put("todayOrderNumber",30);
        result.put("thisWeekOrderNumber",60);
        result.put("thisMonthOrderNumber",70);
        result.put("todayVisitsNumber",100);
        result.put("thisWeekVisitsNumber",120);
        result.put("thisMonthVisitsNumber",230);
        List<Map> hotsetmeals = new ArrayList<>();
        result.put("hotSetmeal",hotsetmeals);
        Map setmeal1 = new HashMap();
        setmeal1.put("setmeal_count",100);
        setmeal1.put("proportion",0.5);
        setmeal1.put("name","套餐1");
        hotsetmeals.add(setmeal1);

        Map setmeal2 = new HashMap();
        setmeal2.put("setmeal_count",100);
        setmeal2.put("proportion",0.5);
        setmeal2.put("name","套餐2");
        hotsetmeals.add(setmeal2);

        return new Result(true,MessageConstant.GET_BUSINESS_REPORT_SUCCESS,result);

    }
```

>先使用模拟出来的数据来进行测试



## 10-运营数据统计_后台代码（Controller方法完善）

在ReportController中提供getBusinessReportData方法

```java
   
//运营数据统计
    @RequestMapping("/getBusinessReportData")
    public Result getBusinessReportData(){
        try{
            Map<String,Object> data = reportService.getBusinessReportData();
            return new Result(true,MessageConstant.GET_BUSINESS_REPORT_SUCCESS,data);
        }catch (Exception e){
            return new Result(false,MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }
```



```java
    @Reference
    private ReportService reportService;
```



在health_interface工程中创建ReportService服务接口并声明getBusinessReport方法

```java
import java.util.Map;
public interface ReportService {
    /**
    * 获得运营统计数据
    * Map数据格式：
    {
    * todayNewMember ‐> number
    * totalMember ‐> number
    * thisWeekNewMember ‐> number
    * thisMonthNewMember ‐> number
    * todayOrderNumber ‐> number
    * todayVisitsNumber ‐> number
    * thisWeekOrderNumber ‐> number
    * thisWeekVisitsNumber ‐> number
    * thisMonthOrderNumber ‐> number
    * thisMonthVisitsNumber ‐> number
    * hotSetmeals ‐> List<Setmeal>
    }
    */
    public Map<String,Object> getBusinessReport() throws Exception;
}
```



## 11-运营数据统计_后台代码（服务实现类查询会员数据和预约到诊数据）

在health_service_provider工程中创建服务实现类ReportServiceImpl并实现ReportService接口

```java
package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.MemberDao;
import com.itheima.dao.OrderDao;
import com.itheima.service.ReportService;
import com.itheima.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营数据统计服务
 */
@Service(interfaceClass = ReportService.class)
@Transactional
public class ReportServiceImpl implements ReportService {
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private OrderDao orderDao;
    //查询运营数据
    public Map<String, Object> getBusinessReportData() throws Exception{
        //报表日期
        String today = DateUtils.parseDate2String(DateUtils.getToday());
        //获得本周一日期
        String thisWeekMonday = DateUtils.parseDate2String(DateUtils.getThisWeekMonday());
        //获得本月第一天日期
        String firstDay4ThisMonth = DateUtils.parseDate2String(DateUtils.getFirstDay4ThisMonth());

        //本日新增会员数 regTime = #{date}
        Integer todayNewMember = memberDao.findMemberCountByDate(today);
        //总会员数
        Integer totalMember = memberDao.findMemberTotalCount();
        //本周新增会员数
        Integer thisWeekNewMember = memberDao.findMemberCountAfterDate(thisWeekMonday);
        //本月新增会员数
        Integer thisMonthNewMember = memberDao.findMemberCountAfterDate(firstDay4ThisMonth);

        //今日预约数
        Integer todayOrderNumber = orderDao.findOrderCountByDate(today);
        //本周预约数
        Integer thisWeekOrderNumber = orderDao.findOrderCountAfterDate(thisWeekMonday);
        //本月预约数
        Integer thisMonthOrderNumber = orderDao.findOrderCountAfterDate(firstDay4ThisMonth);
        //今日到诊数
        Integer todayVisitsNumber = orderDao.findVisitsCountByDate(today);
        //本周到诊数
        Integer thisWeekVisitsNumber = orderDao.findVisitsCountAfterDate(thisWeekMonday);
        //本月到诊数
        Integer thisMonthVisitsNumber = orderDao.findVisitsCountAfterDate(firstDay4ThisMonth);

        //热门套餐查询
        List<Map> hotSetmeal = orderDao.findHotSetmeal();

        Map<String,Object> result = new HashMap<>();
        result.put("reportDate",today);
        result.put("todayNewMember",todayNewMember);
        result.put("totalMember",totalMember);
        result.put("thisWeekNewMember",thisWeekNewMember);
        result.put("thisMonthNewMember",thisMonthNewMember);
        result.put("todayOrderNumber",todayOrderNumber);
        result.put("thisWeekOrderNumber",thisWeekOrderNumber);
        result.put("thisMonthOrderNumber",thisMonthOrderNumber);
        result.put("todayVisitsNumber",todayVisitsNumber);
        result.put("thisWeekVisitsNumber",thisWeekVisitsNumber);
        result.put("thisMonthVisitsNumber",thisMonthVisitsNumber);
        result.put("hotSetmeal",hotSetmeal);
        return result;
    }
}

```



## 12-运营数据统计_后台代码（服务实现类查询热门套餐）

在OrderDao和MemberDao中声明相关统计查询方法

```java
package com.itheima.dao;
import com.itheima.pojo.Order;
import java.util.List;
import java.util.Map;
public interface OrderDao {
    public void add(Order order);
    public List<Order> findByCondition(Order order);
    public Map findById4Detail(Integer id);
    public Integer findOrderCountByDate(String date);
    public Integer findOrderCountAfterDate(String date);
    public Integer findVisitsCountByDate(String date);
    public Integer findVisitsCountAfterDate(String date);
    public List<Map> findHotSetmeal();
}
```

```java
package com.itheima.dao;
import com.github.pagehelper.Page;
import com.itheima.pojo.Member;
import java.util.List;
public interface MemberDao {
    public List<Member> findAll();
    public Page<Member> selectByCondition(String queryString);
    public void add(Member member);
    public void deleteById(Integer id);
    public Member findById(Integer id);
    public Member findByTelephone(String telephone);
    public void edit(Member member);
    public Integer findMemberCountBeforeDate(String date);
    public Integer findMemberCountByDate(String date);
    public Integer findMemberCountAfterDate(String date);
    public Integer findMemberTotalCount();
}
```



在OrderDao.xml和MemberDao.xml中定义SQL语句

OrderDao.xml：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.itheima.dao.OrderDao" >
    <resultMap id="baseResultMap" type="com.itheima.pojo.Order">
        <id column="id" property="id"/>
        <result column="member_id" property="memberId"/>
        <result column="orderDate" property="orderDate"/>
        <result column="orderType" property="orderType"/>
        <result column="orderStatus" property="orderStatus"/>
        <result column="setmeal_id" property="setmealId"/>
    </resultMap>
    <!--新增-->
    <insert id="add" parameterType="com.itheima.pojo.Order">
        <selectKey resultType="java.lang.Integer" order="AFTER" keyProperty="id">
            SELECT LAST_INSERT_ID()
        </selectKey>
        insert into t_order(member_id,orderDate,orderType,orderStatus,setmeal_id)
        values (#{memberId},#{orderDate},#{orderType},#{orderStatus},#{setmealId})
    </insert>

    <!--动态条件查询-->
    <select id="findByCondition" parameterType="com.itheima.pojo.Order" resultMap="baseResultMap">
        select * from t_order
        <where>
            <if test="id != null">
                and id = #{id}
            </if>
            <if test="memberId != null">
                and member_id = #{memberId}
            </if>
            <if test="orderDate != null">
                and orderDate = #{orderDate}
            </if>
            <if test="orderType != null">
                and orderType = #{orderType}
            </if>
            <if test="orderStatus != null">
                and orderStatus = #{orderStatus}
            </if>
            <if test="setmealId != null">
                and setmeal_id = #{setmealId}
            </if>
        </where>
    </select>

    <!--根据预约id查询预约信息，包括体检人信息、套餐信息-->
    <select id="findById4Detail" parameterType="int" resultType="map">
        select m.name member ,s.name setmeal,o.orderDate orderDate,o.orderType orderType
        from
          t_order o,
          t_member m,
          t_setmeal s
        where o.member_id=m.id and o.setmeal_id=s.id and o.id=#{id}
    </select>

    <!--根据日期统计预约数-->
    <select id="findOrderCountByDate" parameterType="string" resultType="int">
        select count(id) from t_order where orderDate = #{value}
    </select>

    <!--根据日期统计预约数，统计指定日期之后的预约数-->
    <select id="findOrderCountAfterDate" parameterType="string" resultType="int">
        select count(id) from t_order where orderDate &gt;= #{value}
    </select>

    <!--根据日期统计到诊数-->
    <select id="findVisitsCountByDate" parameterType="string" resultType="int">
        select count(id) from t_order where orderDate = #{value} and orderStatus = '已到诊'
    </select>

    <!--根据日期统计到诊数，统计指定日期之后的到诊数-->
    <select id="findVisitsCountAfterDate" parameterType="string" resultType="int">
        select count(id) from t_order where orderDate &gt;= #{value} and orderStatus = '已到诊'
    </select>

    <!--热门套餐，查询前4条-->
    <select id="findHotSetmeal" resultType="map">
        select s.name, count(o.id) setmeal_count ,count(o.id)/(select count(id) from t_order) proportion
          from t_order o inner join t_setmeal s on s.id = o.setmeal_id
          group by o.setmeal_id
          order by setmeal_count desc limit 0,4
    </select>
</mapper>
```

>要查询套餐预约数在所有预约数中的占比，需要使用套餐预约数/总预约数
>
>总预约数可以通过以下SQL获取
>
>```sql
>select count(id) from t_order
>```
>
>要对套餐进行统计，需要 
>
>```sql
>group by o.setmeal_id
>```
>
>最后按预约数倒序排列 
>
>```sql
>order by setmeal_count desc
>```



MemberDao.xml：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.itheima.dao.MemberDao" >
    <select id="findAll" resultType="com.itheima.pojo.Member">
        select * from t_member
    </select>

    <!--根据条件查询-->
    <select id="selectByCondition" parameterType="string" resultType="com.itheima.pojo.Member">
        select * from t_member
        <if test="value != null and value.length > 0">
            where fileNumber = #{value} or phoneNumber = #{value} or name = #{value}
        </if>
    </select>

    <!--新增会员-->
    <insert id="add" parameterType="com.itheima.pojo.Member">
        <selectKey resultType="java.lang.Integer" order="AFTER" keyProperty="id">
            SELECT LAST_INSERT_ID()
        </selectKey>
        insert into t_member(fileNumber,name,sex,idCard,phoneNumber,regTime,password,email,birthday,remark)
        values (#{fileNumber},#{name},#{sex},#{idCard},#{phoneNumber},#{regTime},#{password},#{email},#{birthday},#{remark})
    </insert>

    <!--删除会员-->
    <delete id="deleteById" parameterType="int">
        delete from t_member where id = #{id}
    </delete>

    <!--根据id查询会员-->
    <select id="findById" parameterType="int" resultType="com.itheima.pojo.Member">
        select * from t_member where id = #{id}
    </select>

    <!--根据id查询会员-->
    <select id="findByTelephone" parameterType="string" resultType="com.itheima.pojo.Member">
        select * from t_member where phoneNumber = #{phoneNumber}
    </select>

    <!--编辑会员-->
    <update id="edit" parameterType="com.itheima.pojo.Member">
        update t_member
        <set>
            <if test="fileNumber != null">
                fileNumber = #{fileNumber},
            </if>
            <if test="name != null">
                name = #{name},
            </if>
            <if test="sex != null">
                sex = #{sex},
            </if>
            <if test="idCard != null">
                idCard = #{idCard},
            </if>
            <if test="phoneNumber != null">
                phoneNumber = #{phoneNumber},
            </if>
            <if test="regTime != null">
                regTime = #{regTime},
            </if>
            <if test="password != null">
                password = #{password},
            </if>
            <if test="email != null">
                email = #{email},
            </if>
            <if test="birthday != null">
                birthday = #{birthday},
            </if>
            <if test="remark != null">
                remark = #{remark},
            </if>
        </set>
        where id = #{id}
    </update>

    <!--根据日期统计会员数，统计指定日期之前的会员数-->
    <select id="findMemberCountBeforeDate" parameterType="string" resultType="int">
        select count(id) from t_member where regTime &lt;= #{value}
    </select>

    <!--根据日期统计会员数-->
    <select id="findMemberCountByDate" parameterType="string" resultType="int">
        select count(id) from t_member where regTime = #{value}
    </select>

    <!--根据日期统计会员数，统计指定日期之后的会员数-->
    <select id="findMemberCountAfterDate" parameterType="string" resultType="int">
        select count(id) from t_member where regTime &gt;= #{value}
    </select>

    <!--总会员数-->
    <select id="findMemberTotalCount" resultType="int">
        select count(id) from t_member
    </select>
</mapper>
```

备注：

mybatis转义字符

```
&lt;	<	小于
&gt;	>	大于
&amp;	&	与
&apos;	'	单引号
&quot;	"	双引号
```

需要注意的是分号是必不可少的。 比如 a > b 我们就写成  a \&gt; b

当然啦， 我们也可以用另外一种，就是<![CDATA[ ]]>符号。 在mybatis中这种符号将不会解析。 比如

```
<![CDATA[ when min(starttime)<='12:00' and max(endtime)<='12:00' ]]>    
```



## 13-运营数据统计_测试

## 14-运营数据统计报表导出_需求分析并提供Excel模板文件

运营数据统计报表导出就是将统计数据写入到Excel并提供给客户端浏览器进行下载，以
便体检机构管理人员对运营数据的查看和存档。

本章节我们需要将运营统计数据通过POI写入到Excel文件，详见

>day10\资源\report_template.xlsx

通过上面的Excel效果可以看到，表格比较复杂，涉及到合并单元格、字体、字号、字体
加粗、对齐方式等的设置。如果我们通过POI编程的方式来设置这些效果代码会非常繁
琐。
在企业实际开发中，对于这种比较复杂的表格导出一般我们会提前设计一个Excel模板文
件，在这个模板文件中提前将表格的结构和样式设置好，我们的程序只需要读取这个文
件并在文件中的相应位置写入具体的值就可以了。
在本章节资料中已经提供了一个名为report_template.xlsx的模板文件，需要将这个文件
复制到health_backend工程的template目录中

>webapp\template\report_template.xlsx



## 15-运营数据统计报表导出_完善页面

在report_business.html页面提供导出按钮并绑定事件

```html
在report_business.html页面提供导出按钮并绑定事件
<div class="excelTitle" >
<el‐button @click="exportExcel">导出Excel</el‐button>运营数据统计
</div>
```

```javascript
methods:{
//导出Excel报表
	exportExcel(){
		window.location.href = '/report/exportBusinessReport.do';
	}
}
```



备注：

ajax请求无法响应下载功能因为response原因，一般请求浏览器是会处理服务器输出的response，例如生成png、文件下载等,然而ajax请求只是个“字符型”的请求，即请求的内容是以文本类型存放的。文件的下载是以二进制形式进行的，虽然可以读取到返回的response，但只是读取而已，是无法执行的，说白点就是js无法调用到浏览器的下载处理机制和程序。



## 16-运营数据统计报表导出_后台代码

在ReportController中提供exportBusinessReport方法，基于POI将数据写入到Excel中
并通过输出流下载到客户端

```java
//导出运营数据
    @RequestMapping("/exportBusinessReport")
    public Result exportBusinessReport(HttpServletRequest request, HttpServletResponse response){
        try{
            Map<String,Object> result = reportService.getBusinessReportData();
            //取出返回结果数据，准备将报表数据写入到Excel文件中
            String reportDate = (String) result.get("reportDate");
            Integer todayNewMember = (Integer) result.get("todayNewMember");
            Integer totalMember = (Integer) result.get("totalMember");
            Integer thisWeekNewMember = (Integer) result.get("thisWeekNewMember");
            Integer thisMonthNewMember = (Integer) result.get("thisMonthNewMember");
            Integer todayOrderNumber = (Integer) result.get("todayOrderNumber");
            Integer thisWeekOrderNumber = (Integer) result.get("thisWeekOrderNumber");
            Integer thisMonthOrderNumber = (Integer) result.get("thisMonthOrderNumber");
            Integer todayVisitsNumber = (Integer) result.get("todayVisitsNumber");
            Integer thisWeekVisitsNumber = (Integer) result.get("thisWeekVisitsNumber");
            Integer thisMonthVisitsNumber = (Integer) result.get("thisMonthVisitsNumber");
            List<Map> hotSetmeal = (List<Map>) result.get("hotSetmeal");

            String filePath = request.getSession().getServletContext().getRealPath("template")+ 
                File.separator+"report_template.xlsx";
            //基于提供的Excel模板文件在内存中创建一个Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(new FileInputStream(new File(filePath)));
            //读取第一个工作表
            XSSFSheet sheet = excel.getSheetAt(0);

            XSSFRow row = sheet.getRow(2);
            row.getCell(5).setCellValue(reportDate);//日期

            row = sheet.getRow(4);
            row.getCell(5).setCellValue(todayNewMember);//新增会员数（本日）
            row.getCell(7).setCellValue(totalMember);//总会员数

            row = sheet.getRow(5);
            row.getCell(5).setCellValue(thisWeekNewMember);//本周新增会员数
            row.getCell(7).setCellValue(thisMonthNewMember);//本月新增会员数

            row = sheet.getRow(7);
            row.getCell(5).setCellValue(todayOrderNumber);//今日预约数
            row.getCell(7).setCellValue(todayVisitsNumber);//今日到诊数

            row = sheet.getRow(8);
            row.getCell(5).setCellValue(thisWeekOrderNumber);//本周预约数
            row.getCell(7).setCellValue(thisWeekVisitsNumber);//本周到诊数

            row = sheet.getRow(9);
            row.getCell(5).setCellValue(thisMonthOrderNumber);//本月预约数
            row.getCell(7).setCellValue(thisMonthVisitsNumber);//本月到诊数

            int rowNum = 12;
            for(Map map : hotSetmeal){//热门套餐
                String name = (String) map.get("name");
                Long setmeal_count = (Long) map.get("setmeal_count");
                BigDecimal proportion = (BigDecimal) map.get("proportion");
                row = sheet.getRow(rowNum ++);
  
                row.getCell(4).setCellValue(name);//套餐名称
                row.getCell(5).setCellValue(setmeal_count);//预约数量
                row.getCell(6).setCellValue(proportion.doubleValue());//占比
            }

            //使用输出流进行表格下载,基于浏览器作为客户端下载
            OutputStream out = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");//代表的是Excel文件类型
            response.setHeader("content-Disposition", "attachment;filename=report.xlsx");//指定以附件形式进行下载
            excel.write(out);

            out.flush();
            out.close();
            excel.close();
            return null;
        }catch (Exception e){
            return new Result(false,MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
```

>File.separator可以适配windows的目录分隔符\和linux的目录分隔符/
>
>获取Web项目的全路径 ，也就是webapp的目录
>String strDirPath = request.getSession().getServletContext().getRealPath("/"); 
>
>那么如果我们要获取template的目录
>
>request.getSession().getServletContext().getRealPath("template")
>
>使用POI访问excel模板时注意行与列的访问索引都要从0开始，如果是第6行，那就是传递rownum 5



```java
public static void main(String[] args) {
        double a=0.000000000000000000000000000000000000000000000000000000000000000000000000000001;
        double b=0.0000000000000000000000000000000000000001;
        double c= a+b;
        System.out.println(c);

        BigDecimal bigDecimal = new BigDecimal("0.000000000000000000000000000000000000000000000000000000000000000000000000000001");
        BigDecimal bigDecimal2 = new BigDecimal("0.0000000000000000000000000000000000000001");

        BigDecimal bigDecimal3 = bigDecimal.add(bigDecimal2);// 加法
    
        System.out.println(bigDecimal3.toString());

    }
//  subtract  减法 加法add // multiply乘法
```





## 17-运营数据统计报表导出_测试

拓展：

利用easy-poi进行导出

http://easypoi.mydoc.io/#text_221144



| 运营数据统计            |                          |                |                           |
| ----------------------- | ------------------------ | -------------- | ------------------------- |
| 日期                    | {{reportDate}}           |                |                           |
| 会员数据统计            |                          |                |                           |
| 新增会员数              | {{todayNewMember}}       | 总会员数       | {{totalMember}}           |
| 本周新增会员数          | {{thisWeekNewMember}}    | 本月新增会员数 | {{thisMonthNewMember}}    |
| 预约到诊数据统计        |                          |                |                           |
| 今日预约数              | {{todayOrderNumber}}     | 今日到诊数     | {{todayVisitsNumber}}     |
| 本周预约数              | {{thisWeekOrderNumber}}  | 本周到诊数     | {{thisWeekVisitsNumber}}  |
| 本月预约数              | {{thisMonthOrderNumber}} | 本月到诊数     | {{thisMonthVisitsNumber}} |
| 热门套餐                |                          |                |                           |
| 套餐名称                | 预约数量                 | 占比           | 备注                      |
| {{$fe:hotSetmeal t.name | t.setmeal_count          | t.proportion   | }}                        |
|                         |                          |                |                           |
|                         |                          |                |                           |
|                         |                          |                |                           |

```
这里面有正常的标签以及fe遍历，fe遍历应该是使用最广的遍历，用来解决遍历后下面还有数据的处理方式 我们要生成的是这个需要一些list集合和一些单纯的数据

fe的写法 fe标志 冒号 list数据 单个元素数据（默认t，可以不写） 第一个元素 {{$fe: maplist t t.id }}

```



在common中引入依赖,不要在 bakcend 的项目引入,否则会jar 包冲突

```xml
        <!--easy-poi jar包引入 -->
        <dependency>
            <groupId>cn.afterturn</groupId>
            <artifactId>easypoi-base</artifactId>
            <version>3.2.0</version>
        </dependency>
        <dependency>
            <groupId>cn.afterturn</groupId>
            <artifactId>easypoi-web</artifactId>
            <version>3.2.0</version>
        </dependency>
        <dependency>
            <groupId>cn.afterturn</groupId>
            <artifactId>easypoi-annotation</artifactId>
            <version>3.2.0</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>5.2.1.Final</version>
        </dependency>
```



```java
  @RequestMapping("/exportBusinessReport")
    public Result exportBusinessReport(HttpServletRequest request, HttpServletResponse response){
        try{
            Map<String,Object> result = reportService.getBusinessReportData();
            
            TemplateExportParams params = new TemplateExportParams(
                    request.getSession().getServletContext().getRealPath("template")+ File.separator+"report_template_easypoi.xlsx");

            Workbook workbook = ExcelExportUtil.exportExcel(params, result);

            //使用输出流进行表格下载,基于浏览器作为客户端下载
            OutputStream out = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");//代表的是Excel文件类型
            response.setHeader("content-Disposition", "attachment;filename=report.xlsx");//指定以附件形式进行下载
            workbook.write(out);

            out.flush();
            out.close();
            workbook.close();

            return null;
        }catch (Exception e){
            return new Result(false,MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }
```

