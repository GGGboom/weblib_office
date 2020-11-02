var WeekBar=function(array)
{
    var days=new Array();
    var times=new Array();
    var counter=0;
    for(var i=0;i<array.length;i++)
    {
      var date=array[i].date.toString().split(' ');
      days.push(date[0]+" "+date[1]+" "+date[2]);
      times.push(array[i].times);
      counter+=array[i].times;  
    }
	// 指定图表的配置项和数据
    var option = {
            title: {
                text: '周记录统计图'
            },
            tooltip: {},
            legend: {
                data:['总登录次数:'+counter]
            },
            xAxis: {
                type:'category',
                data:days
            },
            yAxis: {
                type:'value'                
            },
            series: [{
                name: '总登录次数:'+counter,
                type: 'bar',                
                data:times
            }]
        };
        return option;
}

var MonthBar=function(array)
{

    var times=new Array();
    var weeks=new Array();
    var counter=0;
    for(var i=0;i<array.length;i++)
    {
      var date=array[i].date.toString().split(' ');
      weeks.push("第"+(i+1)+"周"+"(>"+date[1]+" "+date[2]+")");
      times.push(array[i].times);  
      counter+=array[i].times;
    }
    // 指定图表的配置项和数据
    var option = {
            title: {
                text: '月记录统计图'
            },
            tooltip: {},
            legend: {
                data:['总登录次数:'+counter]
            },
            xAxis: {
                type:'category',
                data:weeks
            },
            yAxis: {
                type:'value'                
            },
            series: [{
                name: '总登录次数:'+counter,
                type: 'bar',                
                data:times
            }]
        };
        return option;
}

var WeekUsersBar=function(array)
{

    var times=new Array();
    var users=new Array();
    for(var i=array.length-1;(i>array.length-10)&&(i>0);i--)
    {
      users.push(array[i].account);
      times.push(array[i].times); 
    }
    // 指定图表的配置项和数据
    var option = {
            title: {
                text: '周用户登录次数统计排名(前10名)'
            },
            tooltip: {},
            legend: {
                data:['']
            },
            xAxis: {
                type:'category', 
                data:users,
                axisLabel :{  
                   interval:0   
                }
            },
            yAxis: {
                type:'value'                
            },
            series: [{
                name: '',
                type: 'bar',                
                data:times
            }]
        };
        return option;
}

var MonthUsersBar=function(array)
{

    var times=new Array();
    var users=new Array();
    for(var i=array.length-1;(i>array.length-10)&&(i>0);i--)
    {
      users.push(array[i].account);
      times.push(array[i].times);  

    }
    // 指定图表的配置项和数据
    var option = {
            title: {
                text: '月用户登录次数统计排名(前10名)'
            },
            tooltip: {},
            legend: {
                data:['']
            },
            xAxis: {
                type:'category',
                data:users,
                axisLabel :{  
                   interval:0   
                } 
           },
           yAxis: {
                type:'value'                
           },
           series: [{
                name: '',
                type: 'bar',                
                data:times
            }]
        };
        return option;
}