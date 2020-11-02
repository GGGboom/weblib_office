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
            $scope.selectedNode ={};
            $scope.domainDetailInfo = {};
            $scope.ifEdit = false;
            $scope.ifModify = false;
            $scope.ifAdd = false;
            $scope.usersInDomainData =[];
            var initTree = function(){
                sendRequest('/grouper/getManagerDomainTree.action',
                    {}, function (data) {
                        $scope.folders =data.folder;
                        $scope.teams = data.members;
                        $scope.members = data.member;
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
                    sendRequest('/domain/getDomainInfo_v2.action',
                        {domainId:$scope.selectedNode.domainId}, function (data) {
                            $scope.domainDetailInfo = data;
                            var myDate = new Date(data.createDate.time);
                            myDate = myDate.getFullYear() + '-'+(myDate.getMonth()+1 < 10 ? '0'+(myDate.getMonth()+1) : myDate.getMonth()+1) + '-'
                            +myDate.getDate() + ' '+myDate.getHours() + ':'+myDate.getMinutes() + ':'+myDate.getSeconds();
                            $scope.domainDetailInfo.createDate = myDate;
                            $scope.ifEdit = true;
                            $state.go('manager.domainManagement.domainDetailInfo');
                        },function (data,code) {
                            $rootScope.toastr.error(data.detail || '未知错误');
                        });
                    $scope.advanceSearchParam ={};//防止高级搜索参数影响结果
                    //$scope.refreshGridData();
                }
                if(_thisNode.type == 'team'){
                    sendRequest('/grouper/grouperTree.action', {
                            id: _thisNode.id,
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
                            $scope.usersInDomainGridOptions.maxRowsToShow = Math.min($scope.pagingOptions.pageSize, data.members.length);
                            $scope.usersInDomainGridOptions.minRowsToShow = Math.min($scope.pagingOptions.pageSize, data.members.length);
                            $scope.usersInDomainGridOptions.data = data.members;
                            $scope.gridHeight = 35 * ($scope.usersInDomainGridOptions.minRowsToShow + 1);
                            $scope.gridApi.grid.modifyRows($scope.usersInDomainGridOptions.data);
                        });
                }
            };
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
            
            //createDomainOnFolder
            $scope.createDomainOnFolder = function () {
                $scope.ifAdd = true;
                $state.go('manager.domainManagement.createDomainOnFolder');
            }
            $scope.addDomainMembers = function () {
                $('#addDomainMembersModal').modal();
            }
        }]);