<%@ page contentType="text/html;charset=utf-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%@include file="path.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="Keywords" content="<%=getSiteSeoKeywords%>" />
<meta name="Description" content="<%=getSiteSeoDescription%>" />
<title><%=getSiteName%></title>
<link href="../themes/default/css/global.css" rel="stylesheet" type="text/css" />
<%if(isLogin) { %>
<script>
window.location.href="<%=site%>/pages/personal.jsp";
</script>
<%}%>
<script src="../js/jquery/jquery-1.4.2.pack.js"></script>
<script src="js/TrimPath_Template.js"></script>
<script src="js/DCampus.Public.js"></script>
<script src="js/DCampus.Login.js"></script>
</head>
<body>
<div id="headerWrap">
<div id="header">
  <%@include file="header.jsp"%>
</div>
<script>
$j(".login").remove();
$j(".header_right_bg").html("<a href='index.jsp'>圈子首页</a>");
</script>
</div>
<div id="loginWrap">
<div class="coL"><br />
</div>
<div class="coR">
<div class="loginWrap">
<h3 class="title">登录圈子</h3>
<div class="login-content">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr>
    <td>帐号：</td>
    <td><input type="text" name="account"/></td>
    <td></td>
  </tr>
  <tr>
    <td>密码：</td>
    <td><input type="password" name="password"/></td>
    <td></td>
  </tr>
</table>
<input type="button" class="loginButton submit"/>
</div>
</div>

<div class="chooseMaJiaWrap displayNone">
<h3 class="title">选择马甲</h3>
<div id="showMaJiaList"></div>
<div class="loginBottomBar"><input type="button" class="maJiaButton putOnMaJia" value="披上马甲"/> <input type="button" class="maJiaButton cancel" value="取消"/></div>
</div>
</div>
<div class="clearBoth"></div>
</div>
<div id="copyright_frontpage">
 <%@include file="footer.jsp"%>
</div>
<textarea id="majiaList_template" style="display:none">
<table  border="0" class="maJiaListTable">
{for member in members}
  <tr>
    <td width="20"><input type="radio"  name="groupsMajia" value="\${member.id}"/></td>
    <td width="20"><img src="\${member.icon|html}" width="35" height="35"/></td>
    <td><a href="javascript:void(0)">\${member.name|escape}</a></td>
  </tr>
{/for}
</table>
</textarea>
<%=getSiteStatCode%>
</body>
</html>
