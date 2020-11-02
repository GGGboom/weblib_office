/**
 * Created by lfn on 2015/11/25.
 */
angular.module("moveCopyController", [, "am.modal", "ui.tree"])
    .controller('moveCopyController', ["$scope", "$rootScope", '$modalInstance', 'items', "httpRequest.sendRequest", "modalConfirm", function ($scope, $rootScope, $modalInstance, items, sendRequest, modalConfirm) {
        $scope.data = [];
        $scope.data1 = [];
        $rootScope.selectnode = {selected: false};
        $scope.ishidden = false;
        $scope.btnText = items.title;
        $rootScope.moveCopyId = items.resourceId;
        $rootScope.MCModal = $modalInstance;
        $rootScope.MCTitle = items.title;
        $scope.cancel = function () {
            $rootScope.MCModal.close();//或者$scope.$close()
        };
        $scope.moveAndCopy = function () {
            if ($rootScope.selectnode.text) {
                var moveCopyModal = modalConfirm.confirm({
                    title: '是否将这'+ $rootScope.moveCopyId.length+'个资源' +  $rootScope.MCTitle+ '到: '+ $rootScope.selectnode.text+" 文件夹中 ？",
                    onConfirm: function (data) {
                        var interfaces = '/group/copyResource.action';
                        if ($rootScope.MCTitle == "移动") interfaces = '/group/moveResource.action';
                        // $rootScope.progressbar.start();
                        sendRequest(interfaces,
                            {
                                groupId: $rootScope.MCgroupId,
                                parentId: $rootScope.selectnode.id,
                                id: $rootScope.moveCopyId
                            }, function (data) {
                                // $rootScope.progressbar.complete();
                                if(data.type=="success"){
                                    items.scope.update();
                                    toastr["success"]($rootScope.MCTitle+"成功！");
                                }else   toastr["error"]($rootScope.MCTitle+"失败！");
                            });
                        $rootScope.MCModal.close();
                    },
                    onCancel: function (data) {

                    }
                });
            }
        };
        $scope.visible = function (item) {
            return !($scope.query && $scope.query.length > 0
            && item.displayName.indexOf($scope.query) == -1);
        };
        $scope.findNodes = function () {

        };
        $scope.moveCopyBack = function () {
            $scope.ishidden = false;
        };
        /**顶层树节点**/
        var paramsObj = {containPersonGroup: false, containAblumCategory: false};
        sendRequest("/group/trees.action", paramsObj,
            function (data) {
                $scope.data.push({id: $rootScope.status.personGroupId, displayName: "个人资源库", type: "group"});
                for (var i = 0; i < data.children.length; i++) {
                    $scope.data.push(data.children[i]);
                }
            });
        $scope.openTree = function (scope) {
            var nodeData = scope.$modelValue;
            if (nodeData.nodes != undefined) {
                scope.toggle();
                return false;
            }
            if (nodeData.type == "group") {
                $scope.data1 = [];
                $scope.ishidden = true;
                $rootScope.MCgroupId = nodeData.id;
                sendRequest("/group/getResources.action",
                    {type: "tree", parentId: -nodeData.id}, function (data) {
                        for (var i = 0; i < data.length; i++) {
                            $scope.data1.push(data[i]);
                        }
                    });
                $rootScope.selectnode = {id: -nodeData.id, text: nodeData.displayName};
                $scope.group = nodeData.displayName;
            }
            else {
                nodeData.nodes = [];
                sendRequest("/group/trees.action",
                    {containPersonGroup: false, containAblumCategory: false, categoryId: nodeData.id}, function (data) {
                        for (var i = 0; i < data.children.length; i++) {
                            nodeData.nodes.push(data.children[i]);
                        }
                        scope.toggle();
                    });
            }
        };
        $scope.openGroup = function (scope) {
            var groupData = scope.$modelValue;
            $rootScope.selectnode.selected = false;
            groupData.selected = true;
            $rootScope.selectnode = groupData;
            if (groupData.nodes != undefined) {
                scope.toggle();
                return false;
            }
            groupData.nodes = [];
            sendRequest("/group/getResources.action",
                {type: "tree", parentId: groupData.id}, function (data) {
                    for (var i = 0; i < data.length; i++) {
                        groupData.nodes.push(data[i]);
                    }
                    scope.toggle();
                });

        };
    }])
    .filter('text', function () {
        return function (displayName) {
            displayName = displayName.replace(/(&nbsp;)/g, " ");
            if(displayName.length>30) displayName=displayName.substring(0,30)+"......";
            return displayName;
        }
    });