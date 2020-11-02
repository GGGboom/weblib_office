//var ngGridDraggableRow = require("../../lib/angularjs/ui-grid/ng-grid.plugin.draggablerow.js");
var PersonalController = angular.module("PersonalController", ["ngGrid", "am.modal", "ng-context-menu"]);
PersonalController.controller("PersonalController", ["$scope", "$http", "$rootScope", "$state", "httpRequest.sendRequest", "modalWindow", "modalConfirm",
  function ($scope, $http, $rootScope, $state, sendRequest, modalWindow, modalConfirm) {
    $scope.id = 0;
    $scope.isHidden = true;
    $rootScope.AddDir = true;
    $rootScope.Delete = true;
    $rootScope.Modify = true;
    $rootScope.Upload = true;
    $rootScope.isDrag = true;
    $rootScope.isBack = true;

    $scope.event = true;
    $rootScope.mySelections = [];
    $scope.totalServerItems = 0;
    $scope.isuploading = false;
    $scope.uploadShow = false;
    $scope.isSearch = false;
    $rootScope.istrash = false;
    document.querySelector("body").addEventListener("dragover", function (e) {
      if ($rootScope.isDrag) {
        $rootScope.upload();
      }
    });
    var modal;
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
        angular.element(document.querySelector(".ngViewport")).height(angular.element(window).height() - 120);
        angular.element(window).resize(function () {
          angular.element(document.querySelector(".ngViewport")).height(angular.element(window).height() - 125);
        });
      },
      rowTemplate: 'tpl/listTpls/rowTemplate.html',
      columnDefs: [
        {
          field: "displayName", displayName: '名称',
          cellTemplate: 'tpl/listTpls/cell.html'
        },
        {
          field: "desc", width: 200, displayName: '备注',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)" ng-click="selectRow(row,renderedRows)"><span ng-cell-text>{{row.getProperty(col.field)|displayName}}</span></div>'
        },
        {
          field: "size", width: 80, displayName: '文件大小',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)"   ng-click="selectRow(row,renderedRows)" style="cursor:default;text-align: right;padding-right: 10px;"><span ng-cell-text>{{row.getProperty(col.field)|size}}</span></div>'
        },
        {
          field: "memberName", width: 80, displayName: '创建者',
          cellTemplate: '<div class="ngCellText" ng-dblclick="title(row.entity)"  ng-click="selectRow(row,renderedRows)" style="cursor:default;" ><span ng-cell-text>{{row.getProperty(col.field) }}</span></div>'
        },
        {
          field: "creationDate", width: 160, displayName: '上传时间',
          cellTemplate: '<div class="ngCellText"  ng-dblclick="title(row.entity)" style="cursor:default;" ng-click="selectRow(row,renderedRows)"><span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
        }
      ],
      plugins: [new ngGridDraggableRow(modalConfirm, sendRequest, $rootScope)]
    };

    // start row drag
    $scope.$on("ngGrigDraggableRowEvent_rowDragStart", function (evt, args) {
      $rootScope.isDrag = false;
      // if drag multiple rows, set custom drag image.
      if (args.rowItemsMove.length > 1) {
        $scope.dragRowNum = args.rowItemsMove.length;
        $scope.$apply();
        if (args.dataTransfer.setDragImage) {
          var dragImage = document.getElementById('dragIconMultiRows');
          args.dataTransfer.setDragImage(dragImage, 16, 16);
        }
      }

    });

    // start reordering rows by drag
    $scope.$on('ngGrigDraggableRowEvent_changeRowOrderPre', function (evt, willSortInit) {
      if (willSortInit) {
      }
    });

    // end reordering rows by drag
    $scope.$on('ngGrigDraggableRowEvent_changeRowOrderPost', function (evt, doneSortInit) {
      $rootScope.isDrag = true;
      //  updateArrayIndex($scope.data);
      if (doneSortInit) {
      }
    });
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
    $scope.getPagedDataAsync = function (pageSize, page, id) {
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      if ($scope.$stateParams.id == 1 || $scope.$stateParams.id == 2 || $scope.$stateParams.id == 3 || $scope.$stateParams.id == 4) {
        sendRequest("/group/getClassifyResources.action",
          {
            groupId: $rootScope.status.personGroupId,
            classify: $scope.$stateParams.id,
            limit: pageSize,
            start: page
          }, function (data) {
            $scope.totalServerItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
            $scope.data = data.resources;
            $scope.breadcrumbpath = data.path;
            //$(".ngViewport ")[0].scrollTop = 0;
            $scope.listOptions.parentId = data.parentId;
          });
      }
      else {
        // $rootScope.progressbar.start();
        //获取资源列表
        sendRequest("/group/getResources.action",
          { parentId: id, type: "all", limit: pageSize, start: page }, function (data) {
            // $rootScope.progressbar.complete();
            $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
            $scope.data = data.resources;
            $scope.recyclerId = data.recyclerId;
            $scope.breadcrumbpath = data.path;
            if (data.path.length == 1) {
              $rootScope.istrash = false;
            }
            else if (data.path.length > 1) $rootScope.isBack = false;
            else $rootScope.isBack = true;
            $rootScope.thumbnail = [];

            //$(".ngViewport ")[0].scrollTop = 0;
            $scope.listOptions.parentId = data.parentId;
            $scope.imageId = [];
            for (var i = 0; i < data.resources.length; i++) {
              if (data.resources[i].type == 2) {
                var fileName = data.resources[i].displayName;
                var end = fileName.substring(fileName.lastIndexOf(".") + 1,
                  fileName.length).toLowerCase();
                if (end == ("jpg") || end == ("gif") || end == ("png")
                  || end == ("jpeg") || end == ("bmp"))
                  $scope.imageId.push(data.resources[i].id);
              }
            }
            if ($scope.imageId.length > 0) {
              sendRequest("/group/getThumbnail.action", {
                width: 25,
                height: 25,
                quality: 0,
                id: $scope.imageId
              }, function (data) {
                $rootScope.thumbnail = data;
              });
            }
          });
        sendRequest("/group/getResourceSize.action", {
          groupId: $rootScope.status.personGroupId
        }, function (data) {
          $scope.resourcesSize = data.resourcesSize;
          $scope.totalSize = data.totalSize;
          $scope.procent = (Math.round((data.resourcesSize / data.totalSize) * 10000) / 100).toFixed(1) + '%';
          $scope.progress = { "width": $scope.procent }
        });
      }
    };
    $scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
      if (newVal !== oldVal) {
        if ($scope.isSearch)
          $scope.searchResources($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.searchData.query, $scope.searchData.categoryId, $scope.searchData.owner, $scope.searchData.upCreationDate, $scope.searchData.downCreationDate)
        else   $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.listOptions.parentId);
      }
    });
    $scope.onRightClick = function () {
      $scope.event = false;
    };
    $scope.Close = function () {
      $scope.event = true;
    };
    //下载资源
    $scope.download = function (id) {
      location.href = $rootScope.path + "/group/downloadResource.action?id=" + id;
    };
    $rootScope.multiSelectDownload = function () {
      if ($scope.gridOptions.selectedItems.length > 0) {
        var selectedId = "";
        for (var x in $scope.gridOptions.selectedItems) {
          selectedId = selectedId + ($scope.gridOptions.selectedItems[x].id) + "&id="
        }
        selectedId = selectedId.substring(0, selectedId.length - 4);
        $scope.download(selectedId);
      }
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
        $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
        $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
        $scope.data = data.resources;
        $scope.breadcrumbpath = [{ id: -$rootScope.status.personGroupId, displayName: "搜索结果" }];
        $rootScope.thumbnail = [];
        $scope.imageId = [];
        for (var i = 0; i < data.resources.length; i++) {
          if (data.resources[i].type == "file") {
            var fileName = data.resources[i].displayName;
            var end = fileName.substring(fileName.lastIndexOf(".") + 1,
              fileName.length).toLowerCase();
            if (end == ("jpg") || end == ("gif") || end == ("png")
              || end == ("jpeg") || end == ("bmp"))
              $scope.imageId.push(data.resources[i].id);
          }
        }
        if ($scope.imageId.length > 0) {
          sendRequest("/group/getThumbnail.action", {
            width: 25,
            height: 25,
            quality: 0,
            id: $scope.imageId
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
    $scope.share = function (id) {
      $scope.event = true;
      modalWindow.open({
        templateUrl: 'tpl/share.html',
        controller: 'shareController',
        title: '文件共享',
        width: 900,
        height: 600,
        resolve: {
          items: function () {
            return { "resourceId": id, "groupId": $rootScope.status.personGroupId };
          }
        }
      });

    };
    $scope.createLink = function () {
      modalWindow.open({
        templateUrl: 'tpl/createLink.html',
        controller: 'createLinkController',
        title: '新建链接',
        width: 400,
        height: 157,
        resolve: {
          items: function () {
            return {
              "groupId": $rootScope.status.personGroupId,
              "parentId": $scope.listOptions.parentId,
              "scope": $scope
            };
          }
        }
      });
    };
    $scope.rename = function (x) {
      $scope.event = true;
      if (x.type == 3) {
        modalWindow.open({
          templateUrl: 'tpl/rename.html',
          controller: 'renameController',
          title: '编辑',
          width: 400,
          height: 225,
          resolve: {
            items: function () {
              return {
                "id": x.id,
                "name": x.displayName,
                "desc": x.desc,
                "priority": x.priority,
                "url": x.linkPath,
                "scope": $scope
              };
            }
          }
        });
      }
      else {
        modalWindow.open({
          templateUrl: 'tpl/rename.html',
          controller: 'renameController',
          title: '编辑',
          width: 400,
          height: 193,
          resolve: {
            items: function () {
              return {
                "id": x.id,
                "name": x.displayName,
                "desc": x.desc,
                "priority": x.priority,
                "scope": $scope
              };
            }
          }
        });
      }
    };
    $scope.slide = function (id) {
      $scope.event = true;
      modalWindow.open({
        templateUrl: 'tpl/slide.html',
        controller: 'slideController',
        title: '图片预览',
        width: 800,
        height: 540,
        resolve: {
          items: function () {
            return { "id": id, "imageId": $scope.imageId }
          }
        }

      })
    };
    $scope.Email = function (x) {
      if ($scope.gridOptions.selectedItems.length == 0) $scope.event = true;
      modalWindow.open({
        templateUrl: 'tpl/email.html',
        controller: 'emailController',
        title: '邮件快递',
        width: 800,
        height: 530,
        resolve: {
          items: function () {
            return $scope.gridOptions.selectedItems;
          }
        }

      })
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
    $scope.moveCopy = function (x, title, y) {
      var id = [];
      if (y == 1) {
        id.push(x);
      }
      else {
        for (var i in $scope.gridOptions.selectedItems) {
          id.push($scope.gridOptions.selectedItems[i].id);
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
              return { "resourceId": id, "title": title, "scope": $scope };
            }
          }
        });
      }
    };
    $scope.recycle = function (id) {
      // $rootScope.progressbar.start();
      sendRequest("/group/recycleResource.action",
        { id: id }, function (data) {
          if (data.type == "success") {
            // $rootScope.progressbar.complete();
            $scope.update();
            toastr["success"]("删除成功！");
          } else toastr["error"]("删除失败！");
        });
    };
    $scope.restoreResource = function (id) {
      // $rootScope.progressbar.start();
      sendRequest("/group/restoreResource.action",
        { id: id }, function (data) {
          if (data.type == "success") {
            // $rootScope.progressbar.complete();
            $scope.update();
            toastr["success"]("恢复成功！");
          } else toastr["error"]("恢复失败！");
        });
    };
    $scope.completeDelete = function (id) {
      // $rootScope.progressbar.start();
      sendRequest("/group/deleteResource.action",
        { id: id }, function (data) {
          if (data.type == "success") {
            // $rootScope.progressbar.complete();
            $scope.update();
            toastr["success"]("删除成功！");
          } else toastr["error"]("删除失败！");
        });
    };
    $scope.rowOver = function () {
      this.isHidden = false;
    };
    $scope.rowLeave = function () {
      this.isHidden = true;
    };
    $scope.prev = function () {
      if ($scope.pagingOptions.currentPage <= 1)
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
    $scope.trash = function () {
      if (!$rootScope.istrash) {
        $rootScope.istrash = true;
        $scope.pagingOptions.currentPage = 1;
        $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.recyclerId);
      }
    };
    $scope.title = function (data) {
      if (data.type == 1 || data.type == "folder") {
        $scope.pagingOptions.currentPage = 1;
        $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, data.id);
      }
      else if (data.type == 2 || data.type == "file") {
        var end = data.displayName.substring(data.displayName.lastIndexOf(".") + 1, data.displayName.length).toLowerCase();
        if (end == ("jpg") || end == ("gif") || end == ("png") || end == ("jpeg") || end == ("bmp")) {
          $scope.slide(data.id);
        }
        //if(suffix=="txt"||suffix=="doc"||suffix=="docx"||suffix=="pdf"||suffix=="ppt"||suffix=="pptx"||suffix=="xls"||suffix=="xlsx"){
        //    $.ajax({
        //        type: "GET",
        //        cache: false,
        //        url: "http://www.baidu.com",
        //        data: "",
        //        success: function() {
        //            alert(1);
        //        },
        //        error: function() {
        //            alert(0);
        //        }
        //    });
        //
        //}
        //        open ( $rootScope.path + "/onlineview.jsp?resId=" + data.id+"&j=t");
        else      $scope.download(data.id)
      }
      else if (data.type == 3) {
        open(data.linkPath);
      }
    };
    $rootScope.back = function (id) {
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      if ($scope.isSearch) {
        $scope.isSearch = false;
        $rootScope.isBack = true;
        $scope.pagingOptions.currentPage = 1;
        $scope.id = -$rootScope.status.personGroupId;
        $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.id);
      }
      else if (id == 0) {
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
            $scope.pagingOptions.totalCount = $scope.totalServerItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize);
            $scope.data = data.resources;
            $scope.breadcrumbpath = data.path;
            if (data.path.length == 1) {
              $rootScope.istrash = false;
            }
            else if (data.path.length > 1) $rootScope.isBack = false; else $rootScope.isBack = true;
            $scope.listOptions.parentId = data.parentId;
            $scope.pagingOptions.currentPage = 1;
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
    $rootScope.upload = function () {
      if (!$scope.uploadShow) {
        if ($scope.isuploading) {
          modal.show();
          $scope.uploadShow = true;
        }
        else {
          modal = modalWindow.open({
            template: '<iframe src="lib/html5/upload.html#parentId=' + $scope.listOptions.parentId + '&groupId=' + $rootScope.status.personGroupId + '&singleSize=2097152&leaveSpace=20953448" frameborder="0" scrolling="no" style="width:100%;height:450px;"></iframe>',
            title: '文件上传',
            width: 400,
            height: 500,
            onCloseClicked: function () {
              if ($scope.isuploading)
                modal.hide();
              else modal.close();
              $scope.uploadShow = false;
            }
          });
          $scope.uploadShow = true;
        }
      }
    };
    $rootScope.newFolder = function () {
      modalWindow.open({
        templateUrl: 'tpl/newFolder.html',
        controller: 'newFolderController',
        title: '新建文件夹',
        width: 350,
        height: 143,
        resolve: {
          items: function () {
            return {
              "groupId": $rootScope.status.personGroupId,
              "parentId": $scope.listOptions.parentId,
              "scope": $scope
            };
          }
        }
      });
    };
    $rootScope.multiSelectRestore = function () {
      if ($scope.gridOptions.selectedItems.length > 0) {
        modalConfirm.confirm({
          title: '是否恢复这' + $scope.gridOptions.selectedItems.length + '个资源?',
          onConfirm: function (data) {
            var selectedId = [];
            for (var x in $scope.gridOptions.selectedItems) {
              selectedId.push($scope.gridOptions.selectedItems[x].id)
            }
            $scope.restoreResource(selectedId);

          },
          onCancel: function (data) {
          }
        });
      }

    };
    $rootScope.multiSelectDelete = function () {
      if ($scope.gridOptions.selectedItems.length > 0)
        if ($rootScope.istrash) {
          modalConfirm.confirm({
            title: '彻底删除将无法恢复,是否彻底删除这' + $scope.gridOptions.selectedItems.length + '个资源?',
            onConfirm: function (data) {
              var selectedId = [];
              for (var x in $scope.gridOptions.selectedItems) {
                selectedId.push($scope.gridOptions.selectedItems[x].id)
              }
              $scope.completeDelete(selectedId);

            },
            onCancel: function (data) {
            }
          });
        } else {
          modalConfirm.confirm({
            title: '是否删除这' + $scope.gridOptions.selectedItems.length + '个资源?',
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
        }

    };
    $scope.deleteFile = function (x) {
      if ($rootScope.istrash) {
        modalConfirm.confirm({
          title: '彻底删除将无法恢复,是否彻底删除 ' + x.displayName.replace(/(&nbsp;)/g, " ") + ' 这个资源 ?',
          onConfirm: function (data) {
            $scope.completeDelete(x.id);
          },
          onCancel: function (data) {
          }
        });
      } else {
        modalConfirm.confirm({
          title: '是否删除 ' + x.displayName.replace(/(&nbsp;)/g, " ") + ' 这个资源 ?',
          onConfirm: function (data) {
            $scope.recycle(x.id);
          },
          onCancel: function (data) {
          }
        });
      }

    };
    $scope.update = function () {
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.listOptions.parentId);
    };
    $rootScope.homepage = function () {
      $scope.pagingOptions.currentPage = 1;
      $scope.gridOptions.selectedItems.splice(0, $scope.gridOptions.selectedItems.length);
      $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.id);
    };
    $rootScope.updateList = function () {
      $scope.update();
    };
    $scope.isupload = function () {
      $scope.isuploading = true
    };
    $scope.noupload = function () {
      $scope.isuploading = false;
    };
    GridLayout.init();
    /**获取登录状态及个人信息**/
    sendRequest("/user/status.action", {}, function (data) {
      if (data.status == "login") {
        /**需要个人ID的所有操作，在这里执行**/
        $rootScope.status = data;
        if ($scope.$stateParams.groupId > 0) {
          $rootScope.status.personGroupId = $scope.$stateParams.groupId;
          sendRequest("/group/getGroupPermission.action", { groupId: $scope.$stateParams.groupId }, function (data) {
            $rootScope.AddDir = data.addDir;
            $rootScope.Delete = data.delete;
            $rootScope.Modify = data.modify;
            $rootScope.Upload = data.upload
          });
        }
        $scope.id = -$rootScope.status.personGroupId;
        $scope.getPagedDataAsync($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize, $scope.id);
      } else {
        // window.location.href="http://weblib.ccnl.scut.edu.cn/pages/c/login.jsp";//如果没登陆则跳转到登录页
      }
    });
  }]);

