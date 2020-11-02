var browserTips = '将文件或单个文件夹<br/>拖拽到这里'
var isUploadFolder = true
var resultsArr = []
var arr = []
var countfile = 0
var totalFiles = 0
var countFolder = 0
var coutFolderOut = 0
var countCreateFolder = 0
var _countEntry = 0  //_entry计数
var entries = []

if (!!window.ActiveXObject || 'ActiveXObject' in window) {
  //判断ie10/ie11
  if (/MSIE\s+11.0/i.test(navigator.userAgent) || /MSIE\s+10.0/i.test(navigator.userAgent) || typeof(attachEvent) == 'undefined') {
    browserTips = '将文件拖拽到这里'
    $('.tips').html(browserTips)
  }
}
if (/firefox/.test(navigator.userAgent.toLowerCase())) {
  browserTips = '请选择文件上传'
  $('.tips').html(browserTips)
}
$(function() {
  var _folderIdArr = [], _savePath = []
  var _saveData = [], _element = ''
  var paramArr = location.href.split('#')[1].split('&')
  var parentId = paramArr[0].split('=')[1]
  var groupId = paramArr[1].split('=')[1]
  var singleSize = paramArr[2].split('=')[1]


  // var leaveSpace = paramArr[3].split("=")[1];
  var $this, that
  var folderParentId = null//全局ParentId,记录每次创建文件夹后的id变化
  var markFolderChange = null
  if (!!window.ActiveXObject || 'ActiveXObject' in window) {
    //判断ie10/ie11
    if (/MSIE\s+10.0/i.test(navigator.userAgent) || typeof(attachEvent) == 'undefined') {
    }
  }


  folderParentId = parseInt(parentId)
  $('#groupId').val(groupId)
  $('#parentId').val(parentId)

  $('.dragWrap').height($(window).height() - 70)
  $('.dragWrap').bind('click', function(e) {
    var target = $(e.target)
    if (target.is('.close_error')) {
      target.parent().remove()
      if ($('.template-upload').length == 0) {
        isUploadFolder = true
        resultsArr = []
        arr = []
        countfile = 0
        totalFiles = 0
        countFolder = 0
        coutFolderOut = 0
        countCreateFolder = 0
        _countEntry = 0  //_entry计数
        entries = []
        $('.tips').html(browserTips)
        window.parent.uploadComplete()
        // parent.uploadComplete();
      }
    }
    if ($('.error').length == 0) {
      setTimeout(function() {
        $('.tips ').show()
      }, 100)
    }
  })
  $('.noscript').height($(window).height() - 60)
  $(window).resize(function() {
    $('.dragWrap').height($(window).height() - 90)
    $('.noscript').height($(window).height() - 60)
  })


  //判断Safari
  if (/safari/.test(navigator.userAgent.toLowerCase()) && navigator.userAgent.toLowerCase().indexOf('windows') > 0) {
    $('.container').hide()
    $('.noscript').show()
  } else if (/safari/.test(navigator.userAgent.toLowerCase())) {
    $('.container').show()
  }

  //判断firefox
  if (/firefox/.test(navigator.userAgent.toLowerCase())) {
    $('.container').show()
    $('.noscript').hide()
  }

  //屏蔽所有ie浏览器包括6\7\8\9\10\11
  if (!!window.ActiveXObject || 'ActiveXObject' in window) {
    //判断ie10/ie11
    if (/MSIE\s+10.0/i.test(navigator.userAgent) || typeof(attachEvent) == 'undefined') {
      $('.noscript').hide()
      $('.container').show()
    } else {
      $('.noscript').show()
      $('.container').hide()
    }
  }
  //浏览器判断
  //判断chrome
  if (/chrome/.test(navigator.userAgent.toLowerCase())) {
    $('.container').show()
    $('.noscript').hide()
  }

  'use strict' //以下js语法使用以严谨模式
  //var url = uploadUrl + '/group/uploadResource.action' //本地测试上传url

    var url='http://'+window.location.host+'/group/uploadResource'//部署
//    $('#fileupload').fileupload({
//        url: url
//    });


  $('#fileupload').unbind().fileupload({
    url: url,
    dataType: 'json',
    autoUpload: false,
    acceptFileTypes: '',
    sequentialUploads: true,
    // maxChunkSize:1024*2*1024 ,
    /* limitConcurrentUploads: 1,
     limitMultiFileUploads: 2,*/
    maxFileSize: singleSize / 1024 * 1000000,
    previewThumbnail: false,
    disableImageResize: /Android(?!.*Chrome)|Opera/
      .test(window.navigator.userAgent),
    beforeSend: function(xhr, data) {
      xhr.setRequestHeader('Authorization', 'bearer ' + getCookie('Admin-Token'))
    },
    drop: function(e, data) {
      // parent.uploading();
      if (!isUploadFolder) {
        alert('抱歉，正在上传带有文件夹的资源，上传完毕前暂时不能上传其它文件！')
        return false
      }
      countFolder = 0
      totalFiles = data.files.length
      var _relativePath

      for (var j = 0; j < totalFiles; j++) {
        if (data.files[j].relativePath == undefined) {
          _relativePath = ['']
        }
        else {
          _relativePath = data.files[j].relativePath.split('/')
        }
        if (j == 0) {
          markFolderChange = _relativePath[0]
        }
        /**优化判断拖拽非单文件夹,优化检测性能 hjz**/
        if (j == totalFiles - 1) {
          //判断第一个文件路径与最后一个文件的文件路径不匹配时，马上返回false不允许上传
          if (markFolderChange != _relativePath[0]) {
            notAllowUpload()
            //console.log("not match");
            return false
          } else if (markFolderChange != '' && (markFolderChange == _relativePath[0])) {
            /**doNothing 如果同一个文件夹，则什么都不做**/
            //console.log("nothing");
          } else {
            /**其它情况**/
            //console.log("other scence");
            /**以下检测拖拽是否带有多于一个文件夹的情况下上传**/
            for (var i = 0; i < totalFiles; i++) {
              if (data.files[i].relativePath == undefined) {
                _relativePath = ['']
              }
              else {
                _relativePath = data.files[i].relativePath.split('/')
              }
              if (i == 0) {
                markFolderChange = _relativePath[0]
              }

              if (markFolderChange != '' && _relativePath[0] != '') {
                if (markFolderChange != _relativePath[0]) {
                  notAllowUpload()
                  return false
                }
              }
              if (markFolderChange == '' && _relativePath[0] != '') {
                notAllowUpload()
                return false
              }
              if (markFolderChange != '' && _relativePath[0] == '') {
                notAllowUpload()
                return false
              }
            }
          }
        }
      }

//            if (totalFiles > 2500) {
//                alert("抱歉，由于浏览器性能原因，请勿上传超过2500个文件！");
//                return false;
//            }
      if (totalFiles > 100) {
        $('.tips').html('正在扫描...')
        $('#message').show()
        if (window.confirm('超过100个文件，上传需要花费较长时间，是否继续？')) {
        } else {
          $('.tips').html('将文件拖拽到这里')
          $('#message').hide()
          return false
        }
      }
    },
    add: function(e, data) {






      if (!isUploadFolder) {
        return false
      }

      parentId = location.href.split('#')[1].split('&')[0].split('=')[1]
      if (parentId < 0) {
        parentId = 0
      }
      //drop完会走这里
      if (data.paramName != undefined || typeof(data.fileInput) == 'object') {
        _saveData = []
        _savePath = []
        _folderIdArr = []
        $this = $(this),
          that = $this.data('blueimp-fileupload') ||
            $this.data('fileupload'), options = that.options
        $('#message').show()
      }
      _saveData.push(data)//把要上传的data先保存下载，等创建文件夹后再上传

      if (e.isDefaultPrevented()) {
        return false
      }


      data.context = that._renderUpload(data.files)
      _element = $(_element).add(data.context)//串联jquery对象
      countfile++
      if (countfile == totalFiles) {
        countfile = 0
        $('.tips').html('正在扫描文件...')
        _createFolder(groupId, parentId)//创建并上传文件
      }
      if (typeof(data.fileInput) == 'object') {
        options.filesContainer[
          options.prependFiles ? 'prepend' : 'append'
          ]($(_element))
        $.each(_saveData, function(i, o) {
          var _this = this
          $('#parentId').val(parentId)
          $(_element).eq(i).data('data', _this)
          _this.submit()
        })
        $('#message').hide()
        $('.tips').hide()
        _element = ''
        data.submit()
      }
    }
  }).on('fileuploadprogressall', function(i, o) {
  }).on('fileuploaddone', function() {
    setTimeout(function() {
      if ($('.template-upload').length == 0) {
        $('.tips').html(browserTips)
        $('.tips ').show()
        isUploadFolder = true
        resultsArr = []
        arr = []
        countfile = 0
        totalFiles = 0
        countFolder = 0
        coutFolderOut = 0
        countCreateFolder = 0
        entries = []
        window.parent.uploadComplete()
        window.location.reload()
        // parent.uploadComplete();
      }
    }, 1000)
  }).on('fileuploadfail', function(e, data) {
    try {
      window.parent.uploadError(JSON.parse(data.jqXHR.responseText).detail)
      if (JSON.parse(data.jqXHR.responseText).detail === '请登录后再进行操作！') {
        window.parent.logout()
      }
    } catch (e) {
      console.log(e)
    }

    // console.log(JSON.parse(data.jqXHR.responseText).detail)
  })


  /**hjz创建文件夹**/
  function _createFolder(groupId, parentId) {

    //如果是ie10/ie11 以上版本，则直接上传
    if (/MSIE\s+11.0/i.test(navigator.userAgent) || (!!window.ActiveXObject || 'ActiveXObject' in window && typeof(attachEvent) == 'undefined') || /firefox/.test(navigator.userAgent.toLowerCase()) || (/safari/.test(navigator.userAgent.toLowerCase()) && !/chrome/.test(navigator.userAgent.toLowerCase()))) {
      $('#message').hide()
      $('.tips').hide()

      options.filesContainer[
        options.prependFiles ? 'prepend' : 'append'
        ]($(_element))

      $.each(_saveData, function(i, o) {
        var _this = this
        $('#parentId').val(parentId)
        $(_element).eq(i).data('data', _this)
        _this.submit()

      })
      _element = ''
      return false
    }

    var _entry = $('#fileupload').fileupload('option', 'entry')
    if (_entry.isFile) { //如果带有单个文件
      $('#message').hide()
      $('.tips').hide()

      options.filesContainer[
        options.prependFiles ? 'prepend' : 'append'
        ]($(_element))

      $('.tips').html('')
      //上传文件
      $.each(_saveData, function(i, o) {
        var _this = this
        $('#parentId').val(parentId)
        $(_element).eq(i).data('data', _this)
        _this.submit()
      })
      _element = ''
      return false
    }
    /**以上为单个文件的传输**/


    /**以下为带有文件夹的传输**/
    isUploadFolder = false
    var dirReader = _entry.createReader()
    parentId = location.href.split('#')[1].split('&')[0].split('=')[1]
    if (parentId < 0) {
      parentId = 0
    }

    //创建父级目录
    $.ajax({
      url: '/group/createDir',
      async: false,
      dataType: 'json',
      type: 'post',
      data: 'groupId=' + groupId + '&parentId=' + parentId + '&name=' + _entry.name,
      success: function(data) {
        parentId = data.id//重新复制parentid
        //_savePath.push([_entry.fullPath, data.id]);
        _folderIdArr.push({ 'id': data.id, 'path': _entry.fullPath })//记录parentid
      },
      fail: function(data) {
        console.log(data)
      }
    })

    setTimeout(function() {
      var readEntries = function() {
        dirReader.readEntries(function(results) {
          if (!results.length) {
            if (entries[_countEntry] == undefined) {
              for (var i = 0; i < entries.length; i++) {
                resultsArr.push(entries[i].fullPath)//读取fullPath
              }
              readFolder(folderParentId)
              return false
            }
            dirReader = entries[_countEntry].createReader()
            _countEntry++
            readEntries()
          } else {
            for (i = 0; i < results.length; i++) {
              if (results[i].isDirectory) {
                entries.push(results[i])
              }
            }
            readEntries()
          }
        }, function(e) {
        })
      }
      readEntries()
    }, 2000)


  }

  function readFolder(parentId) {
    if (countCreateFolder != resultsArr.length) {
      var _f = entries[coutFolderOut]
      var _myPath = _f.fullPath.substring(0, _f.fullPath.lastIndexOf('/'))
      for (var i = 0; i < _folderIdArr.length; i++) {
        if (_folderIdArr[i].path == _myPath) {
          parentId = _folderIdArr[i].id
        }
      }
      createFolderAjax(parentId, _f.name)
    } else {
      //上传文件
      setTimeout(function() {
        $('.tips').hide()
        $('#message').hide()
        options.filesContainer[
          options.prependFiles ? 'prepend' : 'append'
          ]($(_element))
        $('.tips').html('')
        var _length = _folderIdArr.length
        var _saveDataPath = _saveData.length
        $.each(_saveData, function(i, o) {
          var _this = this
          var _getParentId
          for (var j = 0; j < _length; j++) {
            if (_folderIdArr[j].path + '/' == '/' + _this.files[0].relativePath) {
              _getParentId = _folderIdArr[j].id
            }
          }
          //console.log(_getParentId + "," + _this.files[0].relativePath);
          if (_getParentId != undefined) {
            $('#parentId').val(_getParentId)
            $(_element).eq(i).data('data', _this)
            _this.submit()
          }
          if (i == _saveDataPath - 1) {
            _element = ''
          }
        })
      }, 1000)
    }
  }

  function createFolderAjax(parentId, name) {
    var _arrLength = resultsArr.length
    if (parentId < 0) {
      parentId = 0
    }
    //创建子目录
    $.ajax({
      url: '/group/createDir',
      async: true,
      dataType: 'json',
      type: 'post',
      data: 'groupId=' + groupId + '&parentId=' + parentId + '&name=' + name,
      success: function(data) {
        _folderIdArr.push({ 'id': data.id, 'path': entries[coutFolderOut].fullPath })
        //_savePath.push([entries[coutFolderOut].fullPath, data.id]);
        countCreateFolder++//创建文件夹计数
        $('.tips').html('正在创建...' + Math.ceil(countCreateFolder / (_arrLength) * 100) + '%' + '<br/>' + countCreateFolder + '/' + (_arrLength) + '个文件夹')
        coutFolderOut++
        readFolder(data.id)
      },
      fail: function(data) {
        console.log(data)
      }
    })
  }


  $('#files').bind('click', function(e) {
    var target = $(e.target)
    if (target.is('.onecancel')) {
      if ($('#files').find('li').length == 0) {
        // parent.uploadComplete();
        window.parent.uploadComplete()
        $('.tips').html(browserTips)
        $('.tips ').show()
        isUploadFolder = true
        isUploadFolder = true
        resultsArr = []
        arr = []
        countfile = 0
        totalFiles = 0
        countFolder = 0
        coutFolderOut = 0
        countCreateFolder = 0
        _countEntry = 0  //_entry计数
        entries = []
      }
    }
  })

  $('.cancelAll').bind('click', function() {
    // parent.uploadComplete();
    window.parent.uploadComplete()
    window.location.reload()
  })

})

function notAllowUpload() {
  alert('抱歉，只能拖拽单个文件夹！')
  $('#message').hide()
  countfile = 0
  countFolder = 0
  totalFiles = 0
  _saveData = []
  _element = ''
  $('.tips').html(browserTips)
}

function getCookie(name) {
  var strcookie = document.cookie//获取cookie字符串
  var arrcookie = strcookie.split('; ')//分割
  //遍历匹配
  for (var i = 0; i < arrcookie.length; i++) {
    var arr = arrcookie[i].split('=')
    if (arr[0] == name) {
      return arr[1]
    }
  }
  return ''
}
