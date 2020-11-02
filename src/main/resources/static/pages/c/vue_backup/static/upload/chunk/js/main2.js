var _getChunkObj = null;
var openbpupload=true;//打开断点续传
resetUploadView();
/**
 * @desc   判断一下是否有续传没有完成
 */
getChunk().then(function (res) {
  if (res.file.length > 0) {
    _getChunkObj = res;
    //如果从getChunk中取回有记录过的文件，如果有则窗口从中间移动到右下方
   // parent.moveToRightConner();
    $('#resourceId').val(res.file[0].resourceId);
    $('#groupId').val(res.file[0].groupId);
    $('#parentId').val(res.file[0].parentId);
    continueUploadView();//显示续传视图
    $('#files').html('<li class="template-upload fade"><p class="name">' + res.file[0].filename + '</p>' +
      '<div class="uploadBar"><span>已上传 ' + res.file[0].no * 1 + 'MB</span></div>' +
      '<a href="javascript:void(0)" class="btn btn-warning cancel onecancel"></a></li>');
  } else {
    resetUploadView();
  }
});
var browserTips = '将文件或单个文件夹<br/>拖拽到这里hhhhhhh';
var isUploading = false;
var jqXHR = null;
$(function () {
  var _element = '';
  var paramArr = location.href.split('#')[1].split('&');
  var parentId = paramArr[0].split('=')[1];
  var groupId = paramArr[1].split('=')[1];
  var singleSize = paramArr[2].split('=')[1];
  var $this, that;
  $('#groupId').val(groupId);
  $('#parentId').val(parentId);

  $('.dragWrap').height($(window).height() - 70);
  $('.dragWrap').bind('click', function (e) {
    var target = $(e.target);
    if (target.is('.close_error')) {
      target.parent().remove();
      if ($('.template-upload').length == 0) {
        isUploading = false;
        $('.tips').html(browserTips);
        parent.uploadComplete();//刷新列表
      }
    }
    if ($('.error').length == 0) {
      setTimeout(function () {
        $('.tips ').show();
      }, 100);
    }
  });

  $(window).resize(function () {
    $('.dragWrap').height($(window).height() - 90);
  });

  //判断chrome
  if (/chrome/.test(navigator.userAgent.toLowerCase())) {
    $('.container').show();
    $('.noscript').hide();
  }
  'use strict'; //以下js语法使用以严谨模式

  var url ='http://'+window.location.host+'/group/uploadChunkResource';//部署
 // var url='/group/uploadChunkResource.action'// 本地开发用
  var __uploadedBytes = 0, __maxChunkSize = 1048576; //1M
  $('#fileupload').unbind().fileupload({
    url: url,
    dataType: 'json',
    autoUpload: false,
    acceptFileTypes: '',
    sequentialUploads: true,
    /* limitConcurrentUploads: 1,*/
    limitMultiFileUploads: 1,
    maxFileSize: singleSize / 1024 * 1000000,
    previewThumbnail: false,
    maxChunkSize: __maxChunkSize,//1M
    uploadedBytes: __uploadedBytes,
    disableImageResize: /Android(?!.*Chrome)|Opera/
      .test(window.navigator.userAgent),
    drop: function (e, data) {
     // parent.uploading();
        totalFiles = data.files.length;
      if (isUploading) {
        alert('抱歉，正在上传文件，暂时不能添加！')
        return false;
      }

      if (data.files.length > 1) {
        alert('抱歉，断点续传不支持上传多个文件！');
        return false;
      }
    },
    add: function (e, data) {

      if (isUploading) {
        alert('抱歉，正在上传文件，暂时不能添加！')
        return false;
      }
      jqXHR = data;

      if (isUploading) {
        return false;
      }


      if (_getChunkObj !== null) {
        if (data.files[0].name === _getChunkObj.file[0].filename) {
          $('#files').empty();
          jqXHR.uploadedBytes = _getChunkObj.file[0].no * __maxChunkSize;
        } else {
          alert('您选择的文件与要续传的文件不一致，请重新选择');
          return false;
        }
      }
      uploadingView();//显示上传区域uploadingView();

      parentId = location.href.split('#')[1].split('&')[0].split('=')[1];
      if (parentId < 0) {
        parentId = 0;
      }
      //drop完会走这里
      if (data.paramName != undefined || typeof(data.fileInput) == 'object') {
        $this = $(this),
          that = $this.data('blueimp-fileupload') ||
            $this.data('fileupload'), options = that.options;
        $('#message').show();
      }

      if (e.isDefaultPrevented()) {
        return false;
      }


      data.context = that._renderUpload(data.files);
      _element = $(_element).add(data.context);//串联jquery对象

      options.filesContainer[
        options.prependFiles ? 'prepend' : 'append'
        ]($(_element));
      $('#message').hide();
      $('.tips').hide();
      _element = '';
     // parent.moveToRightConner();//添加文件上传后，窗口从中间移动到右下角，并开始上传 浩

      data.submit();
    }
  }).on('fileuploadprogressall', function (i, o) {
  }).on('fileuploaddone', function () {
    $('#resourceId').val('');
    setTimeout(function () {
      if ($('.template-upload').length === 0) {
        $('.tips').html(browserTips);
        $('.tips ').show();
        isUploading = false;
        parent.uploadComplete();
       // parent.closeResumeModal();           浩
        localStorage.removeItem('saveBreakPoint');
      }
    }, 1000);
  }).on('fileuploadchunkalways', function (e, data) {
    isUploading = true;
    if (data.result !== undefined) {
      $('#resourceId').val(data.result.files[0].resourceId);
      localStorage.saveBreakPoint = JSON.stringify({
        files: {
          lastModified: data.files[0].lastModified
          , name: data.files[0].name
          , size: data.files[0].size
        },
        result: data.result.files[0]
      });
    }
  });


  $('#files').bind('click', function (e) {
    var target = $(e.target);
    if (target.is('.onecancel')) {
      //parent.uploadComplete();
      if ($('#resourceId').val() > 0) {
        console.log("进入删除分块")
        $.ajax({
          url: '/group/deleteChunckResource',
          dataType: 'json',
          type: 'post',
          data: { resourceId: $('#resourceId').val() },
          success: function (data) {
            $('#resourceId').val('');
            localStorage.removeItem('saveBreakPoint');
            console.log("成功接受");
            window.location.reload();
          }
        });
      } else {
        $('#resourceId').val('');
        console.log("失败接收");
        localStorage.removeItem('saveBreakPoint');
        window.location.reload();
      }
    }
    if (target.is('.stop')) {
      if (!isUploading) {
        return false;
      }
      uploadTrigger = !uploadTrigger;
      if (uploadTrigger) {
        //暂停上传，把当前上传挂起
        target.addClass('continue').attr('title', '继续上传');
        jqXHR.abort();
      } else {
        target.removeClass('continue').attr('title', '暂停上传');
        //获取上次上传到第几块分块设置到继续上传按钮
        getChunk().then(function (res) {
          if (res.file.length > 0) {
            //如果从getChunk中取回有记录过的文件，如果有则窗口从中间移动到右下方
            $('#resourceId').val(res.file[0].resourceId);
            $('#groupId').val(res.file[0].groupId);
            $('#parentId').val(res.file[0].parentId);
            jqXHR.uploadedBytes = res.file[0].no * __maxChunkSize;
            setTimeout(function () {
              jqXHR.submit();
            }, 500);
          }
        });
      }
    }
  });
  $('.stop').bind('click', function () {
    if (!isUploading) {
      return false;
    }
    uploadTrigger = !uploadTrigger;
    if (uploadTrigger) {
      $('.stop span').html('续传');
      jqXHR.abort();
    } else {
      $('.stop span').html('暂停');
      jqXHR.uploadedBytes = JSON.parse(localStorage.saveBreakPoint).result.no * __maxChunkSize;
      $('#resourceId').val(JSON.parse(localStorage.saveBreakPoint).result.resourceId);
      setTimeout(function () {
        jqXHR.submit();
      }, 500);
    }
  });
  $('.cancelAll').bind('click', function () {
    parent.uploadComplete();
    if ($('#resourceId').val() > 0) {
      $.ajax({
        url: '/group/deleteChunckResource',
        dataType: 'json',
        type: 'post',
        data: { resourceId: $('#resourceId').val() },
        success: function (data) {
          console.log(data);
          $('#resourceId').val('');
          localStorage.removeItem('saveBreakPoint')
          openbpupload=false;
          window.location.reload();
        }
      });
    } else {
      $('#resourceId').val('');
      localStorage.removeItem('saveBreakPoint')
      window.location.reload();
    }

  });

});

