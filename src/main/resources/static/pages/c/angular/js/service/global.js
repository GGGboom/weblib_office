/**
 * Created by dcampus2011 on 15/8/24.
 */
angular.module("global", [])
  .factory("global.staticInfo", function () {
    return {
      sitePath: 'http://localhost/webLibProxy'//弃用
    };
  })
  .factory("global.currentInfo", function () {
    return {
      userName: ''
    };
  });