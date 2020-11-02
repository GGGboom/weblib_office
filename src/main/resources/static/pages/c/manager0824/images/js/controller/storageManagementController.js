angular.module('storageManagementController', [])
    .controller('storageManagementController',
                ['$scope',
                    '$rootScope',
                    'httpRequest.sendRequest',
                    '$state',
                    function ($scope, $rootScope, sendRequest, $state) {
        //init panel height
        $scope.windowHeight = angular.element(window).height();
        angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
        angular.element(document.querySelectorAll(".group-info-content")).height($scope.windowHeight - 138);
        angular.element(window).resize(function () {
            $scope.windowHeight = angular.element(window).height();
            angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
            angular.element(document.querySelectorAll(".group-info-content")).height($scope.windowHeight - 138);
        });


        $scope.tree = [];
        $scope.selectedNode = {};//选中的柜子树节点
        $scope.currentGroupId = -1;//未选中任何柜子
        $scope.currentCategoryId = -1;//未选中任何分类
        $scope.adding = false;//当正在添加分类，或柜子时，为true

        var initTree = function () {
            sendRequest('/category/getCategoriesWithManagedGroup.action',
                {
                    parentId: 0,
                    type: 'all',
                    recursion: false
                }, function (data) {
                    //build the tree
                    console.log(data);
                    var _nodes = data.categories.concat(data.groups);
                    $scope.tree.push({displayName: '根目录', nodes: _nodes, leaf: false});

                },function(data,code){
                    $rootScope.toastr.error(code + data.detail);

                });
        };
        initTree();

        $scope.openTree = function (scope) {
            var _thisNode = scope.$modelValue;
            $scope.selectedNode = _thisNode;
            //如果该节点不是柜子节点，且子节点数据已经在scope中，则直接打开
            if (typeof (_thisNode.nodes) != 'undefined' && _thisNode.leaf == false) {
                scope.toggle();
                return false;
            }
            //如果该节点不是组节点，但数据不在scope中，则重新请求数据
            if (_thisNode.leaf == false && typeof (_thisNode.nodes) == 'undefined') {
                sendRequest('/category/getCategoriesWithManagedGroup.action', {
                        parentId: _thisNode.id,
                        type: 'all',
                        recursion: false
                    }, function (data) {
                        _thisNode.nodes = data.categories.concat(data.groups);
                        scope.toggle();
                    },function(data,code){
                    $rootScope.toastr.error(code + data.detail);

                });
                return false;
            }
            console.log("opentree");
        };
        //刷新树节点（新增文件柜，新增分类之后刷新）
        var refreshSingleNode = function(node){
            sendRequest('/category/getCategoriesWithManagedGroup.action',
                {
                    parentId: node.id,
                    type: 'all',
                    recursion: false
                }, function (data) {
                    node.nodes = data.categories.concat(data.groups);

                },function(data,code){
                    $rootScope.toastr.error(code + data.detail);

                });
            console.log("refreshsingle");
        };

        $scope.isSelected = function (scope) {
            if ($scope.selectedNode == scope.$modelValue) {
                return {'background-color': 'aliceblue'};
            }
            return {'background-color': '#ffffff'};
        };
        $scope.selectNode = function (scope) {
            var _thisNode = scope.$modelValue;
            $scope.selectedNode = _thisNode;
            //如果节点是柜子节点，则打开右边权限和详细信息
            if (_thisNode.categoryId > 0) {//暂时这样判断柜子和分类,拥有categoryId属性的为柜子，否则为分类，weblib重构后需要修改
                //选中的是柜子
                console.log("open gridView");
                if ($scope.currentGroupId == _thisNode.id) {
                    return;
                }
                $scope.currentGroupName = _thisNode.displayName;
                $scope.currentGroupId = _thisNode.id;
                $scope.showPermission();
                $scope.showDetailInfo();
            } else
                {
                //选中的是分类分类
                if($scope.currentCategoryId != _thisNode.id){
                    $scope.currentCategoryId = _thisNode.id;
                    $scope.adding = false;
                }

            }
        };
        $scope.showDropdownMenu = function (scope) {
            if ($scope.selectedNode == scope.$modelValue)
                return true;
            return false;
        }
        //创建文件柜
        $scope.createGroup = function () {
            $scope.currentGroupName = '添加文件柜';
            $scope.adding = true;
            $scope.addingGroup =true;
            $scope.addingCategory =false;
            $scope.newGroup = {
                permissionType: 2,
                // categoryId:301,
                // name:1470291433501,
                // addr:1470291433501,
                displayName:'',
                desc:'',
                iconId: 0,
                totalSize: 2,
                singleFileSize: 1048576,
                paiban: 'Landscape',
                documentType: 12,
            };
        };

        $scope.createGroupSubmit = function () {
            //查重名
            sendRequest('/group/checkGroupDisplayNameExists.action',
                {
                    categoryId:$scope.currentCategoryId ,
                    displayName:$scope.newGroup.displayName
                },function(data){
                    if(data.exists){//存在重名
                        $rootScope.toastr.error('该分类下已经存在同名的文件柜，请选用其它名称!');
                    }
                    else{
                        var _param = $scope.newGroup;
                        _param.categoryId=$scope.currentCategoryId;
                        _param.totalSize = $scope.newGroup.totalSize * 1048576;
                        var _time = new Date().getTime();
                        _param.name = _time;
                        _param.addr = _time;
                        sendRequest('/group/createGroup.action',_param,
                            function(data){
                                $rootScope.toastr.success('文件柜创建成功!');
                                $scope.selectedNode.leaf = false;//已经创建了柜子，分类leaf 属性为false
                                refreshSingleNode($scope.selectedNode);
                                $scope.adding = false;
                            });
                    }
                });
        }
        $scope.cancelAdding = function(){
            $scope.adding =false;
        };
        //创建分类
        $scope.createCategory = function(){
            $scope.currentGroupName = '添加分类';
            $scope.adding = true;
            $scope.addingCategory =true;
            $scope.addingGroup =false;
            $scope.newCategory = {
                parentId:$scope.currentCategoryId ,
                name:new Date().getTime(),
                displayName:'',
                desc:''
            }
        };

        $scope.createCategorySubmit = function(){

            sendRequest('/category/createCategory.action',$scope.newCategory,function(data){
                $rootScope.toastr.success('文件柜创建成功!');
                refreshSingleNode($scope.selectedNode);
                $scope.adding = false;
            });
        };

       /* $scope.tabName = {'DEFAULT': '', 'PERMISSION': 'permission', 'DETAILINFO': 'detailInfo'};//tabName enum
        var currentTab = $scope.tabName.DEFAULT;*/

        $scope.showPermission = function () {
            $state.go('manager.storageManagement.permission');

            // currentTab = $scope.tabName.PERMISSION;
        };
        $scope.isgroupDetailsHide = true;
        $scope.showDetailInfo = function () {
            $scope.isgroupDetailsHide = false;
            //$state.go('manager.storageManagement.detailInfo');
            // currentTab = $scope.tabName.DETAILINFO;
            $scope.groupInfo={
                displayName : $scope.selectedNode.displayName,
                totalSize:'',
                remainSize:'',
                createTime:'',
                modifyTime:'',
                desc:$scope.selectedNode.desc
            };
            //获取容量
            sendRequest('/group/getResourceSize.action',{groupId:$scope.currentGroupId},function(data){
                $scope.groupInfo.totalSize = (data.totalSize/1048576).toFixed(2)+'G';
                $scope.groupInfo.remainSize = ((data.totalSize - data.resourcesSize)/1048576).toFixed(2)+'G';
            });
            //获取时间（未知接口）

        };
        //“添加”按钮
        $scope.addPermission = function(){
            initusersTree();
            $("#addPermissionOffcanvas").offCanvas('open');
            $("#structure").tabs();//默认打开组织结构列表
            $scope.showStructure();//默认打开组织结构页面

        };
        //“添加”侧边栏的关闭按钮
        $scope.closeAddPermission = function () {
            $("#addPermissionOffcanvas").offCanvas('close');
            //需要添加清除页面数据功能
            $("input[type='checkbox']").each(function () {
                this.checked = false;
            });
            $scope.checkedCount = 0;
            $scope.checkedId = [];
            $scope.checkedFullName = [];

        };
        //“添加”侧边栏中组织结构和所有用户选项卡切换
        $scope.tabSelect = {'DEFAULT':'', 'STRUCTURE':'structure','USERS':'users'};
        var currentTab = $scope.tabSelect.DEFAULT;

        $scope.showStructure = function () {
            $("#structure").tabs();
            currentTab = $scope.tabSelect.STRUCTURE;
        };
        $scope.showUsers = function () {
            $("#users").tabs();
            currentTab = $scope.tabSelect.USERS;
        }
        //判断“组织结构”和“所有用户”哪个处于活跃状态
        $scope.isTabSelected = function (tabSelect) {
            if(currentTab == tabSelect)
                return true;
            return false;
        };
        //初始化组织结构列表
        $scope.usersTree = [];//用于存储树形结构的数组
        var initusersTree = function () {
            sendRequest('/grouper/grouperTree.action',
                {
                    id: 0,
                    type: 'folder',
                    folderOnly: false
                },function (data) {
                    data.members.push({
                        id:-1,
                        text:"所有用户",
                        type:"allUsers"
                    });
                    $scope.usersTree.push({text: "根目录",type:"rootFolder",nodes: data.members});
                });
            console.log("init success");
        };

        $scope.openUsersTree = function (scope) {
            var _thisUserNode = scope.$modelValue;//获取当前选中的值
            if( typeof(_thisUserNode.nodes) != 'undefined' && _thisUserNode.type != 'team' && _thisUserNode.type !='allUsers' ){
                scope.toggle();
                return false;
            }
            if(_thisUserNode.type != 'team' && _thisUserNode.type !='allUsers' && typeof(_thisUserNode.nodes) == 'undefined'){
                sendRequest('/grouper/grouperTree.action',
                    {
                        id:_thisUserNode.id,
                        type: _thisUserNode.type,
                        folderOnly:false,
                        fullName:_thisUserNode.fullName
                    },function (data) {
                        _thisUserNode.nodes = data.members;
                        scope.toggle();
                    });
                return false;
            }
        };
        //添加侧边栏添加按钮功能
        $scope.checkedCount = 0;
        $scope.checkedId =[];//存储待添加分组的id
        $scope.checkedFullName =[];//存储待添加分组的Name
        var updateCheckedValue = function(action,id,fullName){
            if(action == 'add'){
                $scope.checkedId.push(id);
                $scope.checkedFullName.push(fullName);
                $scope.checkedCount ++;
            }
            if(action == 'remove'){
                var idx = $scope.checkedId.indexOf(id);
                $scope.checkedId.splice(idx,1);
                var idName = $scope.checkedFullName.indexOf(fullName);
                $scope.checkedFullName.splice(idName,1);
                $scope.checkedCount --;
            }
            console.log($scope.checkedId);
            console.log($scope.currentGroupId);
            console.log($scope.checkedFullName);
        };
        $scope.updateChecked = function($event, id, fullName){
            var checkbox = $event.target;//取得当前的checkbox
            var action = (checkbox.checked? 'add':'remove');//取得checkbox是否选中的值
            updateCheckedValue(action,id,fullName);//更新数据
        };
        $scope.addPermissionTo = function(){
            //如果没有选择用户
            if($scope.checkedCount < 1){
                $('#noCheckedAlert').modal();
                return;
            }
            //存在选择的用户
            $('#addPermissionToConfirm').modal({
                onConfirm:function () {
                    $rootScope.progressbar.start();
                    sendRequest('/user/joinGroup.action',{
                        // id:$scope.checkedId,
                        type:'god',
                        groupId:$scope.currentGroupId,
                        name:$scope.checkedFullName
                    },function (data) {
                        $rootScope.progressbar.complete();
                        $scope.closeAddPermission();
                        $scope.refreshGridData();
                        console.log("添加成功");
                    }, function (data, code) {
                        //alert(data.detail);
                        $rootScope.progressbar.complete();
                        $rootScope.toastr.error(data.detail || "未知错误");
                    })

                },
                onCancel:function () {
                    $scope.currentModal.modal('close');
                }
            })
        };

        /*//判断文件柜管理中，查看权限和详细信息两个Tab的激活情况
        $scope.isTabActive = function (tabName) {
            if (currentTab == tabName)
                return true;
            return false;
        };*/



    }]);