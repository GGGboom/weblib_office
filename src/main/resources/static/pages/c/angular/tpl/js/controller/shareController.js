/**
 * Created by lfn on 2015/10/29.
 */
angular.module("shareController", ["ngGrid", "am.modal", "ui.tree"])
    .controller('shareController', ["$scope","$rootScope", '$modalInstance', 'items', "httpRequest.sendRequest", function ($scope,$rootScope, $modalInstance, items, sendRequest) {
        $scope.selectAccounts = [];
        $scope.treeShare = [];
        $scope.totalItems = 0;
        $scope.addSelections = [];
        $scope.deleteSelections = [];
        $scope.deleteTreeSelections = [];
        $scope.treeSelected = [];
        $scope.groupdata = [];
        $scope.groupRang = [];
        $scope.inputName = "";
        $scope.inputGroup="";
        $scope.searchingUsers=false;
        $scope.range="全部";
        $scope.pagingOptions = {
            pageSize: 30,
            currentPage: 1,
            start: 0,
            totalPage: 0
        };
        $scope.gridOption1 = {
            data: "accounts",
            multiSelect: true,
            enableSorting: true,
            //         showSelectionCheckbox: true,
            selectWithCheckboxOnly: true,
            rowHeight: 30,
            headerRowHeight: 30,
            selectedItems: $scope.addSelections,
            enablePaging: true,
            showFooter: false,
            totalServerItems: 'totalServerItems',
            pagingOptions: $scope.pagingOptions,
            rowTemplate: '<div ng-mouseover="rowOver()" ng-mouseleave="rowLeave()" ng-dblclick="showit(row.entity)" ng-repeat="col in renderedColumns" ng-class="col.colIndex()" class="ngCell {{col.cellClass}}" ng-cell></div>',
            columnDefs: [
                {
                    field: "account", displayName: '账号',
                    cellTemplate: '<div class="ngCellText" style="padding-left:10px;"><span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
                },
                {
                    field: "name", displayName: '姓名',
                    cellTemplate: '<div class="ngCellText" style="padding-left:10px;"><span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
                }

            ]
        };
        $scope.gridOption2 = {
            data: "selectAccounts",
            multiSelect: true,
            //         showSelectionCheckbox: true,
            selectWithCheckboxOnly: true,
            rowHeight: 30,
            headerRowHeight: 30,
            enableSorting: true,
            selectedItems: $scope.deleteSelections,
            enablePaging: true,
            showFooter: false,
            totalServerItems: 'totalServerItems',
            pagingOptions: $scope.pagingOptions,
            rowTemplate: '<div ng-mouseover="rowOver()" ng-mouseleave="rowLeave()"  ng-repeat="col in renderedColumns" ng-class="col.colIndex()" class="ngCell {{col.cellClass}}" ng-cell></div>',
            columnDefs: [
                {
                    field: "account", displayName: '账号',
                    cellTemplate: '<div class="ngCellText" style="padding-left:10px;"><span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
                },
                {
                    field: "name", displayName: '姓名',
                    cellTemplate: '<div class="ngCellText" style="padding-left:10px;"><span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
                }

            ]
        };
        $scope.gridOption3 = {
            data: "treeShare",
            multiSelect: true,
            //         showSelectionCheckbox: true,
            selectWithCheckboxOnly: true,
            rowHeight: 30,
            headerRowHeight: 30,
            enableSorting: true,
            selectedItems: $scope.deleteSelections,
            enablePaging: true,
            showFooter: false,
            totalServerItems: 'totalServerItems',
            pagingOptions: $scope.pagingOptions,
            rowTemplate: '<div ng-mouseover="rowOver()" ng-mouseleave="rowLeave()"  ng-repeat="col in renderedColumns" ng-class="col.colIndex()" class="ngCell {{col.cellClass}}" ng-cell></div>',
            columnDefs: [
                {
                    field: "text", displayName: '已选用户组',
                    cellTemplate: '<div class="ngCellText" style="padding-left:10px;"><span ng-cell-text>{{row.getProperty(col.field)}}</span></div>'
                }

            ]
        };
        $scope.addbtn = function () {
            var t = $scope.selectAccounts.length;
            for (var j = 0; j < $scope.gridOption1.selectedItems.length; j++) {
                if (t > 0) {
                    for (var i = 0; i < t; i++) {
                        if ($scope.selectAccounts[i] == $scope.gridOption1.selectedItems[j]) {
                            break;
                        }
                        else if (i == t - 1) {
                            $scope.selectAccounts.push($scope.gridOption1.selectedItems[j]);
                        }
                    }
                } else $scope.selectAccounts.push($scope.gridOption1.selectedItems[j]);

            }
        };
        $scope.deletebtn = function () {
            for (var j = 0; j < $scope.gridOption2.selectedItems.length; j++) {
                for (var i = 0; i < $scope.selectAccounts.length; i++) {
                    if ($scope.selectAccounts[i] == $scope.gridOption2.selectedItems[j]) {
                        $scope.selectAccounts.splice(i, 1);
                        break;
                    }
                }
            }
            $scope.gridOption2.selectedItems.splice(0, $scope.gridOption2.selectedItems.length);
        };
        $scope.cancel = function () {
            $modalInstance.close();//或者$scope.$close()
        };
        $scope.cleanusers=function(){
            $scope.selectAccounts.splice(0, $scope.selectAccounts.length);
            $scope.gridOption2.selectedItems.splice(0, $scope.gridOption2.selectedItems.length);
        };
        $scope.sharing = function () {
            if ($scope.selectAccounts.length > 0 || $scope.treeShare.length > 0) {
                var name = "";
                var memberIds = [];
                var i = $scope.selectAccounts.length;
                while (i--) {
                    name += $scope.selectAccounts[i].account + ";"
                }
                name = name.substring(0, name.length - 1);
                for (var j = 0; j < $scope.treeShare.length; j++) {
                    memberIds.push($scope.treeShare[j].id)
                }
                // $rootScope.progressbar.start();
                sendRequest("/group/shareResourceToMember.action",
                    {
                        resourceId: items.resourceId,
                        memberName: name,
                        memberIds: memberIds,
                        groupId: items.groupId,
                        desc: document.getElementById("postscript").value
                    }, function (data) {
                        // $rootScope.progressbar.complete();
                        if(data.type=="success"){
                            toastr["success"]("分享成功！");
                        }else   toastr["error"]("分享失败！");
                    });
                $modalInstance.close();
            }
            else  toastr["warning"]("请添加共享对象！");
        };
        $scope.showit = function (x) {
            var i = $scope.selectAccounts.length;
            while (i--) {
                if ($scope.selectAccounts[i] == x) {
                    toastr["info"]("该用户已添加！");
                    return
                }
            }
            $scope.selectAccounts.push(x);
        };

        $scope.deleteIt = function (id) {
            $scope.selectAccounts.splice($scope.selectAccounts.indexOf(id), 1);
        };
        $scope.searchUser=function(x){
            if (x != "") {
                $scope.searchingUsers=true;
                $scope.searchName=x;
                $scope.pagingOptions.currentPage = 1;
                $scope.getUsers($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize,  $scope.searchName);
            }else{
                $scope.searchingUsers=false;
                $scope.pagingOptions.currentPage = 1;
                $scope.getAccounts($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
            }
        };
        $scope.searchGroup=function(x){
            if (x != "") {
                sendRequest("/grouper/searchTeam.action",
                    {name: x}, function (data) {
                     $scope.groupdata=data.members;
                    });
            }else{
                sendRequest("/grouper/grouperTree.action", {id: 0, type: "folder", folderOnly: false},
                    function (data) {
                        $scope.groupdata=data.members;
                    });
            }
        };
        $scope.getUsers = function (pageSize, page,x) {
            sendRequest("/user/advancedSearch.action",
                {limit: pageSize, start: page,account:x}, function (data) {
                    $scope.accounts = data.accounts;
                    $scope.totalItems = data.totalCount;
                    $scope.pagingOptions.totalPage = Math.ceil($scope.totalItems / $scope.pagingOptions.pageSize);
                });
        };
        $scope.getAccounts = function (pageSize, page) {
            sendRequest("/user/getAccounts.action",
                {limit: pageSize, start: page}, function (data) {
                    $scope.accounts = data.accounts;
                    $scope.totalItems = data.totalCount;
                    $scope.pagingOptions.totalPage = Math.ceil($scope.totalItems / $scope.pagingOptions.pageSize);
                });
        };
        $scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
            if (newVal !== oldVal && $scope.pagingOptions.currentPage > 0) {
                if($scope.searchingUsers){
                    $scope.getUsers($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize,  $scope.searchName);
                }
                else $scope.getAccounts($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
            }
        });

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
        $scope.getAccounts($scope.pagingOptions.pageSize, 0);


        $scope.visible = function (item) {
            return !($scope.query && $scope.query.length > 0
            && item.displayName.indexOf($scope.query) == -1);
        };
        $scope.findNodes = function () {

        };

        /**顶层树节点**/
        var paramsObj = {id: 0, type: "folder", folderOnly: false};
        sendRequest("/grouper/grouperTree.action", paramsObj,
            function (data) {
                $scope.groupdata=data.members;
                $scope.groupRang=data.members;
            });
        $scope.openTree = function (scope, $event) {
            $event.stopPropagation();
            var nodeData = scope.$modelValue;
            if (nodeData.nodes != undefined) {
                    scope.toggle(scope);
                return false;
            }
            nodeData.nodes = [];
            if (nodeData.type == "team") {
            }
            else {
                sendRequest("/grouper/grouperTree.action",
                    {id: nodeData.id, type: nodeData.type, folderOnly: false}, function (data) {
                        if (data.members.length > 0) {
                            for (var i = 0; i < data.members.length; i++) {
                                nodeData.nodes.push(data.members[i]);
                            }
                            scope.toggle(scope);
                        }
                    });
            }
        };
        $scope.selectTree = function (scope) {
            if($scope.treeSelected[0]!= scope.node) {
                scope.node.isSelected = true;
                if ($scope.treeSelected.length > 0) {
                    $scope.treeSelected[0].isSelected = false;
                    $scope.treeSelected.splice(0, 1);
                    $scope.treeSelected.push(scope.node);
                }
                else {
                    $scope.treeSelected.push(scope.node);
                }
            }
        };
        $scope.selectRange=function(scope){
            var nodeData = scope.$modelValue;
            if (nodeData.nodes != undefined) {
                    scope.toggle(scope);
                return false;
            }
            nodeData.nodes = [];
            if (nodeData.type == "team") {
                $scope.range= scope.$modelValue.text;
                $("#shareRang").hide();
                sendRequest("/grouper/grouperTree.action",
                    {id: nodeData.id, type: nodeData.type, folderOnly: false}, function (data) {
                            for (var i = 0; i < data.members.length; i++) {
                         data.members[i].account= data.members[i].text;
                                data.members[i].name=data.members[i].fullName;
                            }
                        $scope.accounts = data.members;
                    });
            }
            else {
                sendRequest("/grouper/grouperTree.action",
                    {id: nodeData.id, type: nodeData.type, folderOnly: false}, function (data) {
                        if (data.members.length > 0) {
                            for (var i = 0; i < data.members.length; i++) {
                                nodeData.nodes.push(data.members[i]);
                            }
                            scope.toggle(scope);
                        }
                    });
            }
        };
        $scope.setIcon=function(type,collapsed){
            if(type=="team") return 'am-icon-users';
            else if(collapsed) return 'am-icon-caret-down';
            else return 'am-icon-caret-right'
        };
        $scope.resetRange=function(){
            $scope.range= "全部";
            $scope.searchingUsers=false;
            $scope.pagingOptions.currentPage = 1;
            $scope.getAccounts($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
        };
        $scope.treeAddBtn = function () {
            if($scope.treeSelected[0].type=="team"){
                if ($scope.treeShare.length > 0) {
                    for (var i = 0; i <$scope.treeShare.length ; i++) {
                        if ($scope.treeShare[i].id == $scope.treeSelected[0].id) {
                            break;
                        }
                        else if (i == $scope.treeShare.length - 1) {
                            $scope.treeShare.push($scope.treeSelected[0]);
                        }
                    }
                } else $scope.treeShare.push($scope.treeSelected[0]);
            }else  toastr["warning"]("不支持共享到组织！");


        };
        $scope.treeDeleteBtn = function () {
            for (var j = 0; j < $scope.gridOption3.selectedItems.length; j++) {
                for (var i = 0; i < $scope.treeShare.length; i++) {
                    if ($scope.treeShare[i] == $scope.gridOption3.selectedItems[j]) {
                        $scope.treeShare.splice(i, 1);
                        break;
                    }
                }
            }
            $scope.gridOption3.selectedItems.splice(0, $scope.gridOption3.selectedItems.length);
        }
    }]);
