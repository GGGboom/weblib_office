/**
 * Created by QQChan on 2018/2/06.
 */
angular.module('domainManagementController',[])
.controller('domainManagementController',
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
            $scope.folders =[];
            $scope.teams = [];
            $scope.members = [];//管理员？
            $scope.selectedNode ={};//域结构管理
            $scope.domainDetailInfo = {};
            $scope.ifEdit = false;
            $scope.ifModify = false;//修改的提交、取消按钮
            $scope.ifDOnF = false;//创建域的提交、取消按钮
            $scope.ifDWithNF = false;//创建子域的提交、取消按钮
            $scope.usersInDomainData =[];
            $scope.currentModal={};

            var initTree = function(){
                sendRequest('/grouper/getManagerDomainTree.action',
                    {id:-1}, function (data) {
                        $scope.folders.push({id:0,text: "用户根", type:"rootFolder",associated:true,domainId:1,nodes: data.folder.concat(data.member).concat(data.team)});
                        $scope.selectedNode = $scope.folders[0];
                    },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                });

            };
            initTree();
            //设置选中节点的样式
            $scope.isSelected = function (scope) {
                if ($scope.selectedNode == scope.$modelValue) {
                    return {'background-color': '#3bb4f2'};
                }
                return {'background-color': '#ffffff'};
            };

            //设置选中节点的操作
            $scope.selectNode = function (scope) {
                var _thisNode = scope.$modelValue;
                $scope.selectedNode = _thisNode;
                //如果节点已经是域，获取域详细信息，否则显示未创建域
                if (_thisNode.associated) {
                    getDomainInfo();

                    $scope.ifEdit = true;
                    $scope.ifDOnF = false;
                    $scope.ifDWithNF = false;
                    $scope.ifModify = false;
                    $state.go('manager.domainManagement.domainDetailInfo');

                    $scope.advanceSearchParam ={};//防止高级搜索参数影响结果

                }else{
                    noDomainSetBtn();
                    $state.go('manager.domainManagement.noDomainDetailInfo');
                }

            };
            $scope.openDomainTree = function (scope) {
                var _thisNode = scope.$modelValue;
                $scope.selectedNode = _thisNode;
                //如果该节点不是组节点，且子节点数据已经在scope中，则直接打开
                if (typeof (_thisNode.nodes) != 'undefined' && _thisNode.type != 'team' ) {
                    scope.toggle();
                    return false;
                }
                //如果该节点不是组节点，但数据不在scope中，则重新请求数据
                if (_thisNode.type != 'team' && typeof (_thisNode.nodes) == 'undefined') {
                    sendRequest('/grouper/getManagerDomainTree.action',
                        {
                            id: _thisNode.id
                        }, function (data) {
                            //拼接data.folder/data.team/data.member成为新的数组
                            //array.concat(array2)返回一个新的数组
                            _thisNode.nodes = data.folder.concat(data.member).concat(data.team);
                            scope.toggle();
                        });
                    return false;
                }
                if(_thisNode.type == 'team'){
                    sendRequest('/grouper/getManagerDomainTree.action', {
                            id: _thisNode.id
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
                            $scope.usersInDomainGridOptions.maxRowsToShow = Math.min($scope.pagingOptions.pageSize, data.members.length);
                            $scope.usersInDomainGridOptions.minRowsToShow = Math.min($scope.pagingOptions.pageSize, data.members.length);
                            $scope.usersInDomainGridOptions.data = data.members;
                            $scope.gridHeight = 35 * ($scope.usersInDomainGridOptions.minRowsToShow + 1);
                            $scope.gridApi.grid.modifyRows($scope.usersInDomainGridOptions.data);
                        });
                }
            }
            $scope.pagingOptions = {
                // pageSize: 15,
                pageSize:24,
                currentPage: 1,
                start: 0,
                totalPage: 0
            };
            $scope.usersInDomainGridOptions = {
                data:'usersInDomainData',
                multiSelect: true,/*控制点中选中框时是否可以多选，而不是对行的设置*/
                enableRowSelection:false,
                enableSelectAll: true,/*控制全选按钮是否可用*/
                enableRowHeaderSelection: true,
                rowHeight: 35,
                headerRowHeight: 35,
                enableSorting: true,
                enableColumnMenus: false,
                enablePaging: true,
                showFooter: false,
                totalServerItems: 'totalServerItems',
                minRowsToShow: $scope.pagingOptions.pageSize,
                maxRowsToShow: $scope.pagingOptions.pageSize,
                pagingOptions: $scope.pagingOptions,
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
            }
            

            $scope.newDomain = {};
            $scope.newSubDomain = {};
            $scope.userTree = [];//可设置管理员列表
            $scope.selectedManager = {};//选中用户
            $scope.selectedManagerData =[];
            $scope.isSelectedManagers = false;//判断是否有域管理员
            $scope.domainAvailableCapacity = [];//域可用来源
            $scope.modifyDomainDetailInfo = {};
            /*
             **function: createDomainOnFolder
             * desc: 创建域
             * */
            $scope.createDomainOnFolder = function () {
                $scope.domainAvailableCapacity = [];
                $scope.ifEdit = false;
                $scope.ifDOnF = true;
                $scope.ifDWithNF = false;
                $scope.ifModify = false;
                getACS();//获取域可用来源
                $state.go('manager.domainManagement.createDomainOnFolder');
            }
            /*
             **function: createDomainOnFolder
             * desc: 创建域，提交按钮
             * */
            $scope.createDOnFSubmit = function () {
                var selectedFromDomainId = $("#dAC option:selected").attr("id");
                $scope.newDomain.folderIds = $scope.selectedNode.id;
                $scope.newDomain.fromDomainId = selectedFromDomainId;
                $scope.newDomain.memberId =[];
                angular.forEach($scope.selectedManagerData,function (obj) {
                    $scope.newDomain.memberId.push(obj.id);
                })
                sendRequest('/domain/createDomainOnFolder_v2.action',$scope.newDomain,
                    function (data) {
                        $rootScope.toastr.success(data.detail);
                        $scope.refreshTreeData();
                        $scope.newDomain ={};
                        noDomainSetBtn();
                        $state.go('manager.domainManagement.noDomainDetailInfo');
                    },function (data,code) {
                        $rootScope.toastr.error(data.detail || '未知错误');
                    });

            };
            /*
             **function: createDomainOnFolder
             * desc: 创建域，取消按钮
             * */
            $scope.createDOnFCancel = function () {
                $scope.newDomain ={};
                noDomainSetBtn();
                $state.go('manager.domainManagement.noDomainDetailInfo');

            };
            /*
             **function: createDomainWithNewFolder
             * desc: 创建子域
             * */
            $scope.createDomainWithNewFolder = function () {
                $scope.ifEdit = false;
                $scope.ifDOnF = false;
                $scope.ifDWithNF = true;
                $scope.ifModify = false;
                getACS();//获取域可用来源
                $state.go('manager.domainManagement.createDomainWithNewFolder');
            }
            $scope.createDWithNFSubmit = function () {
                var selectedFromDomainId = $("#dAC option:selected").attr("id");
                $scope.newSubDomain.parentFolderId = $scope.selectedNode.id;
                $scope.newSubDomain.fromDomainId = selectedFromDomainId;
                $scope.newSubDomain.memberId =[];
                angular.forEach($scope.selectedManagerData,function (obj) {
                    $scope.newSubDomain.memberId.push(obj.id);
                })
                sendRequest('/domain/createDomainWithNewFolder.action',$scope.newSubDomain,
                    function (data) {
                        $rootScope.toastr.success("创建子域成功！");
                        $scope.refreshTreeData();
                        $scope.newSubDomain ={};
                        noDomainSetBtn();
                        $state.go('manager.domainManagement.noDomainDetailInfo');
                    },function (data,code) {
                        $rootScope.toastr.error(data.detail || '未知错误');
                    });
            };
            $scope.createDWithNFCancel = function () {
                $scope.newSubDomain ={};
                getDomainInfo();
                $scope.ifModify = false;
                $scope.ifEdit = true;
                $scope.ifDOnF = false;
                $scope.ifDWithNF = false;
                $state.go('manager.domainManagement.domainDetailInfo');
            }
            /*function:deleteDomain
             * desc:删除域
             */
            $scope.deleteDomain = function () {
                sendRequest('/domain/deleteDomain_v2.action',{domainId:$scope.selectedNode.domainId},
                    function (data) {
                        $rootScope.toastr.success("成功删除域！");
                        $scope.refreshTreeData();
                        $scope.ifModify = false;
                        $scope.ifEdit = false;
                        $scope.ifDOnF = false;
                        $scope.ifDWithNF = false;
                        $state.go('manager.domainManagement.noDomainDetailInfo');
                    },function (data,code) {
                        $rootScope.toastr.error(data.detail || '未知错误');
                });

            }
            /*function:deleteDomain
             * desc:修改域信息
             */
            $scope.modifyDomain = function () {
                $scope.modifyDomainDetailInfo.name = $scope.domainDetailInfo.domainName;
                $scope.modifyDomainDetailInfo.domainId = $scope.domainDetailInfo.domainId;
                $scope.modifyDomainDetailInfo.desc = $scope.domainDetailInfo.desc;
                $scope.modifyDomainDetailInfo.capacity = $scope.domainDetailInfo.availableCapacity;
                $scope.modifyDomainDetailInfo.fromDomainId = $scope.domainDetailInfo.fromDomainId;
                $scope.ifModify = true;
                $scope.ifEdit = false;
                $scope.ifDOnF = false;
                $scope.ifDWithNF = false;
                console.log($scope.modifyDomainDetailInfo);
                console.log($scope.domainDetailInfo);
                
                $state.go('manager.domainManagement.editDomainDetailInfo');

            };
            $scope.modifyDomainSubmit = function () {
                sendRequest('/domain/editDomain_v2.action',$scope.modifyDomainDetailInfo,
                    function (data) {
                        $rootScope.toastr.success("修改成功！");
                        $scope.modifyDomainDetailInfo ={};
                        getDomainInfo();
                        $scope.ifModify = false;
                        $scope.ifEdit = true;
                        $scope.ifDOnF = false;
                        $scope.ifDWithNF = false;
                        $state.go('manager.domainManagement.domainDetailInfo');
                    },function (data,code) {
                        $rootScope.toastr.error(data.detail || '未知错误');
                    });
            };
            $scope.modifyDomainCancel = function () {
                $scope.modifyDomainDetailInfo ={};
                $scope.ifModify = false;
                $scope.ifEdit = true;
                $scope.ifDOnF = false;
                $scope.ifDWithNF = false;
                $state.go('manager.domainManagement.domainDetailInfo');
            }
            /*
             **function: createDomainWithNewFolder
             * desc: 创建域和子域时，显示域可用容量来源
             * */
            var getACS  = function () {
                sendRequest('/domain/getAvailableCapacitySource.action',{folderIds:$scope.selectedNode.id},
                    function (data) {
                        $scope.domainAvailableCapacity = data.availableSource;
                        angular.forEach($scope.domainAvailableCapacity, function (value, key) {
                            value.availableCapacity = value.availableCapacity/1048576;
                            value.totalCapacity = value.totalCapacity/1048576;
                        });
                    },function (data,code) {
                        $rootScope.toastr.error(data.detail || '未知错误');
                    });
            }
            var getDomainInfo = function () {
                sendRequest('/domain/getDomainInfo_v2.action',
                    {domainId:$scope.selectedNode.domainId}, function (data) {
                        $scope.domainDetailInfo = data;
                        var myDate = new Date(data.createDate.time);
                        myDate = myDate.getFullYear() + '-'+(myDate.getMonth()+1 < 10 ? '0'+(myDate.getMonth()+1) : myDate.getMonth()+1) + '-'
                            +myDate.getDate() + ' '+myDate.getHours() + ':'+myDate.getMinutes() + ':'+myDate.getSeconds();
                        $scope.domainDetailInfo.createDate = myDate;
                    },function (data,code) {
                        $rootScope.toastr.error(data.detail || '未知错误');
                    }
                );
            };
            var noDomainSetBtn = function () {
                $scope.ifModify = false;
                $scope.ifEdit = false;
                $scope.ifDOnF = false;
                $scope.ifDWithNF = false;
            };
            var getUserTree = function () {
                $scope.userTree = [];
                sendRequest('/grouper/grouperTree.action',{id:$scope.selectedNode.id},
                    function (data) {
                        var rootUser = $scope.selectedNode;
                        rootUser.nodes = data.members;
                        $scope.userTree.push(rootUser);
                    },function (data,code) {
                        $rootScope.toastr.error(data.detail || '未知错误');
                    });
            }
            /*function:addDomainMembers
             * desc:添加管理员按钮-打开添加域管理员modal
             */
            $scope.addDomainMembers = function () {
                getUserTree();
                $scope.currentModal = $('#addDomainMembersModal').modal({
                    onCancel: function () {
                        $scope.currentModal.modal('close');
                        $scope.selectedManager ={};
                        $scope.userTree=[];
                        $scope.selectedManagerData =[];
                        $scope.isSelectedManagers = false;
                    }
                });
            };
            $scope.addSubDomainMembers = function () {
                getUserTree();
                $scope.currentModal = $('#addSubDomainMembersModal').modal({
                    onCancel: function () {
                        $scope.currentModal.modal('close');
                        $scope.selectedManager ={};
                        $scope.userTree=[];
                        $scope.selectedManagerData =[];
                        $scope.isSelectedManagers = false;
                    }
                });
            }
            $scope.addDomainMembersSubmit = function () {
                $scope.currentModal.modal('close');
                $scope.isSelectedManagers = true;
            };
            /*function:isUserSelected
             * desc:添加管理员modal-选中某个用户触发的样式变化
             */
            $scope.isUserSelected = function (scope) {
                if ($scope.selectedManager == scope.$modelValue) {
                    return {'background-color': '#3bb4f2'};
                }
                return {'background-color': '#ffffff'};
            };
            /*function:selectManager
             * desc:添加管理员modal-选中某个用户触发的操作
             */
            $scope.selectManager = function (scope) {
                var _thisNode = scope.$modelValue;
                $scope.selectedManager = _thisNode;
            };
            /*function:openUserTree
             * desc:添加管理员modal-左边用户组织/组列表显示
             */
            $scope.openUserTree = function (scope) {
                var _thisNode = scope.$modelValue;
                $scope.selectedManager = _thisNode;
                if (typeof (_thisNode.nodes) != 'undefined' ) {
                    scope.toggle();
                    return false;
                }
                //如果该节点不是组节点，但数据不在scope中，则重新请求数据
                if (!_thisNode.leaf && _thisNode.type!='person' && typeof (_thisNode.nodes) == 'undefined') {
                    sendRequest('/grouper/grouperTree.action',
                        {
                            id: _thisNode.id
                        }, function (data) {
                            //拼接data.folder/data.team/data.member成为新的数组
                            //array.concat(array2)返回一个新的数组
                            _thisNode.nodes = data.members;
                            scope.toggle();
                        });
                    return false;
                }
            }
            /*define:managerPagingOptions
             * desc:添加管理员modal-域管理员列表单页显示的记录数
             */
            $scope.managerPagingOptions = {
                pageSize: 10,
                currentPage: 1,
                start: 0,
                totalPage: 0
            };
            /*define:managerGridOptions
             * desc:添加管理员modal-域管理员列表ui-grid参数设置
             */
            $scope.managerGridOptions = {
                data:'selectedManagerData',
                    multiSelect: false,/*控制点中选中框时是否可以多选，而不是对行的设置*/
                    enableRowSelection:true,
                    enableSelectAll: false,/*控制全选按钮是否可用*/
                    enableRowHeaderSelection:false,
                    rowHeight: 35,
                    headerRowHeight: 35,
                    enableSorting: false,
                    enableColumnMenus: false,
                    enableHorizontalScrollbar:0,
                    enableVerticalScrollbar:0,
                    //enableScrollbars:false,
                    enablePaging: true,
                    showFooter: false,
                    totalServerItems: 'totalServerItems',
                    minRowsToShow: $scope.managerPagingOptions.pageSize,
                    maxRowsToShow: $scope.managerPagingOptions.pageSize,
                    pagingOptions: $scope.managerPagingOptions,
                    selectionRowHeaderWidth:35,
                    columnDefs:[
                        {
                            field: "text", width: '*', displayName: '账号', headerCellClass: 'gridHead'
                        }
                ],
                onRegisterApi: function(gridApi){ $scope.gridApi = gridApi;}
            };
            /*function:addToGrid
             * desc:添加管理员modal-添加按钮-将用户添加到域管理员列表
             */
            $scope.addToGrid = function () {
                $scope.selectedManagerData.push($scope.selectedManager);
                $scope.gridApi.core.refresh();
            };
            /*function:removeFromGrid
             * desc:添加管理员modal-删除按钮-将用户从域管理员列表删除
             */
            $scope.removeFromGrid = function () {
                var toDeleteRow = $scope.gridApi.selection.getSelectedGridRows();
                var toDeleteIndex = $scope.selectedManagerData.indexOf(toDeleteRow[0].entity);
                $scope.selectedManagerData.splice(toDeleteIndex,1);
                $scope.gridApi.core.refresh();
            }
            /*function:isAddBtinDisabled
            * desc:添加管理员modal-判断添加按钮是否可用
            *      1.当前选中的treeNode不为用户时，不可用
            *      2.当前用户已经被添加，不可用
            * return:boolean
            *      1.true:按钮不可用
            */
            $scope.isAddBtnDisabled =function () {
                if($scope.selectedManager.type!='person'){
                    return true;
                }else if($scope.selectedManagerData.indexOf($scope.selectedManager) != -1){
                    return true;
                }
                return false;
            }
            $scope.refreshTreeData = function (scope) {
                $scope.folders = [];
                $rootScope.progressbar.start();
                sendRequest('/grouper/getManagerDomainTree.action',
                    {id:-1}, function (data) {
                        $rootScope.progressbar.complete();
                        $scope.folders.push({id:0,text: "用户根", type:"rootFolder",associated:true,domainId:1,nodes: data.folder.concat(data.member).concat(data.team)});
                        $scope.selectedNode = $scope.folders[0];
                    },function (data,code) {
                        $rootScope.toastr.error(data.detail || '未知错误');
                    });

            }

        }]);