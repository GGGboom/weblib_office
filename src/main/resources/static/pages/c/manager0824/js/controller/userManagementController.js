angular.module('userManagementController',
    ['ng-context-menu', 'ui.grid.selection', 'ui.grid.pagination', 'ngFileUpload','ui.grid.autoResize','ngDraggable'])
    .controller('userManagementController',
                  ['$scope',
                      '$rootScope',
                      'httpRequest.sendRequest',
                      '$state',
                      'uiGridConstants',
                      '$templateCache',
                      'uiGridSelectionService',
                      'GridApi',
                      'Upload','modalConfirm',
                      function ($scope, $rootScope, sendRequest, $state, uiGridConstants, $templateCache,uiGridSelectionService, GridApi, Upload,modalConfirm) {
        //init panel hight
        $scope.windowHeight = angular.element(window).height();

        angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);

        angular.element(window).resize(function () {
            $scope.windowHeight = angular.element(window).height();
            angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
            
        });

        //初始化用户目录树
        $scope.tree = [];
        $scope.selectedNode = {};//选中的用户树节点
        $scope.currentTeamId = -1;//初始选中所有用户
        var initTree = function(){
            sendRequest('/grouper/grouperTree.action',
                {
                    id: 0,
                    type: 'folder',
                    folderOnly: false,
                    withoutLeaf:true
                }, function (data) {
                    //build the tree
                    data.members.push({
                        id: -1,
                        text: "所有用户",
                        type: "allUser"
                    });
                    $scope.tree.push( {text: "根目录", type:"rootFolder",nodes: data.members});

                });
            $scope.currentGroup = '所有用户';
            // console.log($scope.selectedNode);//测试初始状态selectedNode对象是否为空
        };
       initTree();
        //打开用户目录树
        $scope.openTree = function (scope) {

            var _thisNode = scope.$modelValue;
            $scope.selectedNode = _thisNode;
            //如果该节点不是组节点，且子节点数据已经在scope中，则直接打开
            if (typeof (_thisNode.nodes) != 'undefined' && _thisNode.type != 'team' && _thisNode.type != 'allUser') {
                scope.toggle();
                return false;
            }
            //如果该节点不是组节点，但数据不在scope中，则重新请求数据
            if (_thisNode.type != 'team' && _thisNode.type != 'allUser' && typeof (_thisNode.nodes) == 'undefined') {
                sendRequest('/grouper/grouperTree.action',
                    {
                        id: _thisNode.id,
                        type: 'folder',
                        folderOnly: false,
                        withoutLeaf:true
                    }, function (data) {
                        _thisNode.nodes = data.members;
                        scope.toggle();
                    });
                return false;
            }
        };
        //判断右键菜单按钮是否可用
        $scope.isMenuAvailable = function (node, index) {

        };

        //设置选中行的样式
        $scope.isSelected = function (scope) {
          if ($scope.selectedNode == scope.$modelValue) {
              return {'background-color': '#3bb4f2'};
          }
          return {'background-color': '#ffffff'};
        };
        //设置选中行的操作
        $scope.selectNode = function (scope) {
            var _thisNode = scope.$modelValue;
            $scope.selectedNode = _thisNode;
            //如果节点是组节点，则打开右边表格
            if (_thisNode.type == 'team' || _thisNode.type == 'allUser') {
                console.log("open gridView");
                $scope.currentGroup = _thisNode.text;
                if($scope.currentTeamId==_thisNode.id){
                    return;
                }
                $scope.pagingOptions.currentPage = 1;
                $scope.currentTeamId = _thisNode.id;
                $scope.advanceSearchParam ={};//防止高级搜索参数影响结果
                $scope.refreshGridData();
            }
            rowOrNode = false;
        };

        //右边用户列表视图初始化userGrid init
        $templateCache.put('ui-grid/selectionRowHeaderButtons',
            "<div class=\"ui-grid-selection-row-header-buttons\" " +
                 "ng-class=\"{'ui-grid-row-selected':row.isSelected}\">" +
            "<input type=\"checkbox\" " +
                   "ng-model=\"row.isSelected\" " +
                   "ng-click=\"selectButtonClick(row,$event)\"" + //row.isSelected=!row.isSelected;
            "&nbsp;</div>");
        $templateCache.put('ui-grid/selectionSelectAllButtons',
            "<div class=\"ui-grid-selection-row-header-buttons\" " +
                 "ng-class=\"{'ui-grid-all-selected': grid.selection.selectAll}\" " +
                 "ng-show=\"grid.options.enableSelectAll\">" +
            "<input type=\"checkbox\"" +
                   "ng-model=\"grid.selection.selectAll\" " +
                   "ng-click=\"headerButtonClick($event)\"" +//grid.selection.selectAll=!grid.selection.selectAll;
            "</div>"
        );
        $scope.pagingOptions = {
            // pageSize: 15,
            pageSize:24,
            currentPage: 1,
            start: 0,
            totalPage: 0
        };
        $scope.userGridOptions = {

            data: 'data',
            multiSelect: true,/*控制点中选中框时是否可以多选，而不是对行的设置*/
            enableRowSelection:false,
            enableSelectAll: true,/*控制全选按钮是否可用*/
            enableRowHeaderSelection: true,
            // enableFullRowSelection:false,
            rowHeight: 35,
            headerRowHeight: 35,
            enableSorting: true,
            enableColumnMenus: false,
            enableFiltering:true,
            enablePaging: true,
            showFooter: false,
            totalServerItems: 'totalServerItems',
            minRowsToShow: $scope.pagingOptions.pageSize,
            maxRowsToShow: $scope.pagingOptions.pageSize,
            pagingOptions: $scope.pagingOptions,
            enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
            enableVerticalScrollbar: uiGridConstants.scrollbars.NEVER,
            selectionRowHeaderWidth:35,


            rowTemplate: 'tpl/userGridTpls/rowTemplate.html',
            columnDefs:[
                {
                    field: "account", width: '25%', displayName: '账号',
                    headerCellClass: 'gridHead'
                },
                {
                    field: "name", width: '12%', displayName: '姓名',
                    headerCellClass: 'gridHead'
                },
                {
                    field: "department", width: '25%', displayName: '部门',
                    headerCellClass: 'gridHead'
                },
                {
                    field: "position", width: '*', displayName: '岗位',
                    headerCellClass: 'gridHead'
                },
                {
                    field: "status", width: '12%', displayName: '状态',
                    headerCellClass: 'gridHead',
                    cellTemplate: 'tpl/userGridTpls/statusCellTemplate.html'

                }
            ],
            onRegisterApi: function (gridApi) {
                $scope.gridApi = gridApi;
            }
            // plugins: [new ngGridDraggableRow(modalConfirm, sendRequest, $rootScope)]

        };
        $scope.advanceSearchParam = {};//高级搜索按钮
        //发生操作时刷新左边视图目录树
        $scope.refreshTreeData = function (scope) {
            $rootScope.progressbar.start();
            sendRequest('/grouper/grouperTree.action',{
                id:$scope.selectedNode.id,
                type:$scope.selectedNode.type,
                folderOnly:false,
                withoutLeaf:true
            },function (data) {
                $rootScope.progressbar.complete();
                //兼容返回json格式不一致问题
                angular.forEach(data.members, function (value, key) {
                    value.name = value.fullName;
                    value.account = value.text;
                });
                //选中根目录时，需要把“所有用户”这个节点添加到data.members中
                if($scope.selectedNode.type == 'rootFolder'){
                    data.members.push({
                        id: -1,
                        text: "所有用户",
                        type: "allUser"
                    });
                }
                $scope.selectedNode.nodes = data.members;//重新打开目录树？？

            },function (data,code) {
                $rootScope.toastr.error(data.detail || '未知错误');
            })

        }
        //发生操作时刷新右边视图用户列表
        $scope.refreshGridData = function (start) {
            $rootScope.progressbar.start();
            if ($scope.currentTeamId == -1) {
                var param = {
                    start: start || 0,
                    limit: $scope.pagingOptions.pageSize
                }
                param = angular.extend(param,$scope.advanceSearchParam);
                //未选中指定的group
                sendRequest('/user/advancedSearch.action',
                    param
                    ,
                    function (data) {
                        $rootScope.progressbar.complete();
                        $scope.data = data.accounts;
                        $scope.pagingOptions.totalPage = Math.ceil(data.totalCount / $scope.pagingOptions.pageSize);
                        $scope.userGridOptions.maxRowsToShow = Math.min($scope.pagingOptions.pageSize, data.totalCount);
                        $scope.userGridOptions.minRowsToShow = Math.min($scope.pagingOptions.pageSize, data.totalCount);
                        $scope.gridHeight = 35 * ($scope.userGridOptions.minRowsToShow + 1);

                    });
            } else {
                sendRequest('/grouper/grouperTree.action', {
                        id: $scope.currentTeamId,
                        type: 'team',
                        folderOnly: false,
                        withoutLeaf:true
                    },
                    function (data) {
                        $rootScope.progressbar.complete();
                        //兼容返回json格式不一致问题
                        angular.forEach(data.members, function (value, key) {
                            value.name = value.fullName;
                            value.account = value.text;
                        });

                        $scope.pagingOptions.totalPage = Math.ceil(data.members.length / $scope.pagingOptions.pageSize);
                        $scope.gridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL);
                        $scope.userGridOptions.maxRowsToShow = Math.min($scope.pagingOptions.pageSize, data.members.length);
                        $scope.userGridOptions.minRowsToShow = Math.min($scope.pagingOptions.pageSize, data.members.length);
                        $scope.userGridOptions.data = data.members;
                        $scope.gridHeight = 35 * ($scope.userGridOptions.minRowsToShow + 1);
                        $scope.gridApi.grid.modifyRows($scope.userGridOptions.data);
                    });
            }

        };
        //初始化表格，默认显示所有用户
        $rootScope.progressbar.start();
        sendRequest('/user/advancedSearch.action',
            {
                start: 0,
                limit: $scope.pagingOptions.pageSize
            },
            function (data) {
                $rootScope.progressbar.complete();
                $scope.data = data.accounts;
                $scope.pagingOptions.totalPage = Math.ceil(data.totalCount / $scope.pagingOptions.pageSize);
            }, function (data, code) {
                $rootScope.progressbar.complete();
                $rootScope.toastr.error(data.detail || "未知错误");
            });

        //用于获取当前视图
        $scope.currentModal = {};
        $scope.showCopyMove = false;
        var rowOrNode = false;
        $scope.dragRowComplete = function (row) {
            rowOrNode = true;
            // $scope.rowData = row.entity;
        };
        var destNode;
        $scope.dropInTree = function (scope,$event) {
          destNode = scope.$modelValue;
          $scope.currentGroup = destNode.text;
          $scope.showCopyMove = true;
          e = $event;
          if(destNode.type == 'folder' && !rowOrNode){
              // $('#moveOrAddToTeam').css({'top':(e.y) +'px','left':(e.x) +'px'});
              $scope.currentModal=$('#moveOrCopyTo').modal();
              $scope.clonedData={};
          }
          if(destNode.type == 'team' && rowOrNode){
              var selectedRows = detectSelectedRow();
              if (typeof(selectedRows) == 'undefined' || selectedRows.length == 0)
                  return;
              var param = [];
              angular.forEach(selectedRows, function (value, key) {
                  if(value.id){
                      param.push(value.id);
                  }else{
                      param.push(value.members[0].id);
                  }

              });
              $scope.currentModal = $('#addUserToTeamConfirm').modal({
                  onCancel: function () {
                      $scope.currentModal.modal('close');
                      $scope.count = 0;
                      destNode={};
                  }
              });
              $scope.addUserToTeamSubmit = function () {
                  $rootScope.progressbar.start();
                  sendRequest('/grouper/addMemberToTeam.action', {
                      memberId: param,
                      id: destNode.id
                  }, function (data) {
                      $rootScope.progressbar.complete();
                      $scope.currentTeamId = destNode.id;
                      $scope.currentModal.modal('close');
                      $scope.count = 0;
                      $scope.selectedNode = destNode;
                      destNode={};
                      $scope.refreshGridData();
                  }, function (data, code) {
                      $rootScope.progressbar.complete();
                      $scope.currentModal.modal('close');
                      $scope.count = 0;
                      destNode={};
                      $rootScope.toastr.error(data.detail || "未知错误");
                  });
              };
          }

        };
        $scope.moveToTeamOrFolder = function () {
          $scope.showCopyMove = false;
          $scope.currentModal.modal('close');
          $scope.currentModal = $('#moveToConfirm').modal({
              onCancel: function () {
                  $scope.currentModal.modal('close');
                  destNode={};
              }
          });
          $scope.moveToSubmit = function () {
              $rootScope.progressbar.start();
              if(destNode && $scope.selectedNode.type=='team'){
                  sendRequest('/grouper/moveTeam.action',{
                      id:$scope.selectedNode.id,
                      destId:destNode.id
                  },function (data) {
                      $rootScope.progressbar.complete();
                      $scope.selectedNode = $scope.tree[0];
                      $scope.refreshTreeData();
                      // $scope.tree[0].nodes.splice($scope.tree[0].nodes.indexOf($scope.selectedNode),1);
                      $rootScope.toastr.success('移动用户组成功!');
                      destNode = {};
                  },function (data,code) {
                      $rootScope.progressbar.complete();
                      $rootScope.toastr.error(data.detail || "未知错误");
                      destNode = {};
                  })
              }
              if(destNode && $scope.selectedNode.type=='folder'){
                  sendRequest('/grouper/moveFolder.action',{
                      id:$scope.selectedNode.id,
                      destId:destNode.id
                  },function (data) {
                      $rootScope.progressbar.complete();
                      $scope.selectedNode = $scope.tree[0];
                      $rootScope.toastr.success('移动用户组织成功!');
                      $scope.refreshTreeData();
                      destNode = {};
                  },function (data,code) {
                      $rootScope.progressbar.complete();
                      $rootScope.toastr.error(data.detail || "未知错误");
                  })
              }
              $scope.currentModal.modal('close');

          };



        };
        $scope.copyToTeamOrFolder = function () {
          $scope.showCopyMove = false;
          $scope.currentModal.modal('close');
          $scope.currentModal = $('#copyToConfirm').modal({
              onCancel: function () {
                  $scope.currentModal.modal('close');
                  destNode={};
              }
          });
          $scope.copyToSubmit = function () {
              $rootScope.progressbar.start();
              if(destNode && $scope.selectedNode.type=='team'){
                  sendRequest('/grouper/copyTeam.action',{
                      id:$scope.selectedNode.id,
                      destId:destNode.id
                  },function (data) {
                      $rootScope.progressbar.complete();
                      $scope.selectedNode = destNode;
                      $rootScope.toastr.success('复制用户组成功!');
                      $scope.refreshTreeData();
                  },function (data,code) {
                      $rootScope.progressbar.complete();
                      $rootScope.toastr.error(data.detail || "未知错误");
                  })
              }
              if(destNode && $scope.selectedNode.type=='folder'){
                  sendRequest('/grouper/copyFolder.action',{
                      id:$scope.selectedNode.id,
                      destId:destNode.id
                  },function (data) {
                      $rootScope.progressbar.complete();
                      $scope.selectedNode = destNode;
                      $rootScope.toastr.success('复制用户组织成功!');
                      $scope.refreshTreeData();
                      destNode = {};
                  },function (data,code) {
                      $rootScope.progressbar.complete();
                      $rootScope.toastr.error(data.detail || "未知错误");
                      destNode = {};
                  })
              }
              $scope.currentModal.modal('close');
          }

        };
        //判断是否存在同名的组织或组-代码整合
        $scope.wrongName = false;
        var ifWrongName = function (name,type) {//传入名称和新建的类型
            var _thisNode = $scope.selectedNode;
            if(name!=null){
                angular.forEach(_thisNode.nodes,function(data){
                    if (data.text == name && data.type == type) {
                        if(type == 'folder'){
                            $rootScope.toastr.error(name+"这个组织已存在！");
                        }
                        if(type == 'team'){
                            $rootScope.toastr.error(name+"这个组已存在！");
                        }
                        $scope.wrongName = true;//名称重复，做提示
                    }
                });
            }else{
                $scope.wrongName = true;//名称为空时，做提示
            }
        }
        //新建组织按钮
        $scope.newFolder = {};
        $scope.newTeam = {};
        $scope.addFolder = function () {
            $scope.currentModal = $("#addFolderModal").modal({
                onCancel:function () {
                    $scope.currentModal.modal('close');
                    $scope.newFolder = {};
                    $scope.newTeam = {};
                    $scope.wrongName = false;
                }
            });
        };
        $scope.ifSameNameTeam = true;
        $scope.addFolderSubmit = function () {
            $scope.wrongName = false;
            // $scope.ifSameNameTeam = false;
            var name = $scope.newFolder.name;
            $scope.newFolder.id = $scope.selectedNode.id;
            ifWrongName(name,'folder');
            if(!$scope.wrongName){
                //新建组织
                sendRequest('/grouper/createFolder.action',$scope.newFolder,function (data) {
                    $scope.currentModal.modal('close');
                    // _thisNode.nodes=data.members;
                    //同时新建同名用户组
                    var nodes = data.members;
                    // console.log(nodes);
                    if($scope.ifSameNameTeam){
                        $scope.newTeam.name = name;
                        $scope.newTeam.id = nodes[0].id;
                        // console.log($scope.newTeam);
                        sendRequest('/grouper/createTeam.action',$scope.newTeam,function (data) {
                            $rootScope.toastr.success('新建组织成功!');
                            $scope.refreshTreeData();
                        },function (data,code) {
                            $rootScope.toastr.error(data.detail || '未知错误');
                        })

                    }else{
                        $scope.refreshTreeData();//不建同名用户组，刷新页面
                    }
                },function (data, code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                });
                //清空数据
                $scope.newFolder = {};
                $scope.newTeam = {};
            }
        };
        //新建组按钮
        $scope.addTeam = function () {
            $scope.newTeam = {};
            $scope.currentModal = $('#addTeamModal').modal({
                onCancel:function () {
                    $scope.currentModal.modal('close');
                    $scope.newTeam = {};
                    // $scope.wrongTeamName = false;
                    $scope.wrongName = false;
                }
            }
            )
        };
        $scope.addTeamSubmit = function () {
            /*$scope.wrongTeamName = false;
            // console.log($scope.newFolder);
            var name = $scope.newTeam.name;
            $scope.newTeam.id = $scope.selectedNode.id;
            //输入名称不为空时，新建
            if(name != null){
                var _thisNode = $scope.selectedNode;
                //检查是否存在同名组
                angular.forEach(_thisNode.nodes,function (data) {
                    if( data.text == name && data.type == 'team'){
                        $rootScope.toastr.error(name + '这个组已存在');
                        $scope.wrongTeamName = true;
                    }
                });
                if(!$scope.wrongTeamName){
                    sendRequest('/grouper/createTeam.action',$scope.newTeam,function (data) {
                        $scope.currentModal.modal('close');
                        $scope.refreshTreeData();
                    },function (data,code) {
                                $rootScope.toastr.error(data.detail || '未知错误');
                            });
                    //清空数据
                    $scope.newTeam = {};
                }
            }else {
                // $rootScope.toastr.error("名称不能为空！");
                $scope.wrongTeamName = true;
            }*/
            $scope.wrongName = false;
            var name = $scope.newTeam.name;
            $scope.newTeam.id = $scope.selectedNode.id;
            ifWrongName(name,'team');
            if(!$scope.wrongName){
                sendRequest('/grouper/createTeam.action',$scope.newTeam,function () {
                    $scope.currentModal.modal('close');
                    $rootScope.toastr.success('新建组成功!');
                    $scope.refreshTreeData();
                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                });
                //清空数据
                $scope.newTeam = {};
            }
        };
        //编辑组织或组按钮
        $scope.editName = {};
        $scope.editFolderOrTeam = function () {
            $scope.editName.name = $scope.selectedNode.text;
            $scope.currentModal = $('#editFolderOrTeamModal').modal({
                    onCancel:function () {
                        $scope.currentModal.modal('close');
                        $scope.wrongName = false;
                        $scope.editName = {};
                    }
                }
            );
        };
        $scope.editFolderOrTeamSubmit = function () {
            $scope.wrongName = false;
            $scope.editName.id = $scope.selectedNode.id;
            var type = $scope.selectedNode.type;//判断是编辑组织名称还是编辑组名称
           /* */
            // if($scope.editName.text!=null){//此处和下面的写法有什么区别？？为什么这种写法就会先运行一次if里面的内容
            if($scope.editName.name){
                if($scope.editName.name == $scope.selectedNode.text){//当名称没有修改但点击了保存时
                    $scope.currentModal.modal('close');
                }
                if($scope.editName.id == 1445 && $scope.editName.name != $scope.selectedNode.text){//当尝试修改系统这个默认目录时
                    $rootScope.toastr.error("系统默认（组织/组），不允许进行此操作！");
                    $scope.wrongName = true;
                    $scope.editName.name = $scope.selectedNode.text;
                }
                //修改组织名称
                if(type == 'folder' && $scope.editName.name != $scope.selectedNode.text){
                    /*接口说明：/grouper/editFolder.action
                     * 作用：修改组织名称
                     * 参数列表：
                     * 　　　　　id——folder节点的id
                     * 　　　　　name——folder节点的text
                     * */
                    sendRequest('/grouper/editFolder.action',$scope.editName,function () {
                        // $scope.editName.text = $scope.selectedNode.text;
                        $scope.currentModal.modal('close');
                        $rootScope.toastr.success('编辑成功!');
                        $scope.selectedNode.text = $scope.editName.name;
                        $scope.editName = {};
                    },function (data,code) {
                        if(data.code == "403" || data.code =="300"){
                            // if(data.code =="300"){
                            $rootScope.toastr.error("组织名称已存在！");
                            $scope.wrongName = true;
                        }else{
                            $rootScope.toastr.error(data.detail || '未知错误');
                        }

                    });
                }
                //修改组名称
                if(type == 'team' && $scope.editName.name != $scope.selectedNode.text){
                    /*接口：/grouper/editTeam.action
                     * 作用：修改组织名称
                     * 参数列表：
                     * 　　　　　id——Team节点的id
                     * 　　　　　name——Team节点的text
                     * */
                    sendRequest('/grouper/editTeam.action',$scope.editName,function () {
                        $scope.currentModal.modal('close');
                        $scope.selectedNode.text = $scope.editName.name;//更改目录树节点名称
                        $scope.editName = {};
                    },function (data,code) {
                        /*报错说明：
                        * 403:？？
                        * 300：修改的子目录名称重复报错*/
                        if(data.code == "403" || data.code =="300"){
                            $rootScope.toastr.error("组名称已存在！");
                            $scope.wrongName = true;
                        }else{
                            $rootScope.toastr.error(data.detail || '未知错误');
                        }
                    });
                }
            }else {
                $rootScope.toastr.error("名称不能为空！");
                $scope.wrongName = true;
                $scope.editName = {};
            }

        };
        //删除组织或组按钮
        var ifDelete = function (nodes) {
          angular.forEach(nodes,function (data) {
              if(data.id == $scope.selectedNode.id){
                  nodes.splice(nodes.indexOf(data),1);
              }else{
                  ifDelete(data.nodes);
              }
          });
        };
        $scope.deleteFolderOrTeam = function () {
            $scope.currentModal = $('#deleteFolderOrTeamModal').modal({
                onConfirm:function () {
                    // $scope.editName.id = $scope.selectedNode.id;
                    var type = $scope.selectedNode.type;//判断是编辑组织名称还是编辑组名称
                    if(type == 'folder'){
                        sendRequest('/grouper/deleteFolder.action',{
                            id:$scope.selectedNode.id
                        },function (data) {
                            // $scope.tree[0].nodes.splice($scope.tree[0].nodes.indexOf($scope.selectedNode),1);
                           /* angular.forEach($scope.tree[0].nodes,function (data) {
                            /*angular.forEach($scope.tree[0].nodes,function (data) {
                               if(data.id == $scope.selectedNode.id){
                                   $scope.tree[0].nodes.splice($scope.tree[0].nodes.indexOf($scope.selectedNode),1);
                               }
                                   // return angular.forEach()
                            });*/
                            ifDelete($scope.tree[0].nodes);
                            $rootScope.toastr.success('删除成功!');

                        },function (data,code) {
                            $rootScope.toastr.error(data.detail || "未知错误");
                        });
                    }
                    if(type == 'team'){
                        sendRequest('/grouper/deleteTeam.action',{
                            id:$scope.selectedNode.id
                        },function (data) {
                            ifDelete($scope.tree[0].nodes);

                        },function (data,code) {
                            $rootScope.toastr.error(data.detail || "未知错误");
                        })
                    }

                },
                onCancel:function () {
                    $scope.currentModal.modal('close');
                }
                }
            );
        };



        $scope.nextPage = function () {
            if ($scope.pagingOptions.currentPage == $scope.pagingOptions.totalPage) {
                return;
            }
            else {
                $scope.refreshGridData($scope.pagingOptions.currentPage * $scope.pagingOptions.pageSize);
                $scope.pagingOptions.currentPage++;
            }
        };
        $scope.prevPage = function () {
            if ($scope.pagingOptions.currentPage == 1) {
                return;
            }
            else {
                $scope.pagingOptions.currentPage--;
                $scope.refreshGridData(($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
            }
        };
        /*鼠标移入按钮做提示*/
        var tipUser = [
            {id:"addFolder",content:"新建组织"},
            {id:"addTeam",content:"新建组"},
            {id:"editFolderOrTeam",content:"编辑"},
            {id:"deleteFolderOrTeam",content:"删除"},
            {id:"searchGroup",content:"搜索"},
            {id:"addUser",content:"新增用户"},
            {id:"delUser",content:"删除用户"},
            {id:"modUser",content:"编辑用户"},
            {id:"searchUser",content:"搜索用户"},
            {id:"resetUserPw",content:"重置密码"}
        ];
        $scope.tipShow = false;
        $scope.tipContent = '';
        $scope.tipsOn = function (id,$event) {
            // var offset = $(id).offset();
            $scope.tipShow = true;
            if($scope.tipShow){
                // $('#tipShow').css({'top':offset.top +'px','left':offset.left +'px'});
                $('#tipShow').css({'top':($event.pageY+6) +'px','left':($event.pageX+6) +'px'});
            }
            angular.forEach(tipUser,function (data) {
                if(data.id == id){
                    $scope.tipContent = data.content;
                }
            });


        };
        $scope.tipsOff = function () {
            $scope.tipShow = false;
            $scope.tipContent = '';
        };
      //查看详细信息
        $scope.ifEdit = false;
        $scope.showUserDetail = function (row) {
            $scope.ifEdit = true;
            $scope.ifAdd = false;
            $scope.ifModify = false;
          $state.go('manager.userManagement.userDetailInfo');

          $scope.modifyingUser = row.entity;
          var selectedRows = $scope.gridApi.selection.getSelectedGridRows();
          // console.log(selectedRows);
          for (var i in selectedRows) {
              selectedRows[i].setSelected(false);
          };
            row.setSelected(true);

            /*//初始化panel高度
               $scope.rightOffCanvasPanelStyle = {
               height: $scope.windowHeight - 170 + 'px'
               }
               $("#userDetail").offCanvas('open');*/
      };
        //新增用户
        $scope.newUser = {};
        $scope.ifAdd = false;
        $scope.role={
            id:"1",
            name:"systemAdmin",
            desc:"系统管理员"
        }
        $scope.addRole = function () {
            $rootScope.progressbar.start();
            var selectedRows = detectSelectedRow();
            if (typeof(selectedRows) == 'undefined' || selectedRows.length == 0){
                return;
            }
            /*sendRequest('/user/getAllRoles.action', function (data) {
                $rootScope.progressbar.complete();
                $scope.roles = data;
                console.log($scope.roles);
            }, function (data, code) {
                $rootScope.toastr.error(data.detail || "未知错误");
            });*/
            $scope.currentModal = $('#addRoleModal').modal();
        }
        $scope.addUser = function () {
            /*$scope.currentModal = $('#addUserModal').modal({
                onCancel: function () {
                    $scope.currentModal.modal('close');
                    $scope.newUser = {};
                }
            });*/
            $scope.ifEdit = false;
            $scope.ifAdd = true;
            $state.go('manager.userManagement.addUser');
        };
        $scope.addUserSubmit = function () {
            $scope.newUser.teamId = $scope.currentTeamId != 0 ? $scope.currentTeamId : '';
            if($scope.newUser.account && $scope.newUser.password && $scope.newUser.confirmPassword && $scope.newUser.name){
                sendRequest('/user/register.action', $scope.newUser, function (data) {
                    $scope.ifAdd = false;
                    $rootScope.toastr.success('新增用户成功!');
                    $scope.refreshGridData();
                    $scope.newUser = {};
                    $state.go('manager.userManagement');

                }, function (data, code) {
                    // alert(data.detail);
                    $rootScope.toastr.error(data.detail || '未知错误');
                });
            }else{
                alert("必填项不能为空！");
            }

        };
        $scope.addUserCancel = function () {
            $scope.ifAdd = false;
            $scope.newUser = {};
            $state.go('manager.userManagement');
        };
        //删除用户
        $scope.count = 0;
        var detectSelectedRow = function () {
            //检出是否选中用户
            var selectedRows = $scope.gridApi.selection.getSelectedRows();
            $scope.count = selectedRows.length;
            if ($scope.count < 1) {
              $('#noSelectionAlert').modal();
              return [];
            }
            return selectedRows;

        };
        $scope.deleteUser = function () {
            var selectedRows = detectSelectedRow();
            if (typeof(selectedRows) == 'undefined' || selectedRows.length == 0){
              return;
            }
            var param = [];
            angular.forEach(selectedRows, function (value) {
              param.push(value.account);
            });
            $scope.currentModal = $('#deleteUserConfirm').modal({
              onCancel: function () {
                  $scope.count = 0;
              }
            });
            $scope.deleteUserSubmit = function () {
              $rootScope.progressbar.start();
              sendRequest('/user/deleteAccount.action', {account: param}, function (data) {
                  $rootScope.progressbar.complete();
                  $rootScope.toastr.success('删除用户成功!');
                  $scope.currentModal.modal('close');
                  $scope.refreshGridData();

              }, function (data, code) {
                  //alert(data.det2ail);
                  $rootScope.progressbar.complete();
                  $scope.currentModal.modal('close');
                  $rootScope.toastr.error(data.detail || "未知错误");
              });
            };
            /*$scope.currentModal= $('#deleteUserConfirm').modal({
            onConfirm: function () {
            $rootScope.progressbar.start();
            sendRequest('/user/deleteAccount.action', {account: param}, function (data) {
            $rootScope.progressbar.complete();
            $scope.currentModal.modal('close');
            $scope.refreshGridData();

            }, function (data, code) {
            //alert(data.det2ail);
            $rootScope.progressbar.complete();
            $scope.currentModal.modal('close');
            $rootScope.toastr.error(data.detail || "未知错误");
            });
            },
            onCancel: function () {
            $scope.count = 0;
            }
            });*/
      };
        //重置用户密码
        $scope.resetUserPw = function () {
            var selectedRows = detectSelectedRow();
            if (typeof(selectedRows) == 'undefined' || selectedRows.length == 0){
                return;
            }
            $scope.currentModal = $('#resetUserPwConfirm').modal({
                onCancel: function () {
                    $scope.count = 0;
                }
            });
            $scope.resetUserPwSubmit = function () {
                $scope.modifyingUser = selectedRows[0];
                $scope.modifyingUser.password = $scope.modifyingUser.account;
                $rootScope.progressbar.start();
                sendRequest('/user/modifyAccount.action',$scope.modifyingUser, function (data) {
                    $rootScope.progressbar.complete();
                    $rootScope.toastr.success('重置密码成功!');
                    $scope.modifyingUser = {};
                    $scope.currentModal.modal('close');
                    $scope.refreshGridData();

                }, function (data, code) {
                    //alert(data.det2ail);
                    $rootScope.progressbar.complete();
                    $scope.currentModal.modal('close');
                    $rootScope.toastr.error(data.detail || "未知错误");
                });
            };
        };
        // 激活用户
        $scope.restoreUser = function () {
            var selectedRows = detectSelectedRow();
            if (typeof(selectedRows) == 'undefined' || selectedRows.length == 0)
              return;
            var param = [];
            angular.forEach(selectedRows, function (value, key) {
              param.push(value.account);
            });
            $('#restoreUserConfirm').modal({
              onConfirm: function () {
                  //submit restore user
                  $rootScope.progressbar.start();
                  sendRequest('/user/restoreAccount.action', {account: param}, function (data) {
                      $rootScope.progressbar.complete();
                      $rootScope.toastr.success('激活用户成功!');
                      $scope.refreshGridData();

                  }, function (data, code) {
                      //alert(data.detail);
                      $rootScope.progressbar.complete();
                      $rootScope.toastr.error(data.detail || "未知错误");
                  });
              },
              onCancel: function () {
                  $scope.count = 0;
              }
            });

        };
        //停用用户
        $scope.expiredUser = function () {
            // //检出是否选中用户
            // $scope.count = $scope.gridApi.grid.selection.selectedCount;
            // if($scope.count<1){
            //     $('#noSelectionAlert').modal();
            //     return;
            // }
            // var selectedRows = $scope.gridApi.selection.getSelectedRows();
            var selectedRows = detectSelectedRow();
            if (typeof(selectedRows) == 'undefined' || selectedRows.length == 0)
                return;
            var param = [];
            angular.forEach(selectedRows, function (value, key) {
                param.push(value.account);
            });
            //confirm框
            $('#expiredUserConfirm').modal({
                onConfirm: function () {
                    //submit expire user
                    $rootScope.progressbar.start();
                    sendRequest('/user/expiredAccount.action', {account: param}, function (data) {
                        $rootScope.progressbar.complete();
                        $scope.refreshGridData();
                    }, function (data, code) {
                        //alert(data.detail);
                        $rootScope.progressbar.complete();
                        $rootScope.toastr.error(data.detail || "未知错误");
                    });
                },
                onCancel: function () {
                    $scope.count = 0;
                }
            });

        };
        //移入用户
        $scope.addUserToTeam = function () {
            if ($scope.currentTeamId == -1) {
                //拒绝移除
                $rootScope.toastr.error("请选中一个用户组");
                return;
            }

        };
        //移除用户
        $scope.removeUserFromTeam = function () {
            if ($scope.currentTeamId == -1) {
                //拒绝移除
                $rootScope.toastr.error("无法移除当前组的用户");
                return;
            }
            var selectedRows = detectSelectedRow();
            if (typeof(selectedRows) == 'undefined' || selectedRows.length == 0)
                return;
            var param = [];
            angular.forEach(selectedRows, function (value, key) {
                param.push(value.id);
            });
            $scope.currentModal = $('#removeUserConfirm').modal({
                /*onConfirm: function () {
                    $rootScope.progressbar.start();
                    sendRequest('/grouper/removeMemberFromTeam.action', {
                        memberId: param,
                        id: $scope.currentTeamId
                    }, function (data) {
                        $rootScope.progressbar.complete();
                        $scope.refreshGridData();
                    }, function (data, code) {
                        //alert(data.detail);
                        $rootScope.progressbar.complete();
                        $rootScope.toastr.error(data.detail || "未知错误");
                    });
                },*/
                onCancel: function () {
                    $scope.count = 0;
                }
            });
            $scope.removeUserSubmit = function () {
                $rootScope.progressbar.start();
                sendRequest('/grouper/removeMemberFromTeam.action', {
                    memberId: param,
                    id: $scope.currentTeamId
                }, function (data) {
                    $rootScope.progressbar.complete();
                    $rootScope.toastr.success('移除用户成功!');
                    $scope.currentModal.modal('close');
                    $scope.refreshGridData();
                }, function (data, code) {
                    //alert(data.detail);
                    $rootScope.progressbar.complete();
                    $scope.currentModal.modal('close');
                    $rootScope.toastr.error(data.detail || "未知错误");
                });
            };
        };

        //修改用户
        $scope.modifyingUser = {};
        $scope.ifModify = false;//用户右侧栏显示哪些按钮的标记
        $scope.modifyUser = function () {
            var selectedRows = detectSelectedRow();
            if (typeof (selectedRows) == 'undefined' || selectedRows.length != 1) {
                $rootScope.toastr.error("请选择一个用户");
                return;
            }
            $scope.modifyingUser = selectedRows[0];
            $scope.modifyingUser.password = '';//密码不重复发送
            $scope.ifEdit = false;
            $scope.ifModify = true;
            $state.go('manager.userManagement.editUserDetailInfo');
            /*$scope.currentModal = $('#modifyUserModal').modal({
                onConfirm: function () {
                    $rootScope.progressbar.start();
                    sendRequest('/user/modifyAccount.action', $scope.modifyingUser, function (data) {
                        $rootScope.progressbar.complete();
                        $scope.refreshGridData();
                        $scope.modifyingUser = {};
                        $scope.currentModal.modal('close');
                    }, function (data, code) {
                        $rootScope.progressbar.complete();
                        $rootScope.toastr.error(data.detail || "未知错误");
                    });
                },
                onCancel: function () {
                    $scope.count = 0;
                    $scope.modifyingUser = {};
                    $scope.currentModal.modal('close');
                }
            });*/

        };
        $scope.modifyUserCancel = function () {
            $scope.count = 0;
            // $scope.modifyingUser = {};
            $scope.ifModify = false;
            $scope.ifEdit = true;
            $rootScope.$state.go('manager.userManagement.userDetailInfo');
        };
        $scope.modifyUserSubmit = function () {
            $rootScope.progressbar.start();
            if($scope.modifyingUser.account && $scope.modifyingUser.name) {
                sendRequest('/user/modifyAccount.action', $scope.modifyingUser, function (data) {
                    $rootScope.progressbar.complete();
                    $rootScope.toastr.success('修改用户成功!');
                    $scope.ifModify = false;
                    $scope.ifEdit = true;
                    $state.go('manager.userManagement.userDetailInfo');
                }, function (data, code) {
                    $rootScope.progressbar.complete();
                    $rootScope.toastr.error(data.detail || "未知错误");
                });
            }else{
                alert("必填项不能为空！");
            }
        };
        // 导出用户
        $scope.exportUser = function () {
            // location.href = $rootScope.path + '/user/exportAccount.action?';
            location.href = $rootScope.path + '/user/exportAccountFromTeam.action?teamId='+$scope.currentTeamId;

        };
        //导入用户
        $scope.uploadFile = {};
        $scope.importUser = function () {
            $scope.currentModal = $('#importUserModal').modal({

                onConfirm: function () {
                    if($scope.uploadFile){
                        Upload.upload({
                            // url: $rootScope.path + '/user/importAccount.action',
                            url: $rootScope.path + '/user/importAccountToTeam.action?teamId=' + $scope.currentTeamId,
                            data: {filedata:$scope.uploadFile},
                        }).then(function (data) {
                            if (data.status == 200) {
                                $rootScope.toastr.success(data.data.detail);
                                $scope.refreshGridData();
                                $scope.currentModal.modal('close');
                                $scope.uploadFile = {};
                            }
                        }, function (data) {
                            $rootScope.toastr.error(data.data.detail || "未知错误");
                        });
                    }else{
                        $rootScope.toastr.error("请选择要上传的文件");
                    }

                },
                onCancel: function () {
                    $scope.currentModal.modal('close');
                    $scope.uploadFile = {};
                }
            });
        };
        //用户组搜索框
        $scope.searchingGroup = '';
        $scope.searchGroup = function () {
            $scope.currentModal = $('#searchGroupModal').modal({
                onConfirm: function () {
                    if($scope.searchingGroup!=''){
                        sendRequest('/grouper/searchTeam.action', {name: $scope.searchingGroup}, function (data) {
                            if(data.members.length<1){
                                $rootScope.toastr.warning('没有找到相关的组！');
                            }
                            else{
                                $scope.tree = data.members;
                            }
                            $scope.searchingGroup = '';
                            $scope.currentModal.modal('close');

                        }, function (data, code) {
                            $rootScope.toastr.error(data.detail);
                        });
                    }else{
                        $scope.tree=[];
                        initTree();
                        $scope.currentModal.modal('close');
                    }

                },
                onCancel: function () {
                    $scope.currentModal.modal('close');

                }
            });
        };
        //用户搜索框
        $scope.searchingUser ={};
        $scope.searchUser = function(){
            $scope.currentModal = $('#searchUserModal').modal({
                onConfirm:function(){
                    sendRequest('/user/advancedSearch.action',$scope.searchUser,function(data){
                        $scope.currentTeamId =-1;
                        $scope.advanceSearchParam = $scope.searchingUser;
                        $scope.refreshGridData();
                        $scope.currentModal.modal('close');
                        $scope.searchingUser = {};
                    },function(data,code){
                        $rootScope.toastr.error(data.detail);

                    });
                },
                onCancel:function(){
                    $scope.currentModal.modal('close');
                    $scope.searchingUser = {};
                }
            });
        }


    }]);
    /*.directive('pwCheck', [function () {
        return {
            require: 'ngModel',
            link: function (scope, elem, attrs, ctrl) {
                var firstPassword = '#' + attrs.pwCheck;
                elem.add(firstPassword).on('keyup', function () {
                    scope.$apply(function () {
                        var v = elem.val()===$(firstPassword).val();
                        ctrl.$setValidity('pwmatch', v);
                    });
                });
            }
        }
    }])*/