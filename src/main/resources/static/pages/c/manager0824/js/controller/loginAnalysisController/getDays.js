function getDays()
{
   var datetime=new Array();//保存最近一周和最近一月的时间节点（零时刻）
   var days=new Array();//保存最近一周时间节点（零时刻）
   var weeks=new Array();//保存最近一个各个周时间节点（零时刻）

   var now=new Date();
   var year=now.getFullYear();
   var month=now.getMonth();
   var day=now.getDate();
   var today=new Date(year,month,day); //获取当日零时刻时间
   var tomarrow=plus(today,1);  //获取次日零时刻日期    
   for(var i=0;i<7;i++)
   { 
     days.push(minus(today,i));
   }
   for(var i=7;i<36;i+=7)
   {
   	  weeks.push(minus(tomarrow,i));
   }
   datetime.push(days);
   datetime.push(weeks);
   return datetime;
}

//日期减法

function minus(date,num)
{
   mydate=date.valueOf();
   mydate=mydate-num*24*60*60*1000;
   mydate=new Date(mydate);
   return mydate;
}

//时间加法
function plus(date,num)
{
   mydate=date.valueOf();
   mydate=mydate+num*24*60*60*1000;
   mydate=new Date(mydate);
   return mydate;
}