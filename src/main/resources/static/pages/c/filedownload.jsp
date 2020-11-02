<%@ page contentType="text/html;charset=utf-8" %>
<%@page import="org.springframework.web.bind.ServletRequestUtils"%>
<%
 // String code = ServletRequestUtils.getStringParameter(request, "code","");
 //String id = ServletRequestUtils.getStringParameter(request, "id","");
 //String filesize = ServletRequestUtils.getStringParameter(request, "filesize","1000");
 //String name = ServletRequestUtils.getStringParameter(request, "name","demo");
 String token =ServletRequestUtils.getStringParameter(request, "token","");
 //request.setAttribute("id", id);
%>
<!DOCTYPE>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>资源库提取文件页</title>

<style type="text/css">
html,body{height:100%}
body,p,input { margin:0; padding:0; }
body {  font-family:微软雅黑,黑体}

.header{height:44px;background:#f6f4ee url('images/logo_share.png') no-repeat 8px center / 280px 40px;border-bottom:1px solid #dbdbdb;}

.wrap{width:100%;bottom:0;top:45px;position:absolute;min-height:660px;background: url('images/bg_share.png') no-repeat center / cover;}
.wrap .text{font-size:20px;color:#000000;line-height:36px;max-height:72px;overflow:hidden}
.wrap .light{color:#b2b2b2;font-size:12px;line-height:24px}

.wrap .main{width:100%;margin:80px auto 40px;}
.wrap .main .info{background:url("images/ico_file.png") no-repeat top center / 116px 122px;padding:140px 0 0 0;text-align:center}
.wrap .main .inputs{margin:20px auto;width:300px;}
.wrap .main .inputs .code{display:block;width:100%;border:1px solid #66ba07;background:#ffffff;text-align:center;padding:4px 8px;line-height:28px;height:44px;font-size:16px;box-sizing:border-box;}
.wrap .main .inputs .btn{display:block;width:100%;border:0 none;background:#66ba07;height:44px;font-size:16px;line-height:44px;color:#f7f4ef;margin-top:12px;cursor:pointer}

.wrap .main.default{}
.wrap .main.no_file{display:none;background:url("images/ico_nofile.png") no-repeat top center / 116px 122px;padding:140px 0 0 0;text-align:center}

.wrap .footer{position:absolute;bottom:0;text-align:center;width:100%;margin-bottom:40px}
.wrap .footer .intro,
.wrap .footer .copyright{color:#b2b2b2;font-size:12px;line-g=height:16px;}
.wrap .footer .copyright{margin-top:60px;}

@media (max-width:767px){
	.wrap{background:#f6f4ee;min-height:610px;}
	.wrap .main{margin:50px auto 20px;}
	.wrap .footer .copyright{margin-top:40px;}
}
</style>
<script src="login/js/jquery-1.4.2.pack.js"></script>

</head>

<body>
<div class="header">

</div>
<div class="wrap">
	<div class="main default" >
        <div class="info">
            <p class="text" id="fileName">&nbsp;</p>
            <p class="light">有效期：<span id="expiredDate"></span></p>
            <p class="light">文件大小：<span id="sizelimit">0KB</span></p>
        </div>
        <div class="inputs">
            <input type="text" class="code" placeholder="请输入提取码" id="code" />
            <button class="btn" id="download">立即下载</button>
        </div>
    </div>
    <div class="main no_file">
            <p class="text">文件不存在！</p>
            <p class="light">可能已经被撤销或者链接错误</p>
        
    </div>
    <div class="footer">
        <div class="intro">
            DCampus Weblib为员工提供的文件储存服务<br />
            可随时随地上传和下载文档、照片、音乐、<br />
            软件，快捷方便<br />
            文件上传后永久保存
        </div>
        <div class="copyright">
            © DCampus rights reserved. Powered by DCampus
        </div>
    </div>
</div>

<script>

$(function(){
   //getStatus();
   //$(".validTimes").text(getResourceCode.validTimes);
   //$(".expiredDate").text(getResourceCode.expiredDate);
		var filesize = "";
		var checkCode;
		$.ajax({
			url:"/webmail/getResourceInfoByToken",
			data:"token=<%=token%>",
			async:false,
			dataType:"json",
			success:function(data){
			  filesize = data.filesize;
			  //$(".validTimes").text(data.validTimes);
			  $("#expiredDate").text(data.expiredDate);		
			  $("#fileName").text(data.name);
   
			//console.log(data);
			   if(filesize >= 1024 && filesize <= 1024*1024) {
				  filesize = _formatFloat(filesize / 1024 , 2) + "MB";
			   } else if(filesize >= 1024*1024){
					filesize = _formatFloat(filesize / 1024 / 1024 , 2) + "G";
				 }else {
				   filesize = filesize + "KB";
				 }
				 
		   $("#sizelimit").text(filesize);	
		   	  
			  if(data.checkCode == 1) {
			     
				 $("#download").bind("click",function(){
				    if($("#code").val() == "") {
					  alert("请输入验证码");
					}else {
					  $.ajax({
					     url:"/webmail/checkToken?token=<%=token%>",
						 data:"code="+$("#code").val(),
						 dataType:"json",
						 type:"GET",
						 success:function(data){
						    if(data.type == "fail") {
							  alert("提取码错误，请重新输入!");
							} else {
							  window.location.href = "/webmail/downloadByToken?token=<%=token%>"+"&code="+$("#code").val();
							  //window.location.href = "/webmail/downloadByToken?token=<%=token%>"+"&code="+$(".codeInput").find("input").val();
							}
						 },
						 error:function(data){
						     alert("文件不存在!");
						 }
					  });
					}
				 });
		
			  
			  } else {
			    $("#code").hide();
				//$("#download").attr("href","/webmail/downloadByToken?token=<%=token%>");
				$("#download").bind("click",function(){
						window.location.href = "/webmail/downloadByToken?token=<%=token%>";
				 });
			  }
			  
			},error:function(){
			   $(".main.default").hide();
			   $(".main.no_file").show();
			}
			
		});

});
function _formatFloat(src, pos){
  if(src > 0 && src<0.01) {
	 src = 0.01;
  }
  return Math.round(src*Math.pow(10, pos))/Math.pow(10, pos);
}



</script>
</body>
</html>