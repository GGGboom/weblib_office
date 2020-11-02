<%@ page contentType="text/html;charset=utf-8" %>
<%@page import="org.springframework.web.bind.ServletRequestUtils"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Queued Photo Uploader - Standalone Showcase from digitarald.de</title>
<meta name="author" content="Harald Kirschner, digitarald.de" />
<meta name="copyright" content="Copyright 2009 Harald Kirschner" />
<%
    String sessionId = session.getId();
    String filesize = ServletRequestUtils.getStringParameter(request, "filesize","");
	Long getParentId = ServletRequestUtils.getLongParameter(request, "id",0);
	Long getGroupId = ServletRequestUtils.getLongParameter(request, "groupId",0);
%>

<script src="../../../../js/jquery/jquery-1.4.2.pack.js"></script>
<script src="../../../../pages/js/DCampus.Public.js"></script>
<%@include file="../../../../pages/path.jsp"%>
<script type="text/javascript" src="../../source/mootools-1.2.4-core-yc.js"></script>
<script type="text/javascript" src="../../source/Swiff.Uploader.js"></script>
<script type="text/javascript" src="../../source/Fx.ProgressBar.js"></script>
<script type="text/javascript" src="../../source/FancyUpload2.js"></script>
<!-- See script.js -->
<script>
	var up;
	var uploadFunction = {
	   photoArrId : []
	}
		/**
 * FancyUpload Showcase
 *
 * @license		MIT License
 * @author		Harald Kirschner <mail [at] digitarald [dot] de>
 * @copyright	Authors
 */

