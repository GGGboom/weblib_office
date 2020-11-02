<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>DCampus WebLib 登录</title>
<script src="login/js/jquery-1.4.2.pack.js" type="text/javascript"></script>
<script src="login/js/jquery.cookie.pack.js" type="text/javascript"></script>
<style type="text/css">
html,body { margin:0; padding:0;height:100%;font-family:Microsoft YaHei}
input, select{ font-family:Microsoft YaHei}
input:-webkit-autofill { 
-webkit-box-shadow: 0 0 0px 20px white inset; 
outline:none
} 

.wrap { background:#fff url("login/body_bg.png") no-repeat center / cover; padding: 160px 0 0;min-height:100%;box-sizing:border-box;}
.wrap .main{width:1024px;margin:0px auto 0px;background:url("login/main_bg.png") no-repeat center;width:1024px;height:540px;box-shadow:0 0 6px rgba(0,0,0,0.1)}
.wrap .main form{margin-left:590px;padding:100px 90px;}
.wrap .main form .field{display:block;background:none;margin-top:20px;height:40px;width:100%;border:none;padding:8px 0;border-bottom:1px solid #dfdfdf;color:#353535;font-size:16px;line-height:24px;box-sizing:border-box;}

.wrap .main form .field:focus{border-bottom:2px solid #ff870f;padding-bottom:7px}
.wrap .main form .checkbox{font-size:16px;line-height:22px;color:#353535;margin-top:14px;height:22px;}
.wrap .main form .checkbox .unchecked{background:url("login/unchecked.png") no-repeat;width:22px;height:22px;padding:0;border:0;font-size:16px;float:left;margin-right:8px}
.wrap .main form .checkbox .checked{background:url("login/checked.png") no-repeat;}
.wrap .main form .submit_btn{margin-top:44px;background:#5db307;width:100%;height:40px;font-size:16px;line-height:40px;color:#ffffff;border:none;border-radius:2px;}
.wrap .footer{padding:90px 0 40px;text-align:center;}
.wrap .footer .copyright{color:#686767;font-size:11px;line-height:20px}
.wrap .footer .scan_hint{display:inline-block;position:relative}
.wrap .footer .scan_hint a{padding-left:16px;font-size:11px;color:#66ba07;line-height:20px;background:url("login/ico_scan.png") no-repeat 4px center;text-decoration:none}
.wrap .footer .scan_hint .scan_pop{position:absolute;top:-176px;background:url("login/qrcode.png") no-repeat;width:150px;height:176px;left:-28px;display:none}

</style>
</head>

<body>
<div class="wrap">
	<div class="main">
    	<form  method="post" id="form" >
        	<input type="text"  class="field" placeholder="帐号" name="username" id="username"/>
            <input type="password"  class="field" placeholder="密码" name="password" id="password"/>
            
            <div class="checkbox"><input type="button" class="unchecked " id="remember_btn"/> 记住密码</div>
            <input type="button" value="登录"  class="submit_btn local" />
        </form>
    </div>
    <div class="footer">
    	<span class="copyright">© Copyright 2008-2016 DCampus Networks ltd.  </span>
        <div class="scan_hint">
        	<a href="javascript:void(0)" class="">扫码下载客户端</a>
            <div class="scan_pop" >
            
            </div>
        </div>
		
    </div>
</div>
<script>

$(function(){
    $("[name='username']").focus();
			$("[name='password']").unbind().bind("keydown",function(e) {
			   if(e.keyCode == 13) {
			     	$(".local").trigger("click");
			     
			   }
			});
				
	$(".local").bind("click",function(){
	    updateCookie();
		if($.trim($("[name='username']").val()) == "") {
		   alert("请输入用户名");
		} else if($.trim($("[name='password']").val()) == "") {
		   alert("请输入密码");
		} else {
			 $.ajax({
				url:"/login/authenticate_v2"
				,dataType : "json"
				,type : "post"
				,data : "account=" + encodeURIComponent($("[name='username']").val()) +  "&password=" + encodeURIComponent($("[name='password']").val())
				,success : function(data){
					 $.ajax({
						url:"/login/selectMember_v2?temp="+Math.random()
						,dataType : "json"
						,type : "post"
						,data : "memberId=" + data.members[0].id
						,success : function(data){	
								window.location.href = "vue_backup/";
						}
						,error : function(message){
						   var data = eval('('+$.trim(message.responseText)+')');
						   alert(data.detail);
						}
					})		
				}
				,error : function(message){
				   var data = eval('('+$.trim(message.responseText)+')');
				   alert(data.detail);
				}
			 })
		}
		return false;
	});
	
	 //cookie
	 var isRemember=false;
	 if($.cookie("username")!=null) {
		 isRemember=true;
		 $("[name='username']").val($.cookie("username"));
		 $("[name='password']").val($.cookie("password"));				   
		 $("#remember_btn").addClass("checked");
	   } else {
		   isRemember=false;
		 $("[name='username']").val("");
		 $("[name='password']").val("");
		 $("#remember_btn").removeClass("checked");
	   }
	$("#remember_btn").bind("click",function(){
       isRemember=!isRemember;
	   if(isRemember){
		 $("#remember_btn").addClass("checked");
	   }else{
		 $("#remember_btn").removeClass("checked");
	   }
   });
	  

	$(".scan_hint a").bind("mouseenter",function(){
	   $(".scan_pop").show();
	    
	}).bind("mouseleave",function(){
	   $(".scan_pop").hide();
	    
	}).bind("click",function(){
	   if($(".scan_pop").is(":hidden")){
		   $(".scan_pop").show();
	   }else{
		   $(".scan_pop").hide();
	   }
	});
	
	function updateCookie(){
		if(isRemember) {
		  savePassword();
		} else {
		  clearPassword();
	   }
	}
	function savePassword(){
	  $.cookie("username",$("[name='username']").val(),{expires:365});
	  $.cookie("password",$("[name='password']").val(),{expires:365});
	}
	function clearPassword(){
	  $.cookie("username",null);
	  $.cookie("password",null);	   
	}
})


</script>
</body>
</body>
</html>
