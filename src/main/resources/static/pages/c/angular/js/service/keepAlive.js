/**
 * Created by dcampus2011 on 15/9/11.
 */

angular.module("keepAlive", [])
  .factory("keepAlive", ["httpRequest.sendRequest", "$interval", function (sendRequest, $interval) {
    var keepAlive = function () {
      sendRequest("/user/alive.action", "",
        function (data, status, headers, config) {

        },
        function (data, status, headers, config) {

        });
    }
    var promise;
    var t = 3 * 60 * 1000;

    return {
      start: function () {
        if (!promise) {
          promise = $interval(keepAlive, t);
        }
      },
      stop: function () {
        if (promise) {
          $interval.cancel(promise);
          promise = null;
        }
      }
    }
  }])