$j(function(){
	var parentID = <%=getParentId%>;
	$j("input[name='groupId']").val(<%=getGroupId%>);
	if(parentID < 0)
	parentID = 0;
	$j("input[name='parentId']").val(parentID);

	//浏览器判断，如果不是ie的浏览器,给sessionId的表单赋值,jQuery的方法
	if(!$j.browser.msie) {
	   $j("input[name='sessionId']").val("<%=sessionId%>");
	}
	//下面是mootools的方法
	// our uploader instance 
	up = new FancyUpload2($('demo-status'), $('demo-list'), { // options object
		// we console.log infos, remove that in production!!
		verbose: true,
		listRemoveFunction : function(){
		  setTimeout(function(){
		     if($j("#demo-list").find("li").length == 0) {
			    $j(".uploadText").show();
			 }   
		  },1000)
		},
		
		/*fileSizeMax: <%=filesize%> * 1024,*/
		// url is read from the form, so you just have to change one place
		url: $('form-demo').action+"?temp="+Math.random(),
		
		// path to the SWF file
		path: '../../source/Swiff.Uploader.swf',
		
		// remove that line to select all files, or edit it, add more items
		/*typeFilter: {
			'Files (*.rar, *.zip, *.7z,*.vsd,*.xlsx,*.docx,*.pptx,*.pdf,*.tif,*.doc,*.txt,*.xls,*.ppt,*.jpg,*.jpeg,*.gif,*.png,*.swf,*.flv)': '*.rar; *.zip; *.7z;*.vsd;*.xlsx;*.docx;*.pptx;*.pdf;*.tif;*.doc;*.txt;*.xls;*.ppt;*.jpg;*.jpeg;*.gif;*.png;*.swf;*.flv'
		},*/
		// this is our browse button, *target* is overlayed with the Flash movie
		onBeforeStart: function() {
			up.setOptions({
			    data: $('form-demo').toQueryString()
			});
 		},
		target : $("demo-browse"),
		timeLimit : 600,
		// graceful degradation, onLoad is only called if all went well with Flash
		onLoad: function() {
			$('demo-status').removeClass('hide'); // we show the actual UI
			if($('demo-fallback') != null)
			$('demo-fallback').destroy(); // ... and hide the plain form
			if($j(".validation-error").length != 0) {
			   $j(".validation-error").remove();
			}			
			// We relay the interactions with the overlayed flash to the link
			this.target.addEvents({
				click: function() {
					return false;
				},
				mouseenter: function() {
					this.addClass('hover');
				},
				mouseleave: function() {
					this.removeClass('hover');
					this.blur();
				},
				mousedown: function() {
					this.focus();
				}
			});

			// Interactions for the 2 other buttons
			
			if($('demo-clear') != null) {
				$('demo-clear').addEvent('click', function() {
					up.remove(); // remove all files
					setTimeout(function(){$j(".uploadText").fadeIn(100)},500);
					return false;
				});
			}
			if($('demo-upload') != null) {
				$('demo-upload').addEvent('click', function() {
					up.start(); // start upload
					return false;
				});
			}
		},
		
		onStart : function(){
		    $j("#progressWrapCover").show();
		    $j("#progressWrap").show();
		},
		onComplete : function(){
		  $j("#progressWrap").hide();
		  $j("#progressWrapCover").hide();
		},
		onSelect : function(){
		   $j(".uploadText").hide();
				up.start(); // start upload
				return false;		   
		},
		// Edit the following lines, it is your custom event handling
		
		/**
		 * Is called when files were not added, "files" is an array of invalid File classes.
		 * 
		 * This example creates a list of error elements directly in the file list, which
		 * hide on click.
		 */ 
		onSelectFail: function(files) {
			files.each(function(file) {
				new Element('li', {
					'class': 'validation-error',
					html: file.validationErrorMessage || file.validationError,
					title: MooTools.lang.get('FancyUpload', 'removeTitle'),
					events: {
						click: function() {
							this.destroy();
						}
					}
				}).inject(this.list, 'top');
			}, this);
		},
		
		/**
		 * This one was directly in FancyUpload2 before, the event makes it
		 * easier for you, to add your own response handling (you probably want
		 * to send something else than JSON or different items).
		 */
		onFileSuccess: function(file, response) {
			var json = eval('('+response+')');
			if (json.type == 'success') {
				file.element.addClass('file-success');
				 //uploadFunction.photoArrId.push(json.images[0].id);
				//file.info.set('html', '<strong>Image was uploaded:</strong> ' + json.get('width') + ' x ' + json.get('height') + 'px, <em>' + json.get('mime') + '</em>)');
			} else {
				file.element.addClass('file-failed');
				//file.info.set('html', '<strong>An error occured:</strong> ' + (json.get('error') ? (json.get('error') + ' #' + json.get('code')) : response));
			}
		},
		onFileError : function(file,response){
	
			file.element.addClass('file-failed');
		},		
		

		/**
		 * onFail is called when the Flash movie got bashed by some browser plugin
		 * like Adblock or Flashblock.
		 */
		onFail: function(error) {
			switch (error) {
				case 'hidden': // works after enabling the movie and clicking refresh
					alert('To enable the embedded uploader, unblock it in your browser and refresh (see Adblock).');
					break;
				case 'blocked': // This no *full* fail, it works after the user clicks the button
					alert('To enable the embedded uploader, enable the blocked Flash movie (see Flashblock).');
					break;
				case 'empty': // Oh oh, wrong path
					alert('A required file was not found, please be patient and we fix this.');
					break;
				case 'flash': // no flash 9+ :(
					alert('To enable the embedded uploader, install the latest Adobe Flash plugin.')
			}
		}
	});
	
});
	
	</script>
<!-- See style.css -->
<style type="text/css">
/**
 * FancyUpload Showcase
 *
 * @license		MIT License
 * @author		Harald Kirschner <mail [at] digitarald [dot] de>
 * @copyright	Authors
 */

