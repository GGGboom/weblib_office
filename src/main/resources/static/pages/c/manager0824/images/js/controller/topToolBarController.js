angular.module('topToolBarController', [])
    .controller('topToolBarController', ['$scope', '$rootScope', 'httpRequest.sendRequest','$state', function ($scope, $rootScope, sendRequest,$state) {
        $scope.manageUser = function(){
            $state.go('manager.userManagement');
        };
        $scope.manageStorage = function(){
            $state.go('manager.storageManagement');
        };
        $scope.manageApplication = function(){
            $state.go('manager.applicationManagement');
        };
        $scope.manageSystem = function(){
            $state.go('manager.systemManagement');
        };
        $scope.manageAddressbook = function () {
            $state.go('manager.addressbookManagement');
        };
        $scope.isActive = function(state){

            if(new RegExp(state).test($state.$current.name)){
                // return{color:'#ed7700'};橙色
                return {color:'#3bb4f2'};//蓝色
            }
            return {color:'#000000'};
        }

    }]);