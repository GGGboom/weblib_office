angular.module('groupPermissionController', ['ui.grid.autoResize'])
    .controller('groupPermissionController',
        [
            '$scope',
            '$rootScope',
            'httpRequest.sendRequest',
            '$state', 'uiGridConstants',
            'uiGridSelectionService',
            'GridApi',
            function ($scope, $rootScope, sendRequest, $state, uiGridConstants, uiGridSelectionService, GridApi) {
                //init height
                $scope.windowHeight = angular.element(window).height();
                angular.element(document.querySelectorAll(".group-info-content")).height($scope.windowHeight - 245);
                angular.element(window).resize(function () {
                    $scope.windowHeight = angular.element(window).height();
                    angular.element(document.querySelectorAll(".group-info-content")).height($scope.windowHeight - 245);
                });
                $scope.data = [];//存放grid数据

                //userGrid init
                $scope.pagingOptions = {
                    pageSize: 19,
                    currentPage: 1,
                    start: 0,
                    totalPage: 0
                };
                $scope.permissionGridOptions = {
                    data: 'data',
                    multiSelect: true,
                    enableRowSelection: true,
                    enableSelectAll: true,
                    enableRowHeaderSelection: true,
                    rowHeight: 35,
                    headerRowHeight: 35,
                    enableSorting: true,
                    enableColumnMenus: false,
                    enablePaging: true,
                    showFooter: false,
                    totalServerItems: 'totalServerItems',
                    minRowsToShow: $scope.pagingOptions.pageSize,
                    //maxRowsToShow: $scope.pagingOptions.pageSize,
                    pagingOptions: $scope.pagingOptions,
                    enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
                    enableVerticalScrollbar: uiGridConstants.scrollbars.NEVER,


                    //rowTemplate: 'tpl/userGridTpls/rowTemplate.html',
                    columnDefs: [
                        {
                            field: "type", width: '10%', displayName: '类型',
                        },

                        {
                            field: "name", width: '*', displayName: '姓名'
                        },
                        {
                            field: "totalControl", width: '10%', displayName: '完全控制',
                            cellTemplate: 'tpl/permissionGridTpls/checkBoxCellTemplate.html'
                        },
                        {
                            field: "download", width: '10%', displayName: '下载',
                            cellTemplate: 'tpl/permissionGridTpls/checkBoxCellTemplate.html'
                        },
                        {
                            field: "upload", width: '10%', displayName: '上传',
                            cellTemplate: 'tpl/permissionGridTpls/checkBoxCellTemplate.html'
                        },
                        {
                            field: "addDir", width: '10%', displayName: '创建文件夹',
                            cellTemplate: 'tpl/permissionGridTpls/checkBoxCellTemplate.html'
                        },
                        {
                            field: "delete", width: '10%', displayName: '删除',
                            cellTemplate: 'tpl/permissionGridTpls/checkBoxCellTemplate.html'
                        },
                        {
                            field: "modify", width: '10%', displayName: '修改',
                            cellTemplate: 'tpl/permissionGridTpls/checkBoxCellTemplate.html'
                        }
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };
                $scope.refreshGridData = function (start) {
                    sendRequest('/user/getMembersInGroup.action',
                        {
                            groupId: $scope.currentGroupId,
                            start: start || 0,
                            limit: $scope.pagingOptions.pageSize
                        },
                        function (data) {
                            $scope.pagingOptions.totalPage = Math.ceil(data.members.length / $scope.pagingOptions.pageSize);
                            $scope.gridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL);
                            $scope.permissionGridOptions.data = data.members;
                            $scope.permissionGridOptions.maxRowsToShow = Math.min($scope.pagingOptions.pageSize, data.members.length);
                            $scope.permissionGridOptions.minRowsToShow = Math.min($scope.pagingOptions.pageSize, data.members.length);
                            $scope.gridHeight = 35 * ($scope.permissionGridOptions.minRowsToShow + 1);
                            //$scope.gridApi.core.notifyDataChange(uiGridConstants.dataChange.OPTIONS);
                            $scope.gridApi.grid.modifyRows($scope.permissionGridOptions.data);
                            $scope.gridApi.grid.refreshCanvas(true);
                            //刷新grid后，新数据是未修改过的
                            $scope.currentGroupPermissionChanged = false;

                        });
                };
                $scope.$watch('currentGroupId', function (newValue, oldValue) {
                    $scope.refreshGridData();
                });
                //$scope.gridData = {};
                $scope.editedRows = {};
                $scope.currentGroupPermissionChanged = false;
                $scope.permissionChanged = function (row, col) {
                    $scope.currentGroupPermissionChanged = true;
                    //如果修改的是‘完全控制’，则影响其余权限选项
                    if (col.field == 'totalControl') {
                        var flag = row.entity.totalControl;
                        row.entity.upload = flag;
                        row.entity.addDir = flag;
                        row.entity.delete = flag;
                        row.entity.modify = flag;
                    }
                    $scope.editedRows[row.uid] = row;

                };
                //权限已经修改alert框 按钮响应
                $scope.cancelEditedPermission = function () {
                    $scope.refreshGridData();
                };
                $scope.submitEditedPermission = function () {
                    //设置参数
                    var memberIdList =[];
                    var permList = [];
                    var permOpt = ['upload','addDir','modify','delete'];
                    for (var row in $scope.editedRows){
                        memberIdList.push($scope.editedRows[row].entity.id);
                        var permStr = '';
                        for(var field in permOpt){
                            if($scope.editedRows[row].entity[permOpt[field]]){
                                permStr +=(permOpt[field]+"|");
                            }
                        }
                        console.log(permStr);
                        permList.push(permStr);
                    }
                    //提交
                    sendRequest('/user/modifyMemberGroupPermission.action',
                        {
                            groupId:$scope.currentGroupId,
                            memberId:memberIdList,
                            perm:permList
                        },
                        function(data){
                            $scope.refreshGridData();
                            $rootScope.toastr.success('修改成功！');
                        });
                };


            }])