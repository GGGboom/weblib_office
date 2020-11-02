
function getFile(array)
 {
    var daytimes=new Array();  //保存一周的零时刻-次数 对象数组
    var weektimes=new Array();  //保存一月的零时刻-次数 对象数组

    var alldata=new Array();   //保存所有数据

    var count1=0;
    var count2=0;
    var num=20000;   //请求总数


    for(var i=0;i<array[0].length;i++)
    {
      var obj=new Object();
      obj.date=array[0][i];                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
      obj.times=0;
      daytimes.push(obj);
    }

    for(var i=0;i<array[1].length;i++)
    {
      var obj=new Object();
      obj.date=array[1][i];
      obj.times=0;
      weektimes.push(obj);
    }

    $.ajax({
      url:"/log/getLatestLoginLogs.action",
      type:'GET',
      async:false,   //必须要使用同步方式
      dataType:'json',
      data:{start:"0",limit:num},//如何没有设限制，则一次只能返回50条
      success:function(data)
      {
          //alert(data.loginLogs[9998].createDate);

          for(var i=0;i<num-1;i++)
          {
               //获取日志日期
            var string=data.loginLogs[i].createDate;
            var datetime=string.split(' ');
            var date=datetime[0].split('-');
            var time=datetime[1].split(':');
            var mydate=new Date(date[0],date[1]-1,date[2],time[0],time[1],time[2]);
               //alert(mydate);
            
            if(mydate>weektimes[0].date)
            {
                weektimes[0].times++;   

               if(mydate>daytimes[0].date)
               {
                 daytimes[0].times++;
                 continue;
               }
               if(mydate>daytimes[1].date)
               {
                 daytimes[1].times++;
                 continue;
               }
               if(mydate>daytimes[2].date)
               {
                 daytimes[2].times++;
                 continue;
               }
               if(mydate>daytimes[3].date)
               {
                 daytimes[3].times++;
                 continue;
               }
               if(mydate>daytimes[4].date)
               {
                 daytimes[4].times++;
                 continue;
               }
               if(mydate>daytimes[5].date)
               {
                 daytimes[5].times++;
                 continue;
               }
               if(mydate>daytimes[6].date)
               {
                 daytimes[6].times++;
                 continue;
               }                 
            }


            if(mydate>weektimes[1].date)
            {
              weektimes[1].times++;
              continue;
            }
            if(mydate>weektimes[2].date)
            {
              weektimes[2].times++;
              continue;
            }
            if(mydate>weektimes[3].date)
            {
              weektimes[3].times++;
              continue;
            }
            if(mydate>weektimes[4].date)
            {
              weektimes[4].times++;
              continue;
            }
            if(mydate<weektimes[4].date)
            {
              break;
            }             
          }//for
          for(var i=0;i<daytimes.length;i++)
          {
            count1+=daytimes[i].times;
          }
          for(var i=0;i<weektimes.length;i++)
          {
            count2+=weektimes[i].times;
          }



          alldata.push(daytimes);
          alldata.push(weektimes); 
          //alert(count1+" "+count2);
          alldata.push( GetWeekUserTimes(data,count1) ); 
          alldata.push( GetMonthUserTimes(data,count2) );

      },
      error:function()
      {
        alert("请先登录");
      }
    }); 
    //return userAccount;     
    return alldata;  
 } 


 function GetWeekUserTimes(data,count)
 {
         var weekusers=new Array(); //保存一周的用户访问次数
         for (var i = 0; i < count; i++) 
          {
            var bool=false;
            var person=new Object();
            for(var j=0;j<weekusers.length;j++)
            {
              if( data.loginLogs[i].account==weekusers[j].account )
              {
                bool=true;
              }
            }    
            if(bool==false)
             {
               person.account=data.loginLogs[i].account;
               person.times=0;
               weekusers.push(person);
             }     
          }
         //获取每个用户的访问次数
         for(var i=0;i<weekusers.length;i++)
         {  
            var counter=0;
            for(var j=0;j<count;j++)
            {
               if(weekusers[i].account==data.loginLogs[j].account)
               {
                 counter++;
               }              
            } 
            weekusers[i].times=counter;         
         }
         return weekusers;
 }


 function GetMonthUserTimes(data,count)
 {
         var monthusers=new Array() //保存一月用户访问次数
         for (var i = 0; i < count; i++) 
          {
            var bool=false;
            var person=new Object();
            for(var j=0;j<monthusers.length;j++)
            {
              if( data.loginLogs[i].account==monthusers[j].account )
              {
                bool=true;
              }
            }    
            if(bool==false)
             {
               person.account=data.loginLogs[i].account;
               person.times=0;
               monthusers.push(person);
             }     
          }
         //获取每个用户的访问次数
         for(var i=0;i<monthusers.length;i++)
         {  
            var counter=0;
            for(var j=0;j<count;j++)
            {
               if(monthusers[i].account==data.loginLogs[j].account)
               {
                 counter++;
               }              
            } 
            monthusers[i].times=counter;         
         }
         return monthusers;
 }