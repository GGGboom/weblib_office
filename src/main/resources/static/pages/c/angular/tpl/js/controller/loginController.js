angular.module("LoginModule", ["ngCookies", "httpRequest"])
  .controller("LoginController", ["$scope", "$cookies", "global.staticInfo", "httpRequest.sendRequest", "$state",
    "$timeout", function ($scope, $cookies, staticInfo, sendRequest, $state, $timeout) {

      let Base64 = require('js-base64').Base64;
      $scope.saveToCookies = function () {
        localStorage.setItem('username', Base64.encode($scope.loginInfo.username));
        localStorage.setItem('password', Base64.encode($scope.loginInfo.password));
        // $cookies.username = Base64.encode($scope.loginInfo.username);
        // $cookies.password = Base64.encode($scope.loginInfo.password);
        $scope.loginInfo.shouldRemember = true;
      };

      $scope.getFromCookies = function () {
        $scope.loginInfo.username = Base64.decode(localStorage.getItem('username'));
        $scope.loginInfo.password = Base64.decode(localStorage.getItem('password'));
        $scope.loginInfo.shouldRemember = true;
        $scope.loginInfo.isChecked = 1;
      };

      $scope.cleanCookies = function () {
        localStorage.removeItem('username');
        localStorage.removeItem('password');
        $scope.loginInfo.shouldRemember = false;
      };

      // $scope.$watch("loginInfo.shouldRemember", function (newVal, oldVal) {
      //     if (!newVal) {
      //         $scope.cleanCookies();
      //     }
      // });


      $scope.$watch("casOptions", function (newVal, oldVal) {
        if (newVal === '本地认证') {
          $scope.isCas = false;
        } else {
          $scope.isCas = true;
        }
      });


      $scope.rememberme = function (obj) {
        if ($scope.loginInfo.isChecked === 0) {
          $scope.loginInfo.isChecked = 1;
          $scope.loginInfo.shouldRemember = true;
          $scope.saveToCookies();
        } else {
          $scope.loginInfo.isChecked = 0;
          $scope.loginInfo.shouldRemember = false;
          $scope.cleanCookies();
        }

      }

      $scope.login = function (username, password) {
        if (!$scope.isCas) {
          let paramsObj = { "account": $scope.loginInfo.username, "password": $scope.loginInfo.password };
          sendRequest("/login/authenticate.action", paramsObj,
            function (data, status, headers, config) {
              var paramsStr = "memberId=" + data.members[0].id;
              sendRequest("/login/selectMember.action", paramsStr,
                function (data, status, headers, config) {
                  if ($scope.loginInfo.shouldRemember) {
                    $scope.saveToCookies();
                  }
                  localStorage.setItem('isLogin', true);
                  $state.go('main');
                });
            });
        } else {
          document.casForm.submit();
          if ($scope.loginInfo.shouldRemember) {
            $scope.saveToCookies();
          }
        }
      };

      $scope.loginInfo = {};
      $scope.loginInfo.username = "";
      $scope.loginInfo.password = "";
      $scope.loginInfo.shouldRemember = false;
      $scope.loginInfo.isShow = false;
      $scope.loginInfo.isChecked = 0;
      $scope.optionsData = [
        { id: 0, name: '本地认证' },
        { id: 1, name: '中央认证' }
      ];
      $scope.casOptions = '本地认证';
      $scope.isCas = false;

      if (localStorage.getItem('username') !== undefined && localStorage.getItem('username') !== null) {
        $scope.getFromCookies();
      } else {
        $timeout(function () {
          $scope.loginInfo.username = '';
          $scope.loginInfo.password = '';
        }, 500);
      }
    }]);