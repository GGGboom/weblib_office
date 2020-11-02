angular.module("fileChainController", ["am.modal"])
    .controller('fileChainController',["$scope",'$modalInstance','items',"httpRequest.sendRequest",function($scope,$modalInstance,items,sendRequest){
        $scope.privateLink=true;
        $scope.btntext="复制链接及密码";
        $scope.getUrl=function(id,setCode){
            sendRequest( "/webmail/getTokenDownloadUrl.action",
                { id: id, setCode: setCode},function (data) {
                    $scope.urlText = data.data[0].url;
                    if(setCode==1)
                    $scope.urlPassword = data.data[0].code;
                    else  $scope.urlPassword=" ";
                    $scope.copyLink();
                });
        };
        $scope.change= function () {
            $scope.privateLink=!$scope.privateLink;
            if( $scope.privateLink){
                $scope.getUrl(items,1);
                $scope.btntext="复制链接及密码";
            } else {
                $scope.getUrl(items,0);
                $scope.btntext="复制链接";
            }
        };
        $scope.copyLink=function(){
            var text="";
            if( $scope.privateLink)
             text=$scope.urlText+" 密码:"+  $scope.urlPassword;
            else text= $scope.urlText;
            clipButtonClick();
            setClipboardText(text);
        };
        $scope.getUrl(items,1);
    }]);