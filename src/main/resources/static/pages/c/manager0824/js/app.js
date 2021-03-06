angular.module('managerApp', [
        'ui.router',
        'global',
        'keepAlive',
        'ui.tree',
        'ngDraggable',
        'ngProgress',
        'ui.grid',
        'ngGrid',
        'managerModule',
        'topToolBarController',
        'userManagementController',
        'storageManagementController',
        'applicationManagementController',
        'systemManagementController',
        'loginController',
        'groupPermissionController',
        'addressbookManagementController',
        'domainManagementController'
    ])
    .run(['$rootScope', '$state', '$stateParams', 'ngProgressFactory', function ($rootScope, $state, $stateParams, ngProgressFactory) {
        $rootScope.$state = $state;
        $rootScope.$stateParams = $stateParams;
        $rootScope.path = "http://202.38.254.204/weblib";
        $rootScope.listOptions = {
            parentId: 0
        };
        $rootScope.toastr = require("toastr");
        $rootScope.toastr.options = {
            "closeButton": false,
            "debug": false,
            "newestOnTop": false,
            "progressBar": false,
            "positionClass": "toast-top-right",
            "preventDuplicates": false,
            "onclick": null,
            "showDuration": "300",
            "hideDuration": "1000",
            "timeOut": "1500",
            "extendedTimeOut": "1000",
            "showEasing": "swing",
            "hideEasing": "linear",
            "showMethod": "fadeIn",
            "hideMethod": "fadeOut"
        };
        $rootScope.status = {};
        $rootScope.progressbar = ngProgressFactory.createInstance();
        $rootScope.progressbar.setColor("dodgerblue");
    }])
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise('/manager');
        $stateProvider.state('manager', {
            url: '/manager',
            views: {
                "manager": {
                    templateUrl: 'tpl/manager.html',
                    controller: 'managerController'
                },
                "topToolBar@manager": {
                    templateUrl: 'tpl/topToolBar.html',
                    controller: 'topToolBarController'
                },
                "content@manager": {
                    templateUrl: 'tpl/content.html'

                }
            }
        }).state('manager.userManagement', {
            url: '/usermanagement',
            templateUrl:'tpl/userManagement.html',
            controller:'userManagementController'
        }).state('manager.userManagement.userDetailInfo',{
            templateUrl:'tpl/userManagementTpls/userDetailInfo.html'
        }).state('manager.userManagement.editUserDetailInfo',{
            templateUrl:'tpl/userManagementTpls/editUserDetailInfo.html'
        }).state('manager.userManagement.addUser',{
            templateUrl:'tpl/userManagementTpls/addUser.html'
        }).state('manager.storageManagement', {
            url: '/storagemanagement',
            templateUrl:'tpl/storageManagement.html',
            controller:'storageManagementController'
        }).state('manager.storageManagement.permission',{
            templateUrl:'tpl/storageManagementTpls/permission.html',
            controller:'groupPermissionController'
        }).state('manager.applicationManagement',{
            url:'/applicationmanagement',
            templateUrl:'tpl/applicationManagement.html',
            controller:'applicationManagementController'
        }).state('manager.systemManagement',{
            url:'/systemmanagement',
            templateUrl:'tpl/systemManagement.html',
            controller:'systemManagementController' 
        }).state('manager.systemManagement.globalSetting',{
            templateUrl:'tpl/systemManagementTpls/globalSetting.html'
        }).state('manager.systemManagement.editGlobalSetting',{
            templateUrl:'tpl/systemManagementTpls/editGlobalSetting.html'
        }).state('manager.systemManagement.storageIconSet',{
            templateUrl:'tpl/systemManagementTpls/storageIconSet.html'
        }).state('manager.systemManagement.loginLog',{
            templateUrl:'tpl/systemManagementTpls/loginLog.html'
        }).state('manager.systemManagement.uploadLog',{
            templateUrl:'tpl/systemManagementTpls/uploadLog.html'
        }).state('manager.systemManagement.downloadLog',{
            templateUrl:'tpl/systemManagementTpls/downloadLog.html'
        }).state('manager.systemManagement.operateLog',{
            templateUrl:'tpl/systemManagementTpls/operateLog.html'
        }).state('manager.systemManagement.errorLog',{
            templateUrl:'tpl/systemManagementTpls/errorLog.html'
        }).state('manager.addressbookManagement',{
            url:'/addressbookManagement',
            templateUrl:'tpl/addressbookManagement.html',
            controller:'addressbookManagementController'
        }).state('manager.domainManagement', {
            url: '/domainmanagement',
            templateUrl:'tpl/domainManagement.html',
            controller:'domainManagementController'
        }).state('manager.domainManagement.domainDetailInfo',{
            templateUrl:'tpl/domainManagementTpls/domainDetailInfo.html'
        }).state('manager.domainManagement.noDomainDetailInfo',{
            templateUrl:'tpl/domainManagementTpls/noDomainDetailInfo.html'
        }).state('manager.domainManagement.editDomainDetailInfo',{
            templateUrl:'tpl/domainManagementTpls/editDomainDetailInfo.html'
        }).state('manager.domainManagement.createDomainOnFolder',{
            templateUrl:'tpl/domainManagementTpls/createDomainOnFolder.html'
        }).state('manager.domainManagement.createDomainWithNewFolder',{
            templateUrl:'tpl/domainManagementTpls/createDomainWithNewFolder.html'
        }).state('login',{
            controller:'loginController'
        }).state('manager.loginAnalyse',{
            url:'/loginAnalyse',
            templateUrl:'tpl/loginAnalyse.html'
        })
    })
    .controller('appController', ['$scope', '$rootScope', '$state', 'httpRequest.sendRequest', function ($scope, $rootScope, $state, sendRequest) {
        /**获取登录状态及个人信息**/
        sendRequest("/user/status.action", {}, function (data) {
            if (data.status == "login") {
                // console.log("status data"+data);
                $rootScope.status = data;
            } else {
                window.location.href="http://" + window.location.host + "/2016newmanager/app/index.html#/login";//如果没登陆则跳转到登录页
                // $state.go("login");

            }
        });
        $rootScope.logout = function () {
            sendRequest("/login/logout.action", {}, function (data) {
                window.location.href = "http://" + window.location.host + "/2016newmanager/app/index.html#/login";
            });
        }
        $rootScope.goClient = function(){
            window.location.href = "http://" + window.location.host + "/2016newmanager/app/index.html#/main";
        }
        
}]);