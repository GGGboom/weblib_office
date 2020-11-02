angular.module('loginController', [])
    .controller('loginController', ['$location', function ($location) {
        window.location.href = "http://" + window.location.host + "/app/index.html#/login";
    }])