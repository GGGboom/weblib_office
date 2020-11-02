angular.module('systemManagementController',[])
    .controller('systemManagementController',['$scope','$rootScope','httpRequest.sendRequest','$state',
        function($scope,$rootScope,sendRequest,$state){
            //init panel height
            $scope.windowHeight = angular.element(window).height();
            angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
            angular.element(window).resize(function () {
                $scope.windowHeight = angular.element(window).height();
                angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
            });


            $scope.sysTabName = {'GLOBALSETTING': 'globalSetting', 'STORAGEICONSET': 'storageIconSet',
                                 'LOGINLOG':'loginLog','UPLOADLOG':'uploadLog','DOWNLOADLOG':'downloadLog',
                                 'OPERATELOG':'operateLog','ERRORLOG':'errorLog'};//tabName enum
            var currentTab = '';
            $scope.ifEdit = false;
            $scope.ifModify = false;
            $scope.globalSetting ={};
            $scope.editGlobalSetting ={};
            //初始化
            var initAppList = function () {
                currentTab = $scope.sysTabName.GLOBALSETTING;
                sendRequest('/global/getGlobalConfig.action',{},function (data) {
                    $scope.globalSetting = data;
                    $state.go('manager.systemManagement.globalSetting');
                    $scope.ifEdit = true;
                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                })
            };
            initAppList();
            //设置选中的行
            $scope.isTabActive = function (sysTabName) {
                if (currentTab == sysTabName)
                    return {'background-color': '#3bb4f2'};
                return {'background-color': '#ffffff'};
            };
            //全局设置
            $scope.showGlobalSetting = function () {
                currentTab = $scope.sysTabName.GLOBALSETTING;
                sendRequest('/global/getGlobalConfig.action',{},function (data) {
                    $scope.globalSetting = data;
                    $state.go('manager.systemManagement.globalSetting');
                    $scope.ifEdit = true;
                    $scope.ifModify = false;
                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                })

            };
            //文件柜图标设置
            $scope.icons = [];
            $scope.iconsTotalCount = 0;
            $scope.showStorageIconSet = function () {
                sendRequest('/group/getGroupIcons.action',{_t:'t_', start:0, limit:30},function (data) {
                    $scope.icons = data.icons;
                    $scope.iconsTotalCount = data.totalCount;
                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                });
                $scope.ifEdit = false;
                $scope.ifModify = false;
                $state.go('manager.systemManagement.storageIconSet');
                currentTab = $scope.sysTabName.STORAGEICONSET;

            };
            //登陆日志
            $scope.loginLog = function () {
                sendRequest('/log/getLatestLoginLogs.action?start=0&limit=35&tmp=1228554953',{},function (data) {
                    $scope.loginLogData= data.loginLogs;
                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                })
                $scope.ifEdit = false;
                $scope.ifModify = false;
                currentTab = $scope.sysTabName.LOGINLOG;
                $state.go('manager.systemManagement.loginLog');

            };
            //上传日志
            $scope.uploadLog = function () {
                sendRequest('/log/getLatestUploadLogs.action?start=0&limit=35&tmp=1167164631',{},function (data) {
                    $scope.uploadLogData= data.uploadLogs;
                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                })
                $scope.ifEdit = false;
                $scope.ifModify = false;
                currentTab = $scope.sysTabName.UPLOADLOG;
                $state.go('manager.systemManagement.uploadLog');

            };
            //下载日志
            $scope.downloadLog = function () {
                sendRequest('/log/getLatestDownloadLogs.action?start=0&limit=35&tmp=-207782557',{},function (data) {
                    $scope.downloadLogData= data.downloadLogs;
                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                })
                $scope.ifEdit = false;
                $scope.ifModify = false;
                currentTab = $scope.sysTabName.DOWNLOADLOG;
                $state.go('manager.systemManagement.downloadLog');
            };
            //操作日志
            $scope.operateLog = function () {
                sendRequest('/log/getLatestOperateLogs.action?start=0&limit=35&tmp=1415208060', {}, function (data) {
                    $scope.operateLogData = data.operateLogs;

                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                })
                $scope.ifEdit = false;
                $scope.ifModify = false;
                currentTab = $scope.sysTabName.OPERATELOG;
                $state.go('manager.systemManagement.operateLog');
            };
            //错误日志
            $scope.errorLog = function () {
                sendRequest('/log/getLatestErrorLogs.action?start=0&limit=35&tmp=655114382', {}, function (data) {
                    $scope.errorLogData = data.errorLogs;

                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                })
                $scope.ifEdit = false;
                $scope.ifModify = false;
                currentTab = $scope.sysTabName.ERRORLOG;
                $state.go('manager.systemManagement.errorLog');
            };
            //修改全局设置
            $scope.editGlobalSetting = function () {
                $scope.ifEdit = false;
                $scope.ifModify = true;
                currentTab = $scope.sysTabName.GLOBALSETTING;
                $state.go('manager.systemManagement.editGlobalSetting');
            };
            $scope.editGlobalSettingCancel = function () {
                $scope.editGlobalSetting ={};
                $scope.ifModify = false;
                $scope.ifEdit = true;
                $rootScope.$state.go('manager.systemManagement.globalSetting');
            };
            $scope.editGlobalSettingSubmit = function () {
                $rootScope.progressbar.start();
                console.log($scope.globalSetting);
                $scope.editGlobalSetting.site_domain = $scope.globalSetting.site_domain;
                $scope.editGlobalSetting.site_name = $scope.globalSetting.site_name;
                $scope.editGlobalSetting.moveToDifferentGroup =$scope.globalSetting.moveToDifferentGroup;
                $scope.editGlobalSetting.copyToDifferentGroup = $scope.globalSetting.copyToDifferentGroup;
                $scope.editGlobalSetting.smtp_sender  = $scope.globalSetting.smtp_sender;
                $scope.editGlobalSetting.smtp_sendername  = $scope.globalSetting.smtp_sendername;
                $scope.editGlobalSetting.smtp_auth =$scope.globalSetting.smtp_auth;
                $scope.editGlobalSetting.smtp_port = $scope.globalSetting.smtp_port;
                $scope.editGlobalSetting.smtp_username = $scope.globalSetting.smtp_username;
                $scope.editGlobalSetting.smtp_password = $scope.globalSetting.smtp_password;
                $scope.editGlobalSetting.smtp_host = $scope.globalSetting.smtp_host;
                $scope.editGlobalSetting.auditClientRegister = false;

                sendRequest('/global/manageSite.action',$scope.editGlobalSetting, function (data) {
                    $rootScope.progressbar.complete();
                    $rootScope.toastr.success('修改成功!');
                    $scope.ifModify = false;
                    $scope.ifEdit = true;
                    $state.go('manager.systemManagement.globalSetting');
                }, function (data, code) {
                    $rootScope.progressbar.complete();
                    $rootScope.toastr.error(data.detail || "未知错误");
                });

            };
            $scope.pagingOptions = {
                // pageSize: 15,
                pageSize:24,
                currentPage: 1,
                start: 0,
                totalPage: 0
            };
            $scope.folderIconsGridOptions = {
                data:'icons',
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
                columnDefs:[
                    {
                        field: "account", width: '20%', displayName: '图标',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "ip", width: '20%', displayName: '名称',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "createDate", width: '50%', displayName: '描述',
                        headerCellClass: 'gridHead'
                    },

                    {
                        field: "terminal", width: '*', displayName: '图片',
                        headerCellClass: 'gridHead'
                    }
                ]
            };
            $scope.loginLogGridOptions = {
                data:'loginLogData',
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
                columnDefs:[
                    {
                        field: "account", width: '20%', displayName: '账号',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "ip", width: '20%', displayName: 'IP地址',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "createDate", width: '20%', displayName: '登录时间',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "result", width: '10%', displayName: '登录状态',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "terminal", width: '*', displayName: '终端',
                        headerCellClass: 'gridHead'
                    }
                ]
            }
            $scope.uploadLogGridOptions = {
                data:'uploadLogData',
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
                columnDefs:[
                    {
                        field: "account", width: '20%', displayName: '操作者',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "ip", width: '20%', displayName: 'IP地址',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "createDate", width: '20%', displayName: '操作时间',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "groupName", width: '10%', displayName: '所在文件柜',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "terminal", width: '*', displayName: '终端',
                        headerCellClass: 'gridHead'
                    },
                ]
            }
            $scope.downloadLogGridOptions = {
                data:'downloadLogData',
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
                columnDefs:[
                    {
                        field: "targetObject", width: '15%', displayName: '下载文件名称',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "account", width: '15%', displayName: '操作者',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "ip", width: '20%', displayName: 'IP地址',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "createDate", width: '20%', displayName: '操作时间',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "groupName", width: '10%', displayName: '所在文件柜',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "terminal", width: '*', displayName: '终端',
                        headerCellClass: 'gridHead'
                    },
                ]
            }
            $scope.operateLogGridOptions = {
                data:'operateLogData',
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
                columnDefs:[
                    {
                        field: "account", width: '15%', displayName: '操作者',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "ip", width: '15%', displayName: 'IP地址',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "createDate", width: '20%', displayName: '操作时间',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "description", width: '30%', displayName: '操作描述',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "terminal", width: '*', displayName: '终端',
                        headerCellClass: 'gridHead'
                    },
                ]
            }
            $scope.errorLogGridOptions = {
                data:'errorLogData',
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
                columnDefs:[
                    {
                        field: "account", width: '15%', displayName: '操作者',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "ip", width: '15%', displayName: 'IP地址',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "createDate", width: '20%', displayName: '操作时间',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "description", width: '30%', displayName: '操作提示',
                        headerCellClass: 'gridHead'
                    },
                    {
                        field: "terminal", width: '*', displayName: '终端',
                        headerCellClass: 'gridHead'
                    },
                ]
            }
    }]);
