/**
 * Created by lfn on 2015/9/10.
 */
angular.module("TopToolController", [])
    .controller('TopToolController', ["$scope", "$rootScope", "httpRequest.sendRequest", "modalWindow", function ($scope, $rootScope, sendRequest, modalWindow) {
        //var amazeui = require("../../lib/amaze/js/amazeui.js");
        amazeui();
        //require("amazeui");
        $scope.searchText = "";
        $scope.addDir=$rootScope.AddDir;
        $scope.oldManagerUrl = Config.sitePath + '/pages/c/manager/';//旧版管理端地址
        $scope.newManagerUrl = Config.sitePath + '/pages/c/manager0824/'//新版管理端地址
        $scope.getAccount = function () {
            sendRequest("/user/getAccount.action", {}, function (data) {
                $scope.userdata = data;
                $scope.username = data.name;
            });
        };
        $scope.modifyAccount = function () {
            modalWindow.open({
                templateUrl: 'tpl/modifyAccount.html',
                controller: 'modifyAccountController',
                title: '个人信息设置',
                width: 650,
                height: 490,
                resolve: {
                    items: function () {
                        return $scope;
                    }
                }
            });
        };
        $scope.getAccount();
    }]);