angular.module('managerModule', ['am.modal', 'httpRequest'])
    .controller('managerController', ['$scope', 'keepAlive', 'modalWindow', 'modalConfirm', '$state', 'httpRequest.sendRequest', function ($scope, keepAlive, modalWindow, modalConfirm, $state,sendRequest) {

        //keepAlive
        keepAlive.start();

        // check status
        // sendRequest('/user/status.action', {}, function (data) {
        //     if (data.status == 'login') {
        //         //默认跳转到用户管理state
        //         $state.go('manager.userManagement');
        //     } else {
        //         $state.go('login');
        //     }
        // });

        //判断访问路径，默认跳转到用户管理state
        if($state.is('manager'))
            $state.go('manager.userManagement');



    }]);