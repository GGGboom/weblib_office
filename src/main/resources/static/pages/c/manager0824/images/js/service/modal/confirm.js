/**
 * Created by dcampus2011 on 15/10/28.
 */


angular.module("am.modal")
    .directive('amConfirm', [
        function( ) {

            return {
                transclude:true,
                restrict: 'EA',
                replace: true,
                templateUrl: 'js/service/modal/amModalConfirm.html',
                scope:{
                    title:'=confirmTitle',
                    content:'='
                }
            };
        }])
    .provider("modalConfirm",function(){
        var $confirmProvider = {
            options: {
            },
            $get:['$rootScope',"$compile",'$timeout',function($rootScope,$compile,$timeout){
                var $confirm={};
                //var hasInit=false;
                $confirm.confirm=function(confirmOptions){
                    //if(!hasInit){

                    var modalScope = $rootScope.$new();
                    modalScope.title='';
                    modalScope.content=''
                    modalScope.closeClicked=function(){//console.log("closeClicked")
                        if(modalOptions.onCloseClicked){
                            modalOptions.onCloseClicked();
                        }else{

                        }

                    }

                    var body =angular.element('body').eq(0);

                    var angularDomEl = angular.element('<am-confirm id="am_confirm" confirm-title="title" content="content"></am-confirm>');

                    var modalDomEl = $compile(angularDomEl)(modalScope);

                    body.append(modalDomEl);

                    //hasInit=true;
                    //}
                    modalScope.title=confirmOptions.title;
                    modalScope.content=confirmOptions.content;
                    $timeout(function(){

                        var $modal = $('#am_confirm');


                        var onConfirm = function() {
                            if(confirmOptions.onConfirm){
                                confirmOptions.onConfirm(confirmOptions.data);
                            }
                        };
                        var onCancel = function() {
                            if(confirmOptions.onCancel){
                                confirmOptions.onCancel(confirmOptions.data);
                            }
                        }

                        var amOption={
                            relatedElement: this,
                            closeViaDimmer: 0,
                            onConfirm: onConfirm,
                            onCancel: onCancel
                        };
                        $modal.modal(amOption);

                        $modal.on('closed.modal.amui', function(){
//                    console.log('完成关闭动画');
                            $(this).removeData('amui.modal');
                            angularDomEl.remove();
                            modalScope.$destroy();
                        });


                    },100);
                }
                return $confirm;
            }]};

        return $confirmProvider;
    })