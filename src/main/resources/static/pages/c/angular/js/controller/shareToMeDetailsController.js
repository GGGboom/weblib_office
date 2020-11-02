var shareToMeDetailsController = angular.module('shareToMeDetailsController', ['ngGrid']);
shareToMeDetailsController.controller('shareToMeDetailsController', ['$scope', '$http', '$rootScope', '$state',
  'httpRequest.sendRequest', 'modalWindow', 'modalConfirm', '$timeout', '$compile', '$stateParams',
  function ($scope, $http, $rootScope, $state, sendRequest, modalWindow, modalConfirm, $timeout, $compile, $stateParams) {
    $scope.id = 0;
    $rootScope.floor = 0;
    $scope.isHidden = true;
    $rootScope.AddDir = false;
    $rootScope.Delete = true;
    $rootScope.Upload = false;
    $rootScope.isBack = false;
    $scope.isSearch = false;
    $rootScope.istrash = false;
    $rootScope.isShowUpload = false;
    $scope.event = true;
    $rootScope.mySelections = [];
    $scope.totalServerItems = 0;
    $rootScope.shareDetailParams = JSON.parse(localStorage.saveShareDetailsParams);


    /**分页**/
    $scope.pagingOptions = {
      pageSize: 40,
      currentPage: 1,
      start: 0,
      totalPage: 0,
      totalCount: 0
    };
    //grid初始化设置
    $scope.gridOptions = {
      data: 'data',
      multiSelect: true,
      showSelectionCheckbox: true,
      selectWithCheckboxOnly: true,
      rowHeight: 30,
      headerRowHeight: 25,
      enableSorting: false,
      selectedItems: $rootScope.mySelections,
      enablePaging: true,
      showFooter: false,
      totalServerItems: 'totalServerItems',
      pagingOptions: $scope.pagingOptions,
      init: function () {
        console.log($rootScope.shareDetailParams);
      },
      rowTemplate: '<div ng-mouseover="rowOver()" ng-mouseleave="rowLeave()" ' +
      ' ng-repeat="col in renderedColumns" ng-class="col.colIndex()"' +
      ' class="ngCell {{col.cellClass}}" ng-cell></div>',
      columnDefs: [
        {
          field: 'displayName', displayName: '文件',
          cellTemplate: 'tpl/listTpls/shareToMeCell.html'
          // cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)"' +
          // ' ng-click="selectRow(row,renderedRows)"><img style="position:relative;top:-2px;margin-right:5px;" src={{row.entity|shareicon}} >{{row.getProperty(col.field)}}</span>' +
          // '<span ng-if="row.entity.status !== \'1\' && row.entity.status !== undefined"' +
          // ' class="unReadCountInside"></span></div>'
        },
        {
          field: 'size', width: 200, displayName: '文件大小',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)"' +
          ' ng-click="selectRow(row,renderedRows)"><span ng-cell-text>{{row.getProperty(col.field)|size}}</span></div>'
        }
      ]
    };

    //首次获取列表的判断
    if ($stateParams.id === '') {
      $scope.data = $rootScope.shareDetailParams.resources;//从参数中获取数据
      $rootScope.shareBreadCrumb = [{
        id: 0,
        displayName: '我的接收'
      }, {
        displayName: $rootScope.shareDetailParams.provider,
        id: 'top'
      }];
      $scope.breadcrumbpath = $rootScope.shareBreadCrumb;//设置面包屑
    } else {
      if (localStorage.shareBreadCrumb !== undefined) {
        //如果在本地存储存储中有面包屑，则从里面拿，防止刷新被刷走了
        $rootScope.shareBreadCrumb = JSON.parse(localStorage.shareBreadCrumb);
      }
      //用来判断和裁切面包屑数组
      let __length = $rootScope.shareBreadCrumb.length, _idArr = [];
      for (let i = 0; i < __length; i++) {
        _idArr.push($rootScope.shareBreadCrumb[i].id);//获取到面包屑ID数组
      }
      let __index = _idArr.indexOf(parseInt($stateParams.id));
      if ($rootScope.shareBreadCrumb.length !== __index + 1) {
        //如果返回的时候，要把非当前面包屑的多余数据裁掉
        $rootScope.shareBreadCrumb.splice(__index + 1, $rootScope.shareBreadCrumb.length);
      }
      //从地址栏的参数获取当前一级的分享列表
      sendRequest('/group/getSharedChildResources_v2.action',
        {
          parentId: $stateParams.id
        }, function (res) {
          $scope.data = res.resources;//设置列表数据
          $scope.breadcrumbpath = $rootScope.shareBreadCrumb; //设置面包屑
        });
    }


    $scope.selectRow = function (row, renderedRows) {
      if ($scope.event) {
        renderedRows.forEach(function (o, i) {
          o.selected = false;
          o.orig.selected = false;
        });
        // for (var i in renderedRows) {
        //   renderedRows[i].selected = false;
        //   renderedRows[i].orig.selected = false;
        // }
        row.orig.selected = true;
        row.selected = true;
        $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
        $scope.gridOptions.selectedItems.push(row.entity);
        if (row.entity.id !== -1) {
          sendRequest('/group/markReceived.action',
            {
              id: row.entity.id
            }, function (res) {
              if (res.code === '200') {
                $rootScope.shareDetailParams.resources[row.rowIndex].status = '1';
                $scope.data = $rootScope.shareDetailParams.resources;
                $rootScope.getReadCount();
              }
            });
        }
      }

    };
    $scope.onRightClick = function (row, renderedRows) {
      renderedRows.forEach(function (o, i) {
        o.selected = false;
        o.orig.selected = false;
      });
      row.orig.selected = true;
      row.selected = true;
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      $scope.gridOptions.selectedItems.push(row.entity);
      $scope.event = true;
    };

    $scope.Close = function () {
      $scope.event = true;
    };

    $scope.goToState = function (id) {
      let __length = $scope.breadcrumbpath.length, __idArr = [];
      for (let i = 0; i < __length; i++) {
        __idArr.push($scope.breadcrumbpath[i].id);
      }
      let _index = __idArr.indexOf(id) + 1;
      history.go(-($scope.breadcrumbpath.length - _index));
    };


    //获取分享给我的列表 hjz
    $scope.getResourceAsync = function (pageSize, page) {
      //获取资源列表
      sendRequest('/group/getMyReceive.action',
        { limit: pageSize, start: page, memberId: 0 }, function (data) {
          // $rootScope.progressbar.complete();
          $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
          $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
          // console.log($scope.pagingOptions.totalPage)
          $scope.data = data.shareRecords;
          $rootScope.thumbnail = [];
          $rootScope.isBack = false;
          $scope.listOptions.parentId = data.parentId;
        });
    };
    $scope.update = function () {
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      $scope.getResourceAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
    };


    $scope.getSharedChildResources = function (pageSize, page, id) {
      if (id) {
        //获取资源列表
        sendRequest('/group/getSharedChildResources_v2.action',
          { parentId: id }, function (data) {
            $scope.data = data.resources;
          });
      }
    };


    $rootScope.multiSelectDownload = function () {
      if ($scope.gridOptions.selectedItems.length > 0) {
        var selectedId = '';
        for (var x in $scope.gridOptions.selectedItems) {
          if ($scope.gridOptions.selectedItems[x].resourceId === undefined) {
            $scope.gridOptions.selectedItems[x].resourceId = $scope.gridOptions.selectedItems[x].id;
          }
          selectedId = selectedId + ($scope.gridOptions.selectedItems[x].resourceId) + '&id=';
        }
        selectedId = selectedId.substring(0, selectedId.length - 4);
        $scope.download(selectedId);
      } else {
        toastr['warning']('请先选择一个条目！');
      }
    };

    $scope.recycle = function (id) {
      // $rootScope.progressbar.start();
      sendRequest('/group/deleteReceivedResource_v2.action',
        id, function (data) {
          if (data.type == 'success') {
            // $rootScope.progressbar.complete();
            // $scope.update();
            history.go(-(document.querySelectorAll('.breadcrumb li').length - 1));
            $rootScope.getReadCount();
            toastr['success']('删除成功！');
          } else toastr['error']('删除失败！');
        });
    };

    $scope.deleteFile = function (x) {
      modalConfirm.confirm({
        title: '是否删除 ' + x.displayName.replace(/(&nbsp;)/g, ' ') + ' 这条接收内容 ?',
        onConfirm: function (data) {
          $scope.recycle(x.id);
        },
        onCancel: function (data) {
        }
      });
    };

    $rootScope.multiSelectDelete = function () {
      modalConfirm.confirm({
        title: '是否删除整条来自' + $rootScope.shareDetailParams.provider + '分享的内容?',
        onConfirm: function (data) {
          let __id = '';
          $rootScope.shareDetailParams.resources.forEach(function (value, index) {
            __id += 'id=' + value.id + '&';
          });
          $scope.recycle(__id.substring(0, __id.length - 1));

        },
        onCancel: function (data) {
        }
      });
    };

    $scope.searchResources = function (pageSize, page, x, categoryId, owner, upCreationDate, downCreationDate) {
      sendRequest('/group/searchResources.action', {
        start: page,
        limit: pageSize,
        query: x,
        categoryId: categoryId,
        owner: owner,
        upCreationDate: upCreationDate,
        downCreationDate: downCreationDate
      }, function (data) {
        // $rootScope.progressbar.complete();
        $scope.totalServerItems = data.totalCount;
        $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
        $scope.data = data.resources;
        $scope.breadcrumbpath = [{ id: -$rootScope.status.personGroupId, displayName: '搜索结果' }];
        $rootScope.thumbnail = [];
        var imageId = [];
        for (var i = 0; i < data.resources.length; i++) {
          if (data.resources[i].type == 'file') {
            var fileName = data.resources[i].displayName;
            var end = fileName.substring(fileName.lastIndexOf('.') + 1,
              fileName.length).toLowerCase();
            if (end == ('jpg') || end == ('gif') || end == ('png')
              || end == ('jpeg') || end == ('bmp'))
              imageId.push(data.resources[i].id);
          }
        }
        if (imageId.length > 0) {
          sendRequest('/group/getThumbnail.action', {
            width: 25,
            height: 25,
            quality: 0,
            id: imageId
          }, function (data) {
            $rootScope.thumbnail = data;
          });
        }
      });
    };

    $rootScope.searchBtn = function (x, categoryId, owner, upCreationDate, downCreationDate) {
      if (x !== '') {
        $scope.searchData = {};
        $scope.searchData.query = x;
        $scope.searchData.categoryId = categoryId;
        $scope.searchData.owner = owner;
        $scope.searchData.upCreationDate = upCreationDate;
        $scope.searchData.downCreationDate = downCreationDate;
        $scope.isSearch = true;
        $rootScope.isBack = false;
        $scope.pagingOptions.currentPage = 1;
        $scope.searchResources($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.searchData.query, $scope.searchData.categoryId, $scope.searchData.owner, $scope.searchData.upCreationDate, $scope.searchData.downCreationDate);
      }
    };

    $rootScope.advancedSearch = function () {
      modalWindow.open({
        templateUrl: 'tpl/advancedSearch.html',
        controller: 'advancedSearchController',
        title: '高级搜索',
        width: 460,
        height: 253,
        resolve: {
          items: function () {
            return { 'scope': $scope };
          }
        }
      });
    };
    $scope.fileChain = function (id) {
      if ($scope.gridOptions.selectedItems.length > 1) {
        modalConfirm.confirm({
          title: '您将会批量生成' + $scope.gridOptions.selectedItems.length + '个文件外链 ',
          onConfirm: function (data) {
            var selectedId = [];
            for (var x in $scope.gridOptions.selectedItems) {
              selectedId.push($scope.gridOptions.selectedItems[x].id)
            }
            if (document.querySelector('#multiLinkCode').checked) {
              $scope.multiFileChan(selectedId, 1);
            } else {
              $scope.multiFileChan(selectedId, 0);
            }
          },
          onCancel: function (data) {
          }
        });

        $timeout(function () {
          angular.element(document.querySelector('#am_confirm span')).append('&nbsp;<input type="checkbox" checked id="multiLinkCode"/> 批量加密？');
          document.querySelectorAll('.am-modal-confirm .am-modal-btn')[0].innerHTML = '生成';
        }, 100);
      } else {
        // $scope.event = true;
        modalWindow.open({
          templateUrl: 'tpl/fileChain.html',
          controller: 'fileChainController',
          title: '文件外链',
          width: 600,
          height: 420,
          resolve: {
            items: function () {
              return id;
            }
          }
        });
      }
    };
    $scope.multiFileChan = function (arr, code) {

      var idStr = '';
      for (var i = 0; i < arr.length; i++) {
        idStr = idStr + 'id=' + arr[i] + '&';
      }

      location.href = Config.sitePath + '/webmail/exportTokenDownloadUrl.action?' + idStr + 'setCode=' + code;

    };
    $scope.Email = function (id) {
      $scope.event = true;
      id.id = id.resourceId;
      modalWindow.open({
        templateUrl: 'tpl/email.html',
        controller: 'emailController',
        title: '邮件快递',
        width: 800,
        height: 530,
        resolve: {
          items: function () {
            return [id];
          }
        }
      });
    };
    $scope.moveCopy = function (x, title, y) {
      var id = [];
      if (y == 1) {
        id.push(x);
      }
      else {
        for (var i in $scope.gridOptions.selectedItems) {
          id.push($scope.gridOptions.selectedItems[i].resourceId);
        }
      }
      if (id.length > 0) {
        modalWindow.open({
          templateUrl: 'tpl/moveCopy.html',
          controller: 'moveCopyController',
          title: title,
          width: 500,
          height: 600,
          resolve: {
            items: function () {
              return { 'resourceId': id, 'title': title, 'scope': $scope };
            }
          }
        });
      }
    };

    //下载资源
    $scope.download = function (id, subID) {
      if (id === undefined && subID !== undefined) {
        id = subID;
      }
      location.href = $rootScope.path + '/group/downloadResource.action?id=' + id;
    };


    $scope.rowOver = function () {
      this.isHidden = false;
    };
    $scope.rowLeave = function () {
      this.isHidden = true;
    };

    $scope.title = function (data, index) {
      if (data.type === 1) {
        $state.go('main.shareToMeDetails', { id: data.resourceId });
        $rootScope.shareBreadCrumb.push({
          displayName: data.displayName,
          id: data.resourceId
        });
        localStorage.shareBreadCrumb = JSON.stringify($rootScope.shareBreadCrumb);
      } else if (data.type === 2) {
        $scope.download(data.resourceId);
      }
    };

    $scope.subFolder = function (data) {
      console.log(data)
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      $scope.pagingOptions.currentPage = 1;
      if ($rootScope.floor == 0) {
        $scope.getSharedChildResources($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, data);
        $rootScope.floor++;
      }
      else {
        $scope.getSharedChildResources($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, data);
        $rootScope.floor++;
      }
    }

    $rootScope.back = function (id) {
      history.go(-1);
    };
    GridLayout.init();
  }])
