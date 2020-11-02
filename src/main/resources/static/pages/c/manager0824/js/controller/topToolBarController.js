angular.module('topToolBarController', [])
    .controller('topToolBarController', ['$scope', '$rootScope', 'httpRequest.sendRequest','$state', function ($scope, $rootScope, sendRequest,$state) {
        $scope.imgUser = 'usermanage_select.svg';
        $scope.imgAddress = 'addressmanage.svg';
        $scope.imgStorage = 'storagemanage.svg';
        $scope.imgApplication = 'applicationmanage.svg';
        $scope.imgSystem = 'systemmanage.svg';
        $scope.imgDomain = 'domainmanage.svg';//后期更改
        $scope.manageUser = function(){
            $scope.imgUser = 'usermanage_select.svg';
            $scope.imgStorage = 'storagemanage.svg';
            $scope.imgApplication = 'applicationmanage.svg';
            $scope.imgSystem = 'systemmanage.svg';
            $scope.imgAddress = 'addressmanage.svg';
            $scope.imgDomain = 'domainmanage.svg';
            $state.go('manager.userManagement');
        };
        $scope.manageStorage = function(){
            $scope.imgUser = 'usermanage.svg';
            $scope.imgStorage = 'storagemanage_select.svg';
            $scope.imgApplication = 'applicationmanage.svg';
            $scope.imgSystem = 'systemmanage.svg';
            $scope.imgAddress = 'addressmanage.svg';
            $scope.imgDomain = 'domainmanage.svg';
            $state.go('manager.storageManagement');
        };
        $scope.manageApplication = function(){
            $scope.imgUser = 'usermanage.svg';
            $scope.imgStorage = 'storagemanage.svg';
            $scope.imgApplication = 'applicationmanage_select.svg';
            $scope.imgSystem = 'systemmanage.svg';
            $scope.imgAddress = 'addressmanage.svg';
            $scope.imgDomain = 'domainmanage.svg';
            $state.go('manager.applicationManagement');
        };
        $scope.manageSystem = function(){
            $scope.imgUser = 'usermanage.svg';
            $scope.imgStorage = 'storagemanage.svg';
            $scope.imgApplication = 'applicationmanage.svg';
            $scope.imgSystem = 'systemmanage_select.svg';
            $scope.imgAddress = 'addressmanage.svg';
            $scope.imgDomain = 'domainmanage.svg';
            $state.go('manager.systemManagement');
        };
        $scope.manageAddressbook = function () {
            $scope.imgUser = 'usermanage.svg';
            $scope.imgStorage = 'storagemanage.svg';
            $scope.imgApplication = 'applicationmanage.svg';
            $scope.imgSystem = 'systemmanage.svg';
            $scope.imgAddress = 'addressmanage_select.svg';
            $scope.imgDomain = 'domainmanage.svg';
            $state.go('manager.addressbookManagement');
        };
        $scope.manageDomain = function () {
            $scope.imgUser = 'usermanage.svg';
            $scope.imgStorage = 'storagemanage.svg';
            $scope.imgApplication = 'applicationmanage.svg';
            $scope.imgSystem = 'systemmanage.svg';
            $scope.imgAddress = 'addressmanage.svg';
            $scope.imgDomain = 'domainmanage_select.svg';
            $state.go('manager.domainManagement');
        };
        /*$scope.isActive = function(state){
            if(new RegExp(state).test($state.$current.name)){
                return{color:'#ed7700'};橙色
                // return {color:'#3bb4f2'};//蓝色
            }
            return {color:'#000000'};
        }*/
        $scope.loginAnalyse = function () {
            $state.go('manager.loginAnalyse');
        }

    }]);