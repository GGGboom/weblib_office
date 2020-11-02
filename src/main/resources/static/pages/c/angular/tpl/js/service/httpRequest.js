/**
 * Created by dcampus2011 on 15/8/24.
 */
angular.module("httpRequest", [])
  .factory("httpRequest.sendRequest", sendRequest)
  .factory('http.sessionRecoverer', SessionRecover)
  .factory("httpRequest.errorManage", function ($rootScope) {
    return function (status, data) {
      if (status === 500) {
        toastr["error"](data.detail);
      } else {
        toastr["error"]("系统错误，请联系管理员");
      }
    }
  });

sendRequest.$inject = ["$http", "global.staticInfo", "httpRequest.errorManage", "$rootScope", "$timeout"];
SessionRecover.$inject = ['$q', '$injector'];
function sendRequest($http, staticInfo, errorManage, $rootScope, $timeout) {
  return function (action, paramData, successFunc, errorFunc) {
    var req = {
      method: 'POST',
      url: staticInfo.sitePath + action,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      }
    };

    if (typeof(paramData) === 'string') {
      req.data = paramData;
    } else {
      req.params = paramData || {};
    }
    if (action.indexOf('getResources.action') > -1) {
      $rootScope.progressbar.start();
    }
    return $http(req)
      .success(function (data, status, headers, config) {
        if (successFunc) {
          successFunc(data, status, headers, config);
          $rootScope.progressbar.complete();
        }
      })
      .error(function (data, status, headers, config) {
        $rootScope.progressbar.complete();
        if (errorFunc) {
          errorFunc(data, status, headers, config);
        } else {
          errorManage(status, data);
        }
      });
  };
}
/****SessionRecover方法定义
 * @desc 用于对登录超时进行重新登陆，及重发请求
 * @param $q
 * @param $injector
 * @returns {{responseError: sessionRecoverer.responseError}}
 * @constructor
 */
function SessionRecover($q, $injector) {
  let sessionRecoverer = {
    responseError: function (response) {

      let Base64 = require('js-base64').Base64,
        $http = $injector.get('$http'),
        $state = $injector.get('$state'),
        $rootScope = $injector.get('$rootScope'),
        // $cookies = $injector.get('$cookies'),
        sendRequest = $injector.get('httpRequest.sendRequest');

      //判断如果当前请求超时
      if (response.data !== null && response.data.code === '480') {
        //     //定义延时对象
        let deferred = $q.defer();
        // //登录参数
        if (localStorage.getItem('username') !== undefined && localStorage.getItem('username') !== null) {
          let paramsObj = {
            account: Base64.decode(localStorage.getItem('username')),
            password: Base64.decode(localStorage.getItem('password'))
          };
          sendRequest('/login/authenticate.action', paramsObj).then(function (res) {
            console.log(res);
            let paramsStr = 'memberId=' + res.data.members[0].id;
            sendRequest('/login/selectMember.action', paramsStr).then(function (data) {
              localStorage.setItem('isLogin', true);
              deferred.resolve(data);
            }, deferred.reject);
          });
        } else {
          localStorage.removeItem('isLogin');
          $state.go('login');
        }
        return deferred.promise.then(function () {
          return $http(response.config);//重发请求
        });
      } else if (typeof (data) !== 'object') {
        $state.go('login');
        localStorage.removeItem('isLogin');
      }
      return $q.reject(response);//否则非登录超时，正常返回其它错误信息
    }
  };
  return sessionRecoverer;//返回Recover对象
}