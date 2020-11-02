var shareToMeController = angular.module('shareToMeController', ['ngGrid', 'ng-context-menu']);
shareToMeController.controller('shareToMeController', ['$scope', '$http', '$rootScope', '$state',
  'httpRequest.sendRequest', 'modalWindow', 'modalConfirm', '$timeout', '$compile',
  function ($scope, $http, $rootScope, $state, sendRequest, modalWindow, modalConfirm, $timeout, $compile) {
    $scope.id = 0;
    $rootScope.floor = 0;
    $scope.isHidden = true;
    $rootScope.AddDir = false;
    $rootScope.Delete = true;
    $rootScope.Upload = false;
    $rootScope.isBack = true;
    $scope.isSearch = false;
    $rootScope.istrash = false;
    $rootScope.isShowUpload = false;
    $scope.event = true;
    $rootScope.mySelections = [];
    $scope.totalServerItems = 0;
    $scope.breadcrumbpath = [{ id: -$rootScope.status.personGroupId, displayName: '我的接收' }];
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
      // groups: ['provider','shareDate'],
      headerRowHeight: 25,
      enableSorting: false,
      selectedItems: $rootScope.mySelections,
      enablePaging: true,
      showFooter: false,
      totalServerItems: 'totalServerItems',
      pagingOptions: $scope.pagingOptions,
      init: function () {
      },
      rowTemplate: '<div  ng-mouseover="rowOver()" ng-mouseleave="rowLeave()"  ng-repeat="col in renderedColumns"' +
      ' ng-class="col.colIndex()" class="ngCell {{col.cellClass}}" ng-cell data-target="menu-{{node}}" ' +
      'ng-cellcontext-menu="onRightClick()" context-menu-close="Close()"></div>',
      columnDefs: [
        {
          field: 'provider', width: 200, displayName: '分享者',
          // cellTemplate: 'tpl/listTpls/shareToMeCell.html'
          cellTemplate: '<div class="ngCellText" ng-click="title(row.entity,row.rowIndex)"' +
          ' ng-click="selectRow(row,renderedRows)">' +
          '<span ng-cell-text><span ng-cell-text>{{row.getProperty(col.field)}}</span>' +
          '<span ng-if="row.entity.readCount !== 0" class="unReadCount">{{row.entity.readCount}}</span>' +
          '</div>'

        },
        {
          field: 'shareDate', width: 160, displayName: '共享时间',
          cellTemplate: '<div class="ngCellText" ng-click="title(row.entity,row.rowIndex)"' +
          ' ng-click="selectRow(row,renderedRows)" style="cursor:default;" >' +
          '<span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
        },
        {
          field: 'shareDate', displayName: '资源',
          cellTemplate: '<div class="ngCellText" ng-click="title(row.entity,row.rowIndex)"' +
          ' ng-click="selectRow(row,renderedRows)" style="cursor:default;" >' +
          '<span ng-cell-text>包含：{{row.entity.resources[0].displayName}} 等 {{row.entity.resources.length}} 条资源</span>' +
          '</div>'
        }
      ]
    };
    $scope.selectRow = function (row, renderedRows) {
      if ($scope.event) {
        for (var i in renderedRows) {
          renderedRows[i].selected = false;
          renderedRows[i].orig.selected = false;
        }
        row.orig.selected = true;
        row.selected = true;
        $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
        $scope.gridOptions.selectedItems.push(row.entity);
      }
    };
    $scope.onRightClick = function () {
      $scope.event = false;
    };
    $scope.Close = function () {
      $scope.event = true;
    };
    /**获取登录状态及个人信息**/
    sendRequest('/user/status.action', {}, function (data) {
      if (data.status === 'login') {
        /**需要个人ID的所有操作，在这里执行**/
        $rootScope.status = data;
        $scope.id = -$rootScope.status.personGroupId;
        $scope.getResourceAsync($scope.pagingOptions.pageSize,
          ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
      } else {
        // window.location.href="http://weblib.ccnl.scut.edu.cn/pages/c/login.jsp";//如果没登陆则跳转到登录页
      }
    });

    //获取分享给我的列表 hjz
    $scope.getResourceAsync = function (pageSize, page) {
      //获取资源列表
      sendRequest('/group/getMyReceive.action',
        { limit: pageSize, start: page, memberId: 0 }, function (data) {
          let _length = data.shareRecords.length;
          for (let i = 0; i < _length; i++) {
            let __length = data.shareRecords[i].resources.length;
            let __count = 0;
            for (let j = 0; j < __length; j++) {
              if (data.shareRecords[i].resources[j].status !== '1') {
                __count++;
              }
            }
            data.shareRecords[i].readCount = __count;
          }
          $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
          $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
          $scope.data = data.shareRecords;
          $rootScope.thumbnail = [];
          $rootScope.isBack = true;
          $scope.breadcrumbpath = [{ id: -$rootScope.status.personGroupId, displayName: '我的接收' }];
          $scope.listOptions.parentId = data.parentId;
        });
    };
    $scope.update = function () {
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      $scope.getResourceAsync($scope.pagingOptions.pageSize,
        ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
    };
    $scope.getPagedDataAsync = function (pageSize, page, id) {
      if (id) {
        // $rootScope.progressbar.start();
        //获取资源列表
        sendRequest('/group/getResources.action',
          { parentId: id, type: 'all', limit: pageSize, start: page }, function (data) {
            // $rootScope.progressbar.complete();
            $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
            $scope.data = data.resources;
            $scope.breadcrumbpath = data.path;
            if (data.path.length > 1) {
              $rootScope.isBack = false;
            } else {
              $rootScope.isBack = true;
            }
            //$(".ngViewport ")[0].scrollTop = 0;
            $scope.listOptions.parentId = data.parentId;
            $rootScope.thumbnail = [];
            var imageId = [];
            for (var i = 0; i < data.resources.length; i++) {
              if (data.resources[i].type == 2) {
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
      }
    };


    $scope.getSharedChildResources = function (pageSize, page, id) {
      if (id) {
        // $rootScope.progressbar.start();
        //获取资源列表
        sendRequest('/group/getSharedChildResources_v2.action',
          { parentId: id, type: 'all', limit: pageSize, start: page }, function (data) {
            // $rootScope.progressbar.complete();
            $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
            $scope.data = data.resources;
            $scope.breadcrumbpath = data.path;
            if (data.path.length > 1) {
              $rootScope.isBack = false;
            } else {
              $rootScope.isBack = true;
            }
            //$(".ngViewport ")[0].scrollTop = 0;
            $scope.listOptions.parentId = data.parentId;
            // $rootScope.thumbnail = [];
            // var imageId = [];
            // for (var i = 0; i < data.resources.length; i++) {
            //   if (data.resources[i].type == 2) {
            //     var fileName = data.resources[i].displayName;
            //     var end = fileName.substring(fileName.lastIndexOf('.') + 1,
            //       fileName.length).toLowerCase();
            //     if (end == ('jpg') || end == ('gif') || end == ('png')
            //       || end == ('jpeg') || end == ('bmp'))
            //       imageId.push(data.resources[i].id);
            //   }
            // }
            // if (imageId.length > 0) {
            //   sendRequest('/group/getThumbnail.action', {
            //     width: 25,
            //     height: 25,
            //     quality: 0,
            //     id: imageId
            //   }, function (data) {
            //     $rootScope.thumbnail = data;
            //   });
            // }
          });
      }
    };


    $rootScope.multiSelectDownload = function () {
      if ($scope.gridOptions.selectedItems.length > 0) {
        var selectedId = '';
        $scope.gridOptions.selectedItems.forEach(function (o, i) {
          o.resources.forEach(function (oo, ii) {
            selectedId += oo.resourceId + '&id=' ;
          });
        });

        // for (var x in $scope.gridOptions.selectedItems) {
        //   if ($scope.gridOptions.selectedItems[x].resourceId === undefined) {
        //     $scope.gridOptions.selectedItems[x].resourceId = $scope.gridOptions.selectedItems[x].id;
        //   }
        //   selectedId = selectedId + ($scope.gridOptions.selectedItems[x].resourceId) + '&id='
        // }
        selectedId = selectedId.substring(0, selectedId.length - 4);
        console.log(selectedId)
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
            $scope.update();
            $rootScope.getReadCount();
            toastr['success']('删除成功！');
          } else {
            toastr['error']('删除失败！');
          }
        });
    };
    $scope.deleteFile = function (x) {
      modalConfirm.confirm({
        title: '是否删除 ' + x.displayName.replace(/(&nbsp;)/g, ' ') + ' 这条接收内容 ?',
        onConfirm: function (data) {
          let _str = '';
          for (let i = 0; i < x.length; i++) {
            _str += 'id=' + x.id + '&';
          }
          console.log(_str);
          //$scope.recycle(x.id);
        },
        onCancel: function (data) {
        }
      });
    };
    $rootScope.multiSelectDelete = function () {
      if ($scope.gridOptions.selectedItems.length > 0)
        modalConfirm.confirm({
          title: '是否删除这' + $scope.gridOptions.selectedItems.length + '条接收内容?',
          onConfirm: function (data) {
            let _str = '';

            var selectedId = [];

            $scope.gridOptions.selectedItems.forEach(function (value, index) {
              for (let i = 0; i < value.resources.length; i++) {
                _str += 'id=' + value.resources[i].id + '&';
              }
            });
            $scope.recycle(_str.substring(0, _str.length - 1));
          },
          onCancel: function (data) {
          }
        });
    };
    $scope.searchResources = function (pageSize, page, x, categoryId, owner, upCreationDate, downCreationDate) {
      // $rootScope.progressbar.start();
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
      if (x != '') {
        $scope.searchData = {};
        $scope.searchData.query = x;
        $scope.searchData.categoryId = categoryId;
        $scope.searchData.owner = owner;
        $scope.searchData.upCreationDate = upCreationDate;
        $scope.searchData.downCreationDate = downCreationDate;
        $scope.isSearch = true;
        $rootScope.isBack = false;
        $scope.pagingOptions.currentPage = 1;
        $scope.searchResources($scope.pagingOptions.pageSize,
          ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize,
          $scope.searchData.query, $scope.searchData.categoryId, $scope.searchData.owner,
          $scope.searchData.upCreationDate, $scope.searchData.downCreationDate);
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
            // var selectedId = [];
            // for (var x in $scope.gridOptions.selectedItems) {
            //   selectedId.push($scope.gridOptions.selectedItems[x].id)
            // }
            // $scope.multiFileChan(selectedId, 0);
          }
        });

        $timeout(function () {
          angular.element(document.querySelector('#am_confirm span'))
            .append('&nbsp;<input type="checkbox" checked id="multiLinkCode"/> 批量加密？');
          document.querySelectorAll('.am-modal-confirm .am-modal-btn')[0].innerHTML = '生成';
          // document.querySelectorAll('.am-modal-confirm .am-modal-btn')[1].innerHTML = '不加密';
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
        idStr = idStr + 'id=' + arr[i] + '&'
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
            return id;
          }
        }

      })
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
    $scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
      if (newVal !== oldVal && newVal !== oldVal) {
        if ($scope.isSearch)
          $scope.searchResources($scope.pagingOptions.pageSize,
            ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.searchData.query,
            $scope.searchData.categoryId, $scope.searchData.owner, $scope.searchData.upCreationDate,
            $scope.searchData.downCreationDate)
        else if ($rootScope.floor == 0) {
          $scope.getResourceAsync($scope.pagingOptions.pageSize,
            ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.listOptions.parentId, false);
        }
        else {
          $scope.getPagedDataAsync($scope.pagingOptions.pageSize,
            ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.listOptions.parentId, false);
        }
      }
    });
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
    $scope.prev = function () {
      if ($scope.pagingOptions.currentPage == 1)
        return false;
      $scope.pagingOptions.currentPage--;
    };
    $scope.next = function () {
      if ($scope.pagingOptions.currentPage == $scope.pagingOptions.totalPage)
        return false;
      $scope.pagingOptions.currentPage++;
    };
    $scope.first = function () {
      $scope.pagingOptions.currentPage = 1;
    };
    $scope.end = function () {
      $scope.pagingOptions.currentPage = $scope.pagingOptions.totalPage;
    };
    $scope.title = function (data, index) {
      $state.go('main.shareToMeDetails');
      $rootScope.shareDetailParams = data;
      localStorage.saveShareDetailsParams = JSON.stringify(data);
    };

    $scope.subFolder = function (data) {
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      $scope.pagingOptions.currentPage = 1;
      if ($rootScope.floor == 0) {
        $scope.getSharedChildResources($scope.pagingOptions.pageSize,
          ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, data);
        $rootScope.floor++;
      }
      else {
        $scope.getSharedChildResources($scope.pagingOptions.pageSize,
          ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, data);
        $rootScope.floor++;
      }
    }

    $rootScope.back = function (id) {
      if ($scope.isSearch) {
        $scope.isSearch = false;
        $rootScope.isBack = true;
        $scope.pagingOptions.currentPage = 1;
        $rootScope.floor = 0;
        $scope.getResourceAsync($scope.pagingOptions.pageSize,
          ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
      }
      else if ($rootScope.floor == 0) {

      } else if ($rootScope.floor == 1) {
        $scope.getResourceAsync($scope.pagingOptions.pageSize,
          ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
        $rootScope.floor--;
      }
      else {
        // $rootScope.progressbar.start();
        sendRequest('/group/getResources.action',
          {
            parentId: id,
            back: true,
            type: 'all',
            limit: $scope.pagingOptions.pageSize,
            start: $scope.pagingOptions.start
          }, function (data) {
            // $rootScope.progressbar.complete();
            $scope.totalServerItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
            $scope.data = data.resources;
            $scope.breadcrumbpath = data.path;
            if (data.path.length > 1) $rootScope.isBack = false; else $rootScope.isBack = true;
            $scope.listOptions.parentId = data.parentId;
            $scope.pagingOptions.currentPage = 1;
            $rootScope.thumbnail = [];
            var imageId = [];
            for (var i = 0; i < data.resources.length; i++) {
              if (data.resources[i].type == 2) {
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
        $rootScope.floor--;
      }
    };
    GridLayout.init();
  }]);
shareToMeController.filter('shareicon', function ($rootScope) {
  return function (icon) {
    if (icon.type == 1 || icon.type == 'folder') {
      return 'images/folder.png'
    } else if (icon.type == 2 || icon.type == 'file') {
      var end = icon.displayName.substring(icon.displayName.lastIndexOf('.') + 1,
        icon.displayName.length).toLowerCase();
      if (end == 'zip' || end == 'rar' || end == ('jar') || end == ('7z') || end == ('gz') || end == ('iso')) {
        return 'images/zip.png';
      } else if (end == ('jpg') || end == ('gif') || end == ('png') || end == ('jpeg') || end == ('bmp')) {
        if ($rootScope.thumbnail.length > 0) {
          for (var x in $rootScope.thumbnail) {
            if (icon.resourceId == $rootScope.thumbnail[x].id || icon.id == $rootScope.thumbnail[x].id) {
              return $rootScope.path + $rootScope.thumbnail[x].thumbUrl;
            }
          }
        }
        else return 'images/image.png';
      } else if (end == ('m4a') || end == ('mp3') ||
        end == ('mid') || end == ('xmf') || end == ('ogg') || end == ('wav')) {
        return 'images/audio.png';
      } else if (end == 'txt') {
        return 'images/txt.png';
      } else if (end == ('pdf')) {
        return 'images/pdf.png';
      } else if (end == ('doc') || end == ('docx')) {
        return 'images/doc.png';
      } else if (end == ('xls') || end == ('xlsx')) {
        return 'images/xls.png';
      } else if (end == ('ppt') || end == ('pptx')) {
        return 'images/ppt.png';
      } else if (end == ('apk')) {
        return 'images/apk.png';
      } else if (end == ('3gp') || end == ('mp4') || end == ('avi') ||
        end == ('rmvb') || end == ('wmv') || end == ('mkv') || end == ('flv') || end == ('mpg')) {
        return 'images/video.png'
      } else if (end == ('exe')) {
        return 'images/exe.png';
      }
      else    return 'images/default.png';
    }
    else if (icon.type == 3) {
      return 'images/link.png';
    }
  }
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