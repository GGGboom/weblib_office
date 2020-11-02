/**
 * Created by lfn on 2015/12/29.
 */
angular.module("modifyAccountController", ["am.modal"])
    .controller('modifyAccountController',["$scope","$rootScope",'$modalInstance','items',"httpRequest.sendRequest",function($scope,$rootScope,$modalInstance,items,sendRequest){
        $scope.data={};
        $scope.data.name=items.userdata.name;
        $scope.data.company=items.userdata.company;
        $scope.data.department=items.userdata.department;
        $scope.data.email=items.userdata.email;
        $scope.data.mobile=items.userdata.mobile;
        $scope.data.phone=items.userdata.phone;
        $scope.data.im=items.userdata.im;
        $scope.data.old_password="";
        $scope.data.new_password="";
        $scope.data.confirm_password="";
        $scope.modifyBtn=function(){
            if( $scope.data.name!=items.userdata.name||$scope.data.company!=items.userdata.company||$scope.data.department!=items.userdata.department||
                $scope.data.email!=items.userdata.email||$scope.data.phone!=items.userdata.phone|| $scope.data.im!=items.userdata.im){
                // $rootScope.progressbar.start();
                sendRequest("/user/modifyAccount.action", {
                    name:  $scope.data.name,
                    company:$scope.data.company,
                    department:$scope.data.department,
                    mobile: $scope.data.mobile,
                    phone: $scope.data.phone,
                    email: $scope.data.email,
                    im:$scope.data.im
                }, function (data) {
                    // $rootScope.progressbar.complete();
                    if (data.type == "success") {
                        items.getAccount();
                        toastr["success"]("个人信息修改成功！");
                    } else   toastr["error"]("个人信息修改失败！");
                });
            }
            if($scope.data.old_password&&$scope.data.old_password!=""){
                if($scope.data.new_password==""){
                   toastr["warning"]("请填写新密码！");
                }
                else if($scope.data.confirm_password==""){
                    toastr["warning"]("请确认新密码！");
                }
                else if( $scope.data.new_password!= $scope.data.confirm_password){
                    toastr["error"]("两次输入的新密码不一致！");
               }
                else {
                    sendRequest("/user/modifyPassword.action", {
                        password:$scope.data.old_password,
                        newPassword:$scope.data.new_password
                    }, function (data) {
                        if (data.result == true) {
                            toastr["success"](data.message);
                        } else   toastr["error"](data.message);
                    });
                    $modalInstance.close();
                }
            }else{
                $modalInstance.close();
            }
        };
        $scope.cancel=function(){
            $modalInstance.close();
        }
    }]);