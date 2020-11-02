/**
 * Created by lfn on 2016/3/3.
 */
angular.module("advancedSearchController", ["am.modal"])
  .controller('advancedSearchController', ["$scope", "$rootScope", 'items', '$modalInstance', "httpRequest.sendRequest", function ($scope, $rootScope, items, $modalInstance, sendRequest) {
    $scope.data = {};
    $scope.data.searchText = "";
    $scope.data.range = "全部";
    $scope.data.creator = "";
    $scope.data.fileType = "";
    $scope.categoryId = 0;
    $(".startTime").datepicker("setValue", new Date(0));
    $(".endTime").datepicker("setValue", new Date());
    $scope.advancedSearchBtn = function () {
      if ($scope.data.fileType) $scope.data.searchText = $scope.data.searchText + $scope.data.fileType;
      items.scope.searchBtn($scope.data.searchText, $scope.categoryId, $scope.data.creator, new Date($(".endTime").val()).getTime(), new Date($(".startTime").val()).getTime());
      $modalInstance.close();
    };
    $scope.cancel = function () {
      $modalInstance.close();
    };

    $scope.data1 = [];
    $scope.visible = function (item) {
      return !($scope.query && $scope.query.length > 0
      && item.displayName.indexOf($scope.query) == -1);
    };
    $scope.findNodes = function () {

    };

    /**顶层树节点**/
    var paramsObj = { containPersonGroup: true, containAblumCategory: false };
    sendRequest("/group/trees.action", paramsObj,
      function (data) {
        var rootObject = [{ id: $rootScope.personalGroupId,displayName:'个人资源库' }];
        $scope.data1 = [...rootObject, ...data.children];
        console.log($scope.data1);
      });
    $scope.openTree = function (scope, $event) {
      $event.stopPropagation();
      var nodeData = scope.$modelValue;
      if (nodeData.nodes != undefined) {
        scope.toggle(scope);
        return false;
      }
      nodeData.nodes = [];
      if (nodeData.type === 'group') {
        return false;
      }
      sendRequest("/group/trees.action",
        {
          containPersonGroup: true,
          containAblumCategory: false,
          categoryId: nodeData.id
        }, function (data) {
          nodeData.nodes = data.children;
          scope.toggle();
        });
    };
    $scope.selectTree = function (x) {
      $scope.data.range = x.$modelValue.displayName;
      $scope.categoryId = x.$modelValue.id;
    };
  }]);