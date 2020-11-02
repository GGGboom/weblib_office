/**
 * Created by lfn on 2015/12/22.
 */
angular.module("createLinkController", ["am.modal"])
    .controller('createLinkController', ["$scope", "$rootScope", '$modalInstance', 'items', "httpRequest.sendRequest", function ($scope, $rootScope, $modalInstance, items, sendRequest) {
        $scope.data = {};
        $scope.data.linkname = "";
        $scope.data.url = "";
        $scope.data.option = "http://";
        $scope.parentId = items.parentId;
        if ($scope.parentId < 0) {
            $scope.parentId = 0;
        }
        $scope.createLinkConfirm = function () {
            console.log($scope.data.linkname + $scope.data.url + items.groupId + $scope.parentId);
            if ($scope.data.linkname && $scope.data.url && items.groupId && $scope.parentId >= 0) {
                // $rootScope.progressbar.start();
                sendRequest("/group/createLinkDir.action", {
                    groupId: items.groupId,
                    name: $scope.data.linkname,
                    parentId: $scope.parentId,
                    url: $scope.data.option + $scope.data.url
                }, function (data) {
                    // $rootScope.progressbar.complete();
                    if (data.id) {
                        items.scope.update();
                        toastr["success"]("新建成功！");
                    } else {
                        toastr["error"]("新建失败！");
                    }
                });
            }
            else {
                toastr["warning"]("请填写必要信息！");
            }
        };
    }]);