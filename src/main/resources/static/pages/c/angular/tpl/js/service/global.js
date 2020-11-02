/**
 * Created by dcampus2011 on 15/8/24.
 */
angular.module("global", [])
  .factory("global.staticInfo", function () {
    return {
      sitePath: ''
    };
  })
  .factory("global.currentInfo", function () {
    return {
      userName: ''
    };
  });