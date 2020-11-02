angular.module('applicationManagementController',[])
    .controller('applicationManagementController',['$scope','$rootScope','httpRequest.sendRequest',function($scope,$rootScope,sendRequest){
        $scope.windowHeight = angular.element(window).height();
        angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
        angular.element(window).resize(function () {
            $scope.windowHeight = angular.element(window).height();
            angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
        });
        var initApplicationList = function () {
            sendRequest('/group/getApplication.action',{},function (data) {
                $scope.applications = data.applications;
                $scope.totalCount = data.totalCount;
                console.log(data);
            },function (data,code) {
                if(code =="300"){
                    $rootScope.alert(data.detail);
                }else{
                    $rootScope.toastr.error(data.detail || '未知错误');
                }

            });
        };
        initApplicationList();
    }]);