var myshareController = angular.module("myshareController", ["ngGrid"]);
myshareController.controller("myshareController", ["$scope", "$http", "$rootScope", "$state", "httpRequest.sendRequest", "modalWindow", "modalConfirm",
  function ($scope, $http, $rootScope, $state, sendRequest, modalWindow, modalConfirm) {
    $scope.id = 0;
    $scope.isHidden = true;
    $scope.event = true;
    $rootScope.AddDir = false;
    $rootScope.Delete = true;
    $rootScope.Upload = false;
    $rootScope.isBack = true;
    $scope.isSearch = false;
    $rootScope.istrash = false;
    $rootScope.floor = 0;
    $rootScope.mySelections = [];
    $scope.totalServerItems = 0;
    /**分页**/
    $scope.pagingOptions = {
      pageSize: 30,
      currentPage: 1,
      start: 0,
      totalPage: 0,
      totalCount: 0
    };
    //grid初始化设置
    $scope.gridOptions = {
      data: "data",
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

        // angular.element(document.querySelector(".ngViewport")).height(angular.element(window).height() - 120);
        // angular.element(window).resize(function () {
        //   angular.element(document.querySelector(".ngViewport")).height(angular.element(window).height() - 125);
        // });

      },
      rowTemplate: '<div ng-mouseover="rowOver()" ng-mouseleave="rowLeave()"  ng-repeat="col in renderedColumns" ng-class="col.colIndex()" class="ngCell {{col.cellClass}}" ng-cell></div>',
      columnDefs: [
        {
          field: "displayName", displayName: '名称',
          cellTemplate: 'tpl/listTpls/myShareCell.html'
        },
        {
          field: "size", width: 80, displayName: '文件大小',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)" ng-click="selectRow(row,renderedRows)" style="cursor:default;text-align: right;padding-right: 10px;"><span ng-cell-text>{{row.getProperty(col.field)|size}}</span></div>'
        },
        {
          field: "shareType", width: 80, displayName: '共享类型',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)" ng-click="selectRow(row,renderedRows)" style="cursor:default;"><span ng-cell-text>{{row.getProperty(col.field) |shareType }}</span></div>'
        },
        {
          field: "memberName", width: 80, displayName: '共享对象',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)" ng-click="selectRow(row,renderedRows)" style="cursor:default;"><span ng-cell-text>{{row.getProperty(col.field) }}</span></div>'
        },
        {
          field: "shareDate", width: 120, displayName: '共享时间',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)" ng-click="selectRow(row,renderedRows)" style="cursor:default;"><span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
        },
        {
          field: "expiredDate", width: 120, displayName: '失效时间',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)" ng-click="selectRow(row,renderedRows)" style="cursor:default;"><span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
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
      if (data.status == 'login') {
        /**需要个人ID的所有操作，在这里执行**/
        $rootScope.status = data;
        $scope.id = -$rootScope.status.personGroupId;
        $scope.getResourceAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
      } else {
        // window.location.href="http://weblib.ccnl.scut.edu.cn/pages/c/login.jsp";//如果没登陆则跳转到登录页
      }
    });
    $scope.update = function () {
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      $scope.getResourceAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
    };
    $scope.getResourceAsync = function (pageSize, page) {
      // $rootScope.progressbar.start();
      //获取资源列表
      sendRequest("/group/getMySharedResources.action",
        { limit: pageSize, start: page }, function (data) {
          // $rootScope.progressbar.complete();
          $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
          $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
          $scope.data = data.resources;
          $rootScope.isBack = true;
          $scope.breadcrumbpath = [{ id: -$rootScope.status.personGroupId, displayName: "我的分享" }];
          $rootScope.thumbnail = [];
          //$(".ngViewport ")[0].scrollTop = 0;
          $scope.listOptions.parentId = data.parentId;
          var imageId = [];
          for (var i = 0; i < data.resources.length; i++) {
            if (data.resources[i].type == 2) {
              var fileName = data.resources[i].displayName;
              var end = fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length).toLowerCase();
              if (end == ("jpg") || end == ("gif") || end == ("png")
                || end == ("jpeg") || end == ("bmp"))
                imageId.push(data.resources[i].resourceId);
            }
          }
          if (imageId.length > 0) {
            sendRequest("/group/getThumbnail.action", {
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

    $scope.getPagedDataAsync = function (pageSize, page, id) {
      if (id) {
        // $rootScope.progressbar.start();
        //获取资源列表
        sendRequest("/group/getResources.action",
          { parentId: id, type: "all", limit: pageSize, start: page }, function (data) {
            // $rootScope.progressbar.complete();
            $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
            $scope.data = data.resources;
            $scope.breadcrumbpath = data.path;
            if (data.path.length > 1) $rootScope.isBack = false; else $rootScope.isBack = true;
            //$(".ngViewport ")[0].scrollTop = 0;
            $scope.listOptions.parentId = data.parentId;
            $rootScope.thumbnail = [];
            var imageId = [];
            for (var i = 0; i < data.resources.length; i++) {
              if (data.resources[i].type == 2) {
                var fileName = data.resources[i].displayName;
                var end = fileName.substring(fileName.lastIndexOf(".") + 1,
                  fileName.length).toLowerCase();
                if (end == ("jpg") || end == ("gif") || end == ("png")
                  || end == ("jpeg") || end == ("bmp"))
                  imageId.push(data.resources[i].id);
              }
            }
            if (imageId.length > 0) {
              sendRequest("/group/getThumbnail.action", {
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

    $scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
      if (newVal !== oldVal && newVal !== oldVal) {
        if ($scope.isSearch)
          $scope.searchResources($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.searchData.query, $scope.searchData.categoryId, $scope.searchData.owner, $scope.searchData.upCreationDate, $scope.searchData.downCreationDate)
        else if ($rootScope.floor == 0)
          $scope.getResourceAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
        else $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.listOptions.parentId);
      }
    });
    //下载资源
    $scope.download = function (id, subID) {
      if (id === undefined && subID !== undefined) {
        id = subID;
      }
      location.href = $rootScope.path + "/group/downloadResource.action?id=" + id;
    };
    $rootScope.multiSelectDownload = function () {
      if ($scope.gridOptions.selectedItems.length > 0) {
        var selectedId = "";
        for (var x in $scope.gridOptions.selectedItems) {
          selectedId = selectedId + ($scope.gridOptions.selectedItems[x].resourceId) + "&id="
        }
        selectedId = selectedId.substring(0, selectedId.length - 4);
        $scope.download(selectedId);
      }
    };
    $scope.recycle = function (id) {
      // $rootScope.progressbar.start();
      sendRequest("/group/deleteSharedResource.action",
        { id: id }, function (data) {
          if (data.type == "success") {
            // $rootScope.progressbar.complete();
            $scope.update();
            toastr["success"]("删除成功！");
          } else toastr["error"]("删除失败！");
        });
    };
    $scope.deleteFile = function (x) {
      modalConfirm.confirm({
        title: '是否删除 ' + x.displayName.replace(/(&nbsp;)/g, " ") + ' 这条分享内容 ?',
        onConfirm: function (data) {
          $scope.recycle(x.id);
        },
        onCancel: function (data) {
        }
      });
    };
    $scope.searchResources = function (pageSize, page, x, categoryId, owner, upCreationDate, downCreationDate) {
      // $rootScope.progressbar.start();
      sendRequest("/group/searchResources.action", {
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
        $scope.breadcrumbpath = [{ id: -$rootScope.status.personGroupId, displayName: "搜索结果" }];
        $rootScope.thumbnail = [];
        var imageId = [];
        for (var i = 0; i < data.resources.length; i++) {
          if (data.resources[i].type == "file") {
            var fileName = data.resources[i].displayName;
            var end = fileName.substring(fileName.lastIndexOf(".") + 1,
              fileName.length).toLowerCase();
            if (end == ("jpg") || end == ("gif") || end == ("png")
              || end == ("jpeg") || end == ("bmp"))
              imageId.push(data.resources[i].id);
          }
        }
        if (imageId.length > 0) {
          sendRequest("/group/getThumbnail.action", {
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
      if (x != "") {
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
            return { "scope": $scope };
          }
        }
      });
    };
    $scope.fileChain = function (id) {
      $scope.event = true;
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

      })
    };
    $rootScope.multiSelectDelete = function () {
      if ($scope.gridOptions.selectedItems.length > 0)
        modalConfirm.confirm({
          title: '是否删除这' + $scope.gridOptions.selectedItems.length + '条分享内容?',
          onConfirm: function (data) {
            var selectedId = [];
            for (var x in $scope.gridOptions.selectedItems) {
              selectedId.push($scope.gridOptions.selectedItems[x].id)
            }
            $scope.recycle(selectedId);

          },
          onCancel: function (data) {
          }
        });
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
    $scope.title = function (data) {
      if (data.type == 1 || data.type == "folder") {
        //$scope.download(data.resourceId)
        $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
        $scope.pagingOptions.currentPage = 1;
        if ($rootScope.floor == 0) {
          $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, data.resourceId);
          $rootScope.floor++;
        }
        else {
          $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, data.id);
          $rootScope.floor++;
        }
      }
      else if (data.type == 2 || data.type == "file") {
        if (data.resourceId === undefined) {
          data.resourceId = data.id;
        }
        $scope.download(data.resourceId);
        //var suffix = data.displayName.substring(data.displayName.lastIndexOf(".") + 1, data.displayName.length).toLowerCase();
        //if (suffix == "txt" || suffix == "doc" || suffix == "docx" || suffix == "pdf" || suffix == "ppt" || suffix == "pptx" || suffix == "xls" || suffix == "xlsx")
        //{
        //
        //}
        //open($rootScope.path + "/onlineview.jsp?resId=" + data.resourceId + "&j=t");
      }
      else if (data.type == 3) {
        open(data.linkPath);
      }
    };
    $rootScope.back = function (id) {
      if ($scope.isSearch) {
        $scope.isSearch = false;
        $rootScope.isBack = true;
        $scope.pagingOptions.currentPage = 1;
        $rootScope.floor = 0;
        $scope.getResourceAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
      }
      else if ($rootScope.floor == 0) {

      } else if ($rootScope.floor == 1) {
        $scope.getResourceAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
        $rootScope.floor--;
        $scope.pagingOptions.currentPage = 1;
      }
      else {
        // $rootScope.progressbar.start();
        sendRequest("/group/getResources.action",
          {
            parentId: id,
            back: true,
            type: "all",
            limit: $scope.pagingOptions.pageSize,
            start: $scope.pagingOptions.start
          }, function (data) {
            // $rootScope.progressbar.complete();
            $scope.totalServerItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
            $scope.data = data.resources;
            $scope.listOptions.parentId = data.parentId;
            $scope.pagingOptions.currentPage = 1;
            $rootScope.thumbnail = [];
            var imageId = [];
            for (var i = 0; i < data.resources.length; i++) {
              if (data.resources[i].type == 2) {
                var fileName = data.resources[i].displayName;
                var end = fileName.substring(fileName.lastIndexOf(".") + 1,
                  fileName.length).toLowerCase();
                if (end == ("jpg") || end == ("gif") || end == ("png")
                  || end == ("jpeg") || end == ("bmp"))
                  imageId.push(data.resources[i].id);
              }
            }
            if (imageId.length > 0) {
              sendRequest("/group/getThumbnail.action", {
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

myshareController.filter('shareType', function ($rootScope) {
  return function (shareType) {
    if (shareType == 0) {
      shareType = "系统分享";
    }
    else if (shareType == 1) {
      shareType = "文件外链"
    }
    return shareType;
  }
});
myshareController.filter('shareicon', function ($rootScope) {
  return function (icon) {
    if (icon.type == 1 || icon.type == "folder") {
      return "images/folder.png"
    } else if (icon.type == 2 || icon.type == "file") {
      var end = icon.displayName.substring(icon.displayName.lastIndexOf(".") + 1,
        icon.displayName.length).toLowerCase();
      if (end == "zip" || end == "rar" || end == ("jar") || end == ("7z") || end == ("gz") || end == ("iso")) {
        return "images/zip.png";
      } else if (end == ("jpg") || end == ("gif") || end == ("png") || end == ("jpeg") || end == ("bmp")) {
        if ($rootScope.thumbnail.length > 0) {
          for (var x in $rootScope.thumbnail) {
            if (icon.resourceId == $rootScope.thumbnail[x].id || icon.id == $rootScope.thumbnail[x].id) {
              return $rootScope.path + $rootScope.thumbnail[x].thumbUrl;
            }
          }
        }
        else return "images/image.png";
      } else if (end == ("m4a") || end == ("mp3") || end == ("mid") || end == ("xmf") || end == ("ogg") || end == ("wav")) {
        return "images/audio.png";
      } else if (end == "txt") {
        return "images/txt.png";
      } else if (end == ("pdf")) {
        return "images/pdf.png";
      } else if (end == ("doc") || end == ("docx")) {
        return "images/doc.png";
      } else if (end == ("xls") || end == ("xlsx")) {
        return "images/xls.png";
      } else if (end == ("ppt") || end == ("pptx")) {
        return "images/ppt.png";
      } else if (end == ("apk")) {
        return "images/apk.png";
      } else if (end == ("3gp") || end == ("mp4") || end == ("avi") || end == ("rmvb") || end == ("wmv") || end == ("mkv") || end == ("flv") || end == ("mpg")) {
        return "images/video.png"
      } else if (end == ("exe")) {
        return "images/exe.png";
      }
      else    return "images/default.png";
    }
    else if (icon.type == 3) {
      return "images/link.png";
    }
  }
});

/**主界面布局JS**/
var GridLayout = {
  init: function () {
    // var _this = this;
    // // _this.resize();
    // window.onresize = function () {
    //   // _this.resize();
    // }
  },
  resize: function () {

  }
}
