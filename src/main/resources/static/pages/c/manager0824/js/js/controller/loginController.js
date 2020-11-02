angular.module('loginController', [])
    .controller('loginController', ['$location', function ($location) {
        window.location.href = "http://" + window.location.host + "/2016newmanager/app/index.html#/login";
    }])