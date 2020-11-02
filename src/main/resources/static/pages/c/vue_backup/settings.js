//https://weblib.ccnl.scut.edu.cn/pages/c/vue/#cas //中央认证回跳地址
const version = '4.1.9'

var basePath = '/pages/c/vue/';//'/pages/c/vue/' //打包接口前缀路径,包括上传的iframe要修改这里

var casUrl = 'https://ccnl.scut.edu.cn/cas/login?service='
    + encodeURIComponent('https://weblib.ccnl.scut.edu.cn/pages/casLogin.jsp')

var copyright = `© Copyright 2008-2019 DCampus Networks ltd. WebLib Plus V${version} 微服务`

var isJwtLogin = false // 判断是否采用微服务登录，false则为原来登录
var serverurl=''
var uploadUrl = './'  //上传的url前缀

console.log('版本号：'+version)