;

shareToMeDetailsController.filter('shareicon', function ($rootScope) {
  return function (icon) {
    if (icon.type === 1 || icon.type === 'folder') {
      return 'images/folder.png';
    } else if (icon.type === 2 || icon.type === 'file') {
      var end = icon.displayName.substring(icon.displayName.lastIndexOf('.') + 1,
        icon.displayName.length).toLowerCase();
      if (end === 'zip' || end === 'rar' || end === ('jar') || end === ('7z') || end === ('gz') || end === ('iso')) {
        return 'images/zip.png';
      } else if (end === ('jpg') || end === ('gif') || end === ('png') || end === ('jpeg') || end === ('bmp')) {
        if ($rootScope.thumbnail.length > 0) {
          for (var x in $rootScope.thumbnail) {
            if (icon.resourceId === $rootScope.thumbnail[x].id || icon.id === $rootScope.thumbnail[x].id) {
              return $rootScope.path + $rootScope.thumbnail[x].thumbUrl;
            }
          }
        }
        else {
          return 'images/image.png';
        }
      } else if (end === ('m4a') || end === ('mp3') || end === ('mid') || end === ('xmf') ||
        end === ('ogg') || end === ('wav')) {
        return 'images/audio.png';
      } else if (end === 'txt') {
        return 'images/txt.png';
      } else if (end === ('pdf')) {
        return 'images/pdf.png';
      } else if (end === ('doc') || end === ('docx')) {
        return 'images/doc.png';
      } else if (end === ('xls') || end === ('xlsx')) {
        return 'images/xls.png';
      } else if (end === ('ppt') || end === ('pptx')) {
        return 'images/ppt.png';
      } else if (end === ('apk')) {
        return 'images/apk.png';
      } else if (end === ('3gp') || end === ('mp4') || end === ('avi') || end === ('rmvb') || end === ('wmv') ||
        end === ('mkv') || end === ('flv') || end === ('mpg')) {
        return 'images/video.png';
      } else if (end === ('exe')) {
        return 'images/exe.png';
      }
      else {
        return 'images/default.png';
      }
    }
    else if (icon.type === 3) {
      return 'images/link.png';
    }
  };
});

/**主界面布局JS**/
var GridLayout = {
  init: function ($scope) {
    var _this = this;
    _this.resize();
    $(window).bind('resize', function () {
      _this.resize();
    });
    // window.onresize = function ($scope) {
    // console.log($scope.gridOptions);
    // $scope.gridOptions.$gridServices.DomUtilityService.RebuildGrid(
    //   $scope.gridOptions.$gridScope,
    //   $scope.gridOptions.ngGrid
    // );
    // }
  },
  resize: function () {
    setTimeout(function () {
      $('.wrap .contentWrap #right').width($(window).width() - 195);
    }, 200);
    //var _height = document.body.clientHeight, _headerH = document.getElementById("header").offsetHeight;
    //$(".gridStyle").height(_height - _headerH - 40);
    //document.getElementById("left").style.height = (_height - _headerH) + "px";
    //document.getElementById("right").style.height = (_height - _headerH) + "px";
  }
}
GridLayout.init();