function uploadComplete() {
  angular.element(document.getElementsByClassName('gridStyle')).scope().update();
  angular.element(document.getElementsByClassName('gridStyle')).scope().noupload();
}
function uploading() {
  angular.element(document.getElementsByClassName('gridStyle')).scope().isupload();
}
PersonalController.filter('displayName', function () {
  return function (displayName) {
    displayName = displayName.replace(/(&nbsp;)/g, " ");
    return displayName;
  }
});

PersonalController.filter('owner', function ($rootScope) {
  return function (owner) {
    if (owner) {
      owner = $rootScope.status.memberName;
    }
    return owner;
  }
});
PersonalController.filter('path', function ($rootScope) {
  return function (path) {
    if (path == $rootScope.status.memberName) {
      path = "个人资源库";
    }
    path = path.replace(/(&nbsp;)/g, " ");
    return path;
  }
});
PersonalController.filter('size', function () {
  return function (size) {
    if (size == 0) {
      return size;
    }
    else if (size < 1024) {
      return size + "KB";
    }
    else if (size < 1048576) {
      return (size / 1024).toFixed(2) + "MB";
    }
    else if (size < 1073741824) {
      return (size / 1048576).toFixed(2) + "GB";
    }
  }
});
PersonalController.filter('icon', function ($rootScope) {
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
            if (icon.id == $rootScope.thumbnail[x].id) {
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
    else return "images/default.png";
  }
});
/**主界面布局JS**/
var GridLayout = {
  init: function () {
    var _this = this;
    _this.resize();
    window.onresize = function () {
      _this.resize();
    }
  },
  resize: function () {
    //var _height = document.body.clientHeight, _headerH = document.getElementById("header").offsetHeight;
    //$(".gridStyle").height(_height - _headerH - 40);
    //document.getElementById("left").style.height = (_height - _headerH) + "px";
    //document.getElementById("right").style.height = (_height - _headerH) + "px";
  }
}
