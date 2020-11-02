/**
 * Created by dcampus2011 on 15/8/24.
 */
angular.module("httpRequest", [])
    .factory("httpRequest.sendRequest", ["$http", "global.staticInfo", "httpRequest.errorManage", function ($http, staticInfo, errorManage) {
        return function (action, paramData, successFunc, errorFunc) {
            var req = {
                method: 'POST',
                url: staticInfo.sitePath + action,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                },
                transformResponse:appendTransform($http.defaults.transformResponse, function (data) {
                    return data.replace(/\'/g,'\"');
                })
            };
            if (typeof(paramData) === 'string') {
                req.data = paramData;
            } else {
                req.params = paramData || {};
            }
            function appendTransform(defaults, transform) {

                // We can't guarantee that the default transformation is an array
                defaults = angular.isArray(defaults) ? defaults : [defaults];

                // Append the new transformation to the defaults
                return defaults.reverse().concat(transform).reverse();
            }


            return $http(req)
                .success(function (data, status, headers, config) {
                    if (successFunc) {
                        successFunc(data, status, headers, config);
                    }
                })
                .error(function (data, status, headers, config) {
                    if (errorFunc) {
                        errorFunc(data, status, headers, config);
                    } else {
                        errorManage(status, data);
                    }
                });
        }
    }])

    .factory("httpRequest.errorManage", function ($rootScope) {
        return function (status, data) {
            if (status == 500) {
                $rootScope.progressbar.complete();
                $rootScope.toastr["error"](data.detail);
            } else {
                $rootScope.toastr["error"]("其他错误");
            }
        }
    });