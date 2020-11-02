angular.module('systemManagementController',[])
    .controller('systemManagementController',['$scope','$rootScope','httpRequest.sendRequest','$state',
        function($scope,$rootScope,sendRequest,$state){
            //init panel height
            $scope.windowHeight = angular.element(window).height();
            angular.element(document.querySelectorAll(".system-info-content")).height($scope.windowHeight - 80);
            angular.element(window).resize(function () {
                $scope.windowHeight = angular.element(window).height();
                angular.element(document.querySelectorAll(".system-info-content")).height($scope.windowHeight - 80);
            });


            $scope.sysTabName = {'DEFAULT': '', 'GLOBALSETTING': 'globalSetting',
                                 'STORAGEICONSET': 'storageIconSet','LOGMANAGEMENT':'logManagement'};//tabName enum
            var currentTab = $scope.sysTabName.GLOBALSETTING;
            $state.go('manager.systemManagement.globalSetting');
            $scope.isTabActive = function (sysTabName) {
                if (currentTab == sysTabName)
                    return true;
                return false;
            };
            $scope.showGlobalSetting = function () {
                $state.go('manager.systemManagement.globalSetting');
                currentTab = $scope.sysTabName.GLOBALSETTING;
            };
            $scope.showStorageIconSet = function () {
                $state.go('manager.systemManagement.storageIconSet');
                currentTab = $scope.sysTabName.STORAGEICONSET;
            };
            $scope.showLogManagement = function () {
                $state.go('manager.systemManagement.logManagement');
                currentTab = $scope.sysTabName.LOGMANAGEMENT;
            };

    }]);