/* CSS vs. Adblock tabs */
body{margin:0;padding:0;font:12px/150% "宋体";color:#333;background:#EAF2FA;}
p {font:12px/150% "宋体";color:#333;}
.swiff-uploader-box a {
	display: none !important;
}
/* .hover simulates the flash interactions */
a:hover, a.hover {
	color: red;
}
#demo-status {
	padding: 5px 15px;
	width: 468px;
	height:285px;
	/*border: 1px solid #ADD98C;*/
	background:#ECF0F9;
}
#demo-status .progress {
	background: url(../../assets/progress-bar/progress.gif) no-repeat;
	background-position: +50% 0;
	margin-right: 0.5em;
	vertical-align: middle;
}
#demo-status .progress-text {
	font-size: 0.9em;
	font-weight: bold;
}
#demo-list {
	list-style: none;
	margin: 0;
	background:#fff;
	padding:0;
	border:1px solid #AAC1EA;
}
#demo-list li.validation-error {
	padding-left: 44px;
	display: block;
	clear: left;
	line-height: 40px;
	color: #8a1f11;
	cursor: pointer;
	border-bottom: 1px solid #fbc2c4;
	background: #fbe3e4 url(assets/failed.png) no-repeat 4px 4px;
}
#demo-list li.file {
	border-bottom: 1px solid #eee;
	background: url(assets/file.png) no-repeat 4px 4px;
	overflow: auto;
}
#demo-list li.file.file-uploading {
	background-image: url(assets/uploading.png);
	background-color: #D9DDE9;
}
#demo-list li.file.file-success {
	background-image: url(assets/success.png);
}
#demo-list li.file.file-failed {
	background-image: url(assets/failed.png);
}
#demo-list li.file .file-name {
	font-size: 1.2em;
	margin-left: 44px;
	display: block;
	clear: left;
	line-height: 40px;
	height: 40px;
	font-weight: bold;
}
#demo-list li.file .file-size {
	font-size: 0.9em;
	line-height: 18px;
	float: right;
	margin-top: 2px;
	margin-right: 6px;
}
#demo-list li.file .file-info {
	display: block;
	margin-left: 44px;
	font-size: 0.9em;
	line-height: 20px;
 clear
}
#demo-list li.file .file-remove {
	clear: right;
	float: right;
	line-height: 18px;
	margin-right: 6px;
}
#demo-list {
  height:250px;
  overflow:auto;
  position:relative;
}
.upLoadFunctionWrap {padding:0 0 5px 5px;}
#progressWrap {display:none;z-index:2;position:absolute;left:15px;top:15px;}
#progressWrapCover {display:none;position:absolute;width:100%;height:300px;background:#333;opacity:0.5;filter:alpha(opacity=50);top:0;left:0;z-index:1;}
.uploadText {position:absolute;top:90px;left:100px;font:28px/150% "微软雅黑";color:#ccc;}
.file-remove {display:none;}
</style>


</head>
<body>
<div class="container">
  <!-- See index.html -->
    <form action="<%=site%>/group/uploadResource.action" method="post" enctype="multipart/form-data" id="form-demo">
      <fieldset id="demo-fallback" style="display:none">
      <label for="demo-photoupload"> Upload a Resource:
      <input type="file" name="Filedata" />
      </label>
      </fieldset>
      <input type="hidden" value="" name="groupId"/>
      <input type="hidden" value="" name="sessionId"/>
      <input type="hidden" value="" name="parentId" id="parentId"/>
      <div id="demo-status" class="hide">
        <div class="upLoadFunctionWrap"> <a href="#" id="demo-browse">添加资源</a> <!--a href="#" id="demo-clear">全部删除</a--> <!--a href="#" id="demo-upload">开始上传</a--> <span class="current-text"></span></div>
              <ul id="demo-list">
        <div id="progressWrap"> <p><span class="overall-title"></span> </p><p>
          <img src="../../assets/progress-bar/bar.gif" class="progress overall-progress" /> </p><p><span class="current-title"></span></p> <p> 
          <img src="../../assets/progress-bar/bar.gif" class="progress current-progress"/>  </p>
       </div>
       <div id="progressWrapCover"></div>
       <span class="uploadText">请选择要上传资源</span>
      </ul>
      </div>
    </form>
</div>
</body>
</html>