function notAllowUpload() {
  alert('抱歉，只能拖拽单个文件夹！');
  $('#message').hide();
  countfile = 0;
  countFolder = 0;
  totalFiles = 0;
  _saveData = [];
  _element = '';
  $('.tips').html(browserTips);
}

function resetUploadView() {
  $('.uploadWrap').hide();//显示上传区域
  $('.row').show();//隐藏大添加按钮
  $('.uploadToolbar').hide();//隐藏绿色选择按钮
}

function uploadingView() {
  $('.uploadWrap').show();//显示上传区域
  $('.row').hide();//隐藏大添加按钮
  $('.uploadToolbar').hide();//隐藏绿色选择按钮
}

function continueUploadView() {
  $('.uploadWrap').show();//显示上传区域
  $('.row').hide();//隐藏大添加按钮
  $('.uploadToolbar').show();//显示绿色上传条选择按钮
}

function resumeUploadView() {
  $('.uploadWrap').show();//显示上传区域
  $('.row').hide();//隐藏大添加按钮
  $('.uploadToolbar').show();//隐藏绿色选择按钮
}


function getChunk() {
  return $.ajax({
    url: '/group/getMyChunckResources',
    dataType: 'json',
    type: 'post'
  });
}

function deleteChunk(id) {
  return $.ajax({
    url: '/group/deleteChunckResource',
    data: {
      resourceId: id
    },
    dataType: 'json',
    type: 'post'
  })
}