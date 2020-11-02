/**
 * Created by dcampus2011 on 15/8/24.
 */
angular.module("global",[])
    .factory("global.staticInfo",function(){
        return {
            sitePath:"http://202.38.254.204/weblib"
            // sitePath:"http://172.16.0.30/weblib"

        };
    })
    .factory("global.currentInfo",function(){
        return {
            userName:""
        };
    });