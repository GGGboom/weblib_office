/**
 * Created by lfn on 2016/3/18.
 */
angular.module("slideController", ["am.modal"])
    .controller('slideController', ["$scope", "$rootScope", "$timeout", '$modalInstance', 'items', "httpRequest.sendRequest", function ($scope, $rootScope, $timeout, $modalInstance, items, sendRequest) {
        $scope.imageid = items.imageId;
        $scope.id = items.id;
        $scope.imageurl = $rootScope.path;
        $scope.index = $scope.imageid.indexOf($scope.id);
        $timeout(
            function () {
                $('.am-slider').flexslider({controlNav: false, slideshow: false, startAt: $scope.index});
            },
            200
        );
    }]);