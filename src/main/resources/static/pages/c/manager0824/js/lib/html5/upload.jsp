<!DOCTYPE HTML>
<!--
/*
 * jQuery File Upload Plugin Demo 8.6.0
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */
-->
<html lang="en">
<head>
<!-- Force latest IE rendering engine or ChromeFrame if installed -->
<!--[if IE]>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<![endif]-->
<meta charset="utf-8">
<title>jQuery File Upload Demo</title>
<meta name="keyword" content=""/>
<meta name="description" content="File Upload widget with multiple file selection, drag&amp;drop support, progress bars, validation and preview images, audio and video for jQuery. Supports cross-domain, chunked and resumable file uploads and client-side image resizing. Works with any server-side platform (PHP, Python, Ruby on Rails, Java, Node.js, Go etc.) that supports standard HTML form file uploads.">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<!-- Bootstrap CSS Toolkit styles -->
<!-- Generic page styles -->
<!-- Bootstrap styles for responsive website layout, supporting different screen sizes -->
<!-- Bootstrap CSS fixes for IE6 -->
<!--[if lt IE 7]>
<link rel="stylesheet" href="http://blueimp.github.io/cdn/css/bootstrap-ie6.min.css">
<![endif]-->
<!-- blueimp Gallery styles -->
<link rel="stylesheet" href="html5/css/bootstrap.min.css">
<!-- CSS to style the file input field as button and adjust the Bootstrap progress bars -->
<link rel="stylesheet" href="html5/css/jquery.fileupload-ui.css">
<link rel="stylesheet" href="html5/css/style.css">
<!-- CSS adjustments for browsers with JavaScript disabled -->
<noscript>
<link rel="stylesheet" href="html5/css/jquery.fileupload-ui-noscript.css">
</noscript>

</head><body>
<div class="container" style="display:none">
<form id="fileupload" action="/group/uploadResource.action" method="POST" enctype="multipart/form-data">
  <noscript>
  <input type="hidden" name="redirect" value="http://blueimp.github.io/jQuery-File-Upload/">
  </noscript>
  <div class="row fileupload-buttonbar">
    <div class="span7">
      <span class="btn btn-success fileinput-button"><span>添加文件...</span>
      <input type="file" name="Filedata" multiple>
      <input type="hidden" id="groupId" name="groupId"  value="908"/>
      <input type="hidden" id="parentId" name="parentId"  value="-908"/>
      </span>
      <span class="fileupload-loading"></span> </div>
  </div>
  <div class="dragWrap">
  <span class="tips">将文件拖拽到这里</span>
  <div role="presentation" class="table table-striped">
    <ul class="files">
    </ul>
  </div>
  </div>
</form>
</div>
<div class="noscript" style="display:none">抱歉！您的浏览器不支持拖拽上传，如要体验更佳的上传功能，请下载以下浏览器：<b>Google Chrome 6.0+</b> / <b>Mozilla Firefox 3.6+</b> / <b>Opera 11.0+</b> / <b>Apple Safari 6.0+ (mac)</b></div>
<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
<li class="template-upload fade">
	  <!--span class="preview"></span-->      
      <p class="name">{%=file.name%}</p>
            {% if (file.error) { %}
			    <p class="name">{%=file.name%}</p>
                <div class="error"><span class="label label-important">Error</span> {%=file.error%}</div> <a href="javascript:void(0)" class="close_error"></a>
            {% } %}
			 <p class="size">{%=o.formatFileSize(file.size)%}</p>
            {% if (!o.files.error) { %}
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
				<div class="upload-status"></div>
            {% } %}	
			{% if (!o.files.error && !i && !o.options.autoUpload) { %}
                <button class="btn btn-primary start">
                    <i class="icon-upload icon-white"></i>
                    <span>Start</span>
                </button>
            {% } %}
            {% if (!i) { %}
                <a href="javascript:void(0)" class="btn btn-warning cancel"></a>
            {% } %}
			</li>

{% } %}
</script>
<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}

        	{% if (file.error) { %}
			<li class="template-upload fade">
			    <p class="name">{%=file.name%}</p>
                <div class="error"><span class="label label-important">Error</span> {%=file.error%}</div> <a href="javascript:void(0)" class="close_error"></a>
				</li>
            {% } %}
			
{% } %}			
</script>
<script src="html5/js/jquery.js"></script>
<script src="html5/js/vendor/jquery.ui.widget.js"></script>
<script src="html5/js/tmpl.js"></script>
<script src="html5/js/load-image.min.js"></script>
<script src="html5/js/canvas-to-bldb.min.js"></script>
<script src="html5/js/jquery.iframe-transport.js"></script>
<script src="html5/js/jquery.fileupload.js"></script>
<script src="html5/js/jquery.fileupload-process.js"></script>
<script src="html5/js/jquery.fileupload-image.js"></script>
<script src="html5/js/jquery.fileupload-audio.js"></script>
<script src="html5/js/jquery.fileupload-video.js"></script>
<script src="html5/js/jquery.fileupload-validate.js"></script>
<script src="html5/js/jquery.fileupload-ui.js"></script>
<script src="html5/js/main.js"></script>
<!-- The XDomainRequest Transport is included for cross-domain file deletion for IE 8 and IE 9 -->
<!--[if (gte IE 8)&(lt IE 10)]>
<script src="html5/js/cors/jquery.xdr-transport.js"></script>
<![endif]-->
</body>
</html>
