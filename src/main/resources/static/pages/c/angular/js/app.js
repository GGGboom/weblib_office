/**
 * Created by dcampus2011 on 15/8/21.
 */
// var toastr = require("toastr");
angular
  .module('WebLibApp', [
    'ui.router',
    'global',
    'LoginModule',
    'MainModule',
    'keepAlive',
    'ui.tree',
    'sidebarController',
    'PersonalController',
    'TopToolController',
    'shareToMeController',
    'shareToMeDetailsController',
    'myshareController',
    'shareController',
    'fileChainController',
    'moveCopyController',
    'emailController',
    'renameController',
    'createLinkController',
    'modifyAccountController',
    'newFolderController',
    'advancedSearchController',
    'slideController',
    'ngProgress'
  ])
  .run(['$rootScope', '$state', '$stateParams', 'ngProgressFactory', 'httpRequest.sendRequest',
    function ($rootScope, $state, $stateParams, ngProgressFactory, sendRequest) {

      /**
       * @desc 用来判断是否登录，检查权限是否匹配，否则跳回登录页
       */
      $rootScope.$on('$stateChangeStart', function (event, next, nextParams, fromState) {


        // /**获取登录状态及个人信息**/
        // sendRequest("/user/status.action", {}, function (data) {
        //   if (data.status == 'login') {
        //     document.body.style.overflow = 'auto';
        //     $rootScope.status = data;
        //     localStorage.isLogin = true;
        //     $rootScope.personalGroupId = data.personGroupId;
        //     $state.go('main');
        //   } else {
        //     if (next.name !== 'login') {
        //       $state.go('login');
        //       document.body.style.overflow = 'hidden';
        //     }
        //     //window.location.href="http://localhost/weblib/app/";//如果没登陆则跳转到登录页
        //   }
        // });

        // if (localStorage.isLogin !== 'true') {//未登录
        //   if (next.name !== 'login') {
        //     event.preventDefault();
        //     // sendRequest("/login/logout.action");//保证绝对退出，所以这里再退出一次
        //     $state.go('login');
        //   } else {
        //     document.body.style.oveflow = 'auto';
        //     /**获取登录状态及个人信息**/
        //     sendRequest("/user/status.action", {}, function (data) {
        //       if (data.status == "login") {
        //         $rootScope.status = data;
        //         localStorage.isLogin = true;
        //         $rootScope.personalGroupId = data.personGroupId;
        //         $state.go('main');
        //       } else {
        //         //window.location.href="http://localhost/weblib/app/";//如果没登陆则跳转到登录页
        //       }
        //     });
        //   }
        // } else {//已登录
        //   if (next.name === 'login') {
        //     event.preventDefault();
        //     $state.go('main');
        //   } else {
        //     document.body.style.overflow = 'hidden';
        //   }
        // }
      });

      $rootScope.$state = $state;
      $rootScope.$stateParams = $stateParams;
      $rootScope.path = '';
      $rootScope.listOptions = {
        parentId: 0
      };
      // toastr = require("toastr");
      toastr.options = {
        'closeButton': false,
        'debug': false,
        'newestOnTop': false,
        'progressBar': false,
        'positionClass': 'toast-top-right',
        'preventDuplicates': false,
        'onclick': null,
        'showDuration': '300',
        'hideDuration': '1000',
        'timeOut': '1500',
        'extendedTimeOut': '1000',
        'showEasing': 'swing',
        'hideEasing': 'linear',
        'showMethod': 'fadeIn',
        'hideMethod': 'fadeOut'
      };
      $rootScope.status = {};
      $rootScope.progressbar = ngProgressFactory.createInstance();
      $rootScope.progressbar.setColor('dodgerblue');

    }])
  .config(function ($stateProvider, $urlRouterProvider, $httpProvider) {
      /**拦截器**/
      $httpProvider.interceptors.push('http.sessionRecoverer');//用于Session失效的时候重新登录，并重发请求
      $urlRouterProvider.otherwise('/login');
      $stateProvider.state('login', {
        url: '/login',
        views: {
          'wrap': {
            templateUrl: 'tpl/login.html',
            controller: 'LoginController'
          }
        }
      }).state('main', {
        url: '/main',
        views: {
          'wrap': {
            templateUrl: 'tpl/main.html',
            controller: 'MainController'
          },
          'content@main': {
            templateUrl: 'tpl/personal.html',
            controller: 'PersonalController'
          },
          'slider@main': {
            templateUrl: 'tpl/sidebar.html',
            controller: 'sidebarController'
          },
          'btnWrap@main': {
            templateUrl: 'tpl/topToolBar.html',
            controller: 'TopToolController'
          }
        }
      }).state('main.public', {
        url: '/public?groupId',
        views: {
          'content@main': {
            templateUrl: 'tpl/personal.html',
            controller: 'PersonalController'
          }
        }
      }).state('main.shareToMe', {
        url: '/shareToMe',
        views: {
          'content@main': {
            templateUrl: 'tpl/myshare.html',
            controller: 'shareToMeController'
          }
        }
      }).state('main.shareToMeDetails', {
        url: '/shareToMe/details/:id',
        views: {
          'content@main': {
            templateUrl: 'tpl/myshare.html',
            controller: 'shareToMeDetailsController'
          }
        }
      }).state('main.myshare', {
        url: '/myshare',
        views: {
          'content@main': {
            templateUrl: 'tpl/myshare.html',
            controller: 'myshareController'
          }
        }
      });
    }
  ).controller('appController', ['$scope', '$rootScope', '$state', 'httpRequest.sendRequest', function ($scope, $rootScope, $state, sendRequest) {
  /**获取登录状态及个人信息**/
  sendRequest('/user/status.action', {}, function (data) {
    if (data.status === 'login') {
      $rootScope.status = data;
      localStorage.isLogin = true;
      $rootScope.personalGroupId = data.personGroupId;
      // document.body.style.overflow = 'hidden';
    } else {
      // $state.go('login');
      // document.body.style.overflow = 'auto';
      //window.location.href="http://localhost/weblib/app/";//如果没登陆则跳转到登录页
    }
  });
  $rootScope.logout = function () {
    // localStorage.removeItem('personalId');
    location.href = Config.sitePath + '/pages/logout.jsp?logout=true';
    // sendRequest("/login/logout.action").then(function (res) {
    //   if (res.status === 200) {
    //     localStorage.removeItem('isLogin');
    //     $state.go('login');
    //   }
    // });
  }
}]);

