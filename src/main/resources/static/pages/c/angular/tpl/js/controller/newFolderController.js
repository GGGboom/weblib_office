/**
 * Created by lfn on 2015/12/21.
 */
angular.module("newFolderController", ["am.modal"])
    .controller('newFolderController', ["$scope", "$rootScope",'items', '$modalInstance', "httpRequest.sendRequest", function ($scope, $rootScope,items, $modalInstance, sendRequest) {
        $scope.data={};
        $scope.data.folderName = "";
        $scope.parentId=items.parentId;
        if( $scope.parentId<0) $scope.parentId=0;
        $scope.newFolderConfirm = function () {
            if($scope.data.folderName&&items.groupId&&$scope.parentId>=0){
                // $rootScope.progressbar.start();
                sendRequest("/group/createDir.action", {
                    groupId: items.groupId,
                    parentId: $scope.parentId,
                    name:  $scope.data.folderName
                }, function (data) {
                    // $rootScope.progressbar.complete();
                    if(data.id){
                        items.scope.update();
                        toastr["success"]("新建成功！");
                    }else   toastr["error"]("新建失败！");
                });
            }
        }
    }]);