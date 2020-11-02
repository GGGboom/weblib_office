/**
 * Created by dcampus2011 on 15/9/11.
 */
angular.module("userStatus", [])
    .factory("userStatus", ["httpRequest.sendRequest", "ui", function (sendRequest) {
        return {
            getStatus: function () {
                return sendRequest("/user/status.action", null,
                    function (data, status, headers, config) {
                        if (data.status == "guest") {

                        } else {

                        }
                    });
            }
        }
    }])