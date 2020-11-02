/**
 * Created by dcampus2011 on 15/8/25.
 */
angular.module("MainModule", ["am.modal"])
    .controller("MainController", ["$scope", "keepAlive", "modalWindow", "modalConfirm", function ($scope, keepAlive, modalWindow, modalConfirm) {


        Layout.init();
        // keepAlive.start();

        //modal窗口示例
        $scope.t = 'w3';
        $scope.showModal = function () {
            var modal = modalWindow.open({
                template: '{{t}}+{{s}}+{{u}}',
                controller: 'modalCtrl',
                scope: $scope,
                title: '标题',
                width: 300,
                height: 600,
                resolve: {
                    data: function () {
                        return 'rer';
                    }
                },
                onCloseClicked: function () {
                    console.log('点击了默认的关闭按钮');
                    modal.close(result);
                }
            });
            modal.opened.then(function () {
                console.log("窗口打开");
            });
            modal.result.then(function () {
                console.log("窗口关闭");
            });

        };

        //modal确认弹窗示例
        var i = 0;
        $scope.showConfirm = function () {
            modalConfirm.confirm({
                title: '标题' + i++,
                content: '内容' + i++,
                data: {
                    param: 'xxx'
                },
                onConfirm: function (data) {
                    console.log('确定：' + data.param);
                },
                onCancel: function (data) {
                    console.log('取消：' + data.param);
                }
            });
        }
    }])
    //modal窗口示例－控制器
    .controller('modalCtrl', ["$scope", '$modalInstance', 'data', function ($scope, $modalInstance, data) {
        $scope.s = "wghjk";
        $scope.u = data;
        $scope.ok = function () {
            $modalInstance.close();//或者$scope.$close()
        };
    }])


;
/**主界面布局JS**/
var Layout = {
    init: function () {
        var _this = this;
        _this.resize();
        window.onresize = function () {
            _this.resize();
        };
    },
    resize: function () {
        //var _height = document.body.clientHeight, _headerH = document.getElementById("header").offsetHeight;
        //document.getElementById("left").style.height = (_height - _headerH) + "px";
        //document.getElementById("right").style.height = (_height - _headerH) + "px";
    }
}