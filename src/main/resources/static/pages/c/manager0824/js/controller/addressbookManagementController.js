/**
 * Created by QQChan on 2016/9/23.
 */
var am = angular.module('addressbookManagementController',[]);
    am.controller('addressbookManagementController',['$scope',
        '$rootScope',
        'httpRequest.sendRequest',
        '$state',
        function ($scope, $rootScope, sendRequest){
            //init panel hight
            $scope.windowHeight = angular.element(window).height();
            angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
            angular.element(window).resize(function () {
                $scope.windowHeight = angular.element(window).height();
                angular.element(document.querySelectorAll(".ngViewport")).height($scope.windowHeight - 80);
            });

            //用于获取当前视图
            $scope.currentModal = {};//当前打开的modal
            //左边视图，标题栏按钮
            $scope.newContact = {};
            $scope.addContact = function () {
                $scope.currentModal = $("#addContactPrompt").modal({
                    onCancel:function () {
                        $scope.currentModal.modal('close');
                        $scope.newContact = {};
                    },
                    onConfirm:function () {
                        sendRequest('/user/createContact.action', $scope.newContact,
                            function (data) {
                                initcontactsList();
                            }, function (data,code) {
                                $rootScope.toastr.error(data.detail || '未知错误');
                            });
                    }
                });
            };
            $scope.editContact = function () {
                $scope.newContact = $scope.selectedContactRow;
                $scope.currentModal = $("#editContactPrompt").modal({
                    onCancel:function () {
                        $scope.currentModal.modal('close');
                        $scope.newContact = {};
                    },
                    onConfirm:function () {
                        sendRequest('/user/modifyContact.action',
                            {id:$scope.newContact.id,
                                contactName:$scope.newContact.name,
                                desc:$scope.newContact.desc},
                            function (data) {
                                console.log('success');
                                $scope.newContact = {};
                                initcontactsList();
                            },function (data,code) {
                                $rootScope.toastr.error(data.detail || '未知错误');
                            });
                    }
                })
            };
            $scope.deleteContact = function () {
                $scope.newContact = $scope.selectedContactRow;
                // console.log($scope.newContact);
                $scope.currentModal = $("#deleteContactPrompt").modal({
                    onCancel:function () {
                        $scope.currentModal.modal('close');
                        $scope.newContact = {};
                    },
                    onConfirm:function () {
                        sendRequest('/user/deleteContact.action',{id:$scope.newContact.id},function (data) {
                            initcontactsList();
                        },function (data,code) {
                            $rootScope.toastr.error(data.detail || '未知错误');
                        });
                    }
                });
            };
            var initcontactsList = function () {
                sendRequest('/user/getAllContact.action',{},function (data) {
                    $scope.contactsTotalCount = data.totalcount;
                    $scope.contactsTree = data.contacts;
                }, function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                });
            };
            initcontactsList();
            $scope.contactRootInContact =[];
            $scope.showUsersinContact = function () {
                $scope.contactRootInContact =[];
                sendRequest('/user/getContactTree.action',{id:$scope.selectedContactRow.id},function (data) {
                    angular.forEach(data.contactRoot, function (value) {
                        $scope.contactRootInContact.push({nodes:value.folder,
                                                          id:value.id,
                                                          name:value.name,
                                                          /*teams:value.team,*/
                                                          type:value.type});
                        /*$scope,contactRootInContact.push(value);*/
                    });

                },function (data,code) {
                    $rootScope.toastr.error(data.detail || '未知错误');
                })
            };
            $scope.selectedContactRow = {};
            $scope.selectedContact = function (thisContact) {
                $scope.selectedContactRow = thisContact;
                $scope.showUsersinContact();
            };
            //中间视图，标题栏按钮
            //中间视图，正文栏
            $scope.isSelected = function (scope) {
                if ($scope.selectedFolder == scope.$modelValue) {
                    return {'background-color': '#3bb4f2'};
                }
                return {'background-color': '#ffffff'};
            };
            $scope.selectedFolder = {};
            $scope.openContact = function (scope) {
                var _thisNode = scope.$modelValue;
                $scope.selectedFolder = _thisNode;
                if (typeof (_thisNode.folders) != 'undefined') {
                    scope.toggle();
                    console.log("heheeh");
                    return false;
                }
            };
            $scope.selectedFolderInContact = function (scope) {
                var _thisNode = scope.$modelValue;
                console.log(_thisNode);
                $scope.selectedFolder = _thisNode;
            }
            //右边视图，正文栏
            $scope.usersTree = [];//用于存储树形结构的数组
            var initusersTree = function () {
                sendRequest('/grouper/grouperTree.action',
                    {
                        id: 0,
                        type: 'folder',
                        folderOnly: false
                    },function (data) {
                        data.members.push({
                            id:-1,
                            text:"所有用户",
                            type:"allUsers"
                        });
                        $scope.usersTree.push({text: "根目录",type:"rootFolder",nodes: data.members});
                    });
            };
            initusersTree();
            $scope.openUsersTree = function (scope) {
                var _thisUserNode = scope.$modelValue;//获取当前选中的值
                if( typeof(_thisUserNode.nodes) != 'undefined' && _thisUserNode.type != 'team' && _thisUserNode.type !='allUsers' ){
                    scope.toggle();
                    return false;
                }
                if(_thisUserNode.type != 'team' && _thisUserNode.type !='allUsers' && typeof(_thisUserNode.nodes) == 'undefined'){
                    sendRequest('/grouper/grouperTree.action',
                        {
                            id:_thisUserNode.id,
                            type: _thisUserNode.type,
                            folderOnly:false,
                            fullName:_thisUserNode.fullName
                        },function (data) {
                            _thisUserNode.nodes = data.members;
                            scope.toggle();
                        });
                    return false;
                }
            };
            $scope.foldersCheckedCount = 0;
            $scope.foldersCheckedId =[];//存储待添加分组的id
            //以下两个函数为复选框函数，目前实现拖拽，暂未使用
            $scope.updateFoldersChecked = function ($event,id) {
                var checkbox = $event.target;//取得当前的checkbox
                var action = (checkbox.checked? 'add':'remove');//取得checkbox是否选中的值
                updateFoldersCheckedValue(action,id);//更新数据
            };
            var updateFoldersCheckedValue = function(action,id){
                if(action == 'add'){
                    $scope.foldersCheckedId.push(id);
                    $scope.foldersCheckedCount ++;
                }
                if(action == 'remove'){
                    var idx = $scope.foldersCheckedId.indexOf(id);
                    $scope.foldersCheckedId.splice(idx,1);
                    $scope.foldersCheckedCount --;
                }
            };
            $scope.addFoldersToContact = function () {
                $scope.currentModal = $('#addFoldersToContactConfirm').modal({
                    onCancel:function () {
                        $scope.currentModal.modal('close');
                    },
                    onConfirm:function () {
                        sendRequest('/user/addFoldersToContact.action', {
                                ids:$scope.foldersCheckedId,
                                id:$scope.selectedContactRow.id
                            },
                            function (data) {
                                $scope.foldersCheckedId = [];
                                $scope.foldersCheckedCount = 0;
                            }, function (data,code) {
                                $rootScope.toastr.error(data.detail || '未知错误');
                            });
                    }
                });
            };

            $scope.foldersCheckedNodeText = [];
            $scope.onDragComplete = function (id,text) {
                $scope.foldersCheckedId.push(id);
                $scope.foldersCheckedNodeText.push(text);

            }
            $scope.onDropComplete = function (thisContact) {
                $scope.selectedContactRow = thisContact;
                $scope.currentModal = $("#beforeDropPrompt").modal({
                    onCancel:function () {
                        $scope.currentModal.modal('close');
                        $scope.foldersCheckedId = [];
                        $scope.foldersCheckedNodeText = [];
                        $scope.foldersCheckedCount = 0;

                    },
                    onConfirm:function () {
                        sendRequest('/user/addFoldersToContact.action', {
                                ids:$scope.foldersCheckedId,
                                id:$scope.selectedContactRow.id
                            },
                            function (data) {
                                $scope.foldersCheckedId = [];
                                $scope.foldersCheckedNodeText = [];
                                $scope.foldersCheckedCount = 0;
                                $scope.showUsersinContact();
                            }, function (data,code) {
                                $rootScope.toastr.error(data.detail || '未知错误');
                            });
                        $scope.currentModal.modal('close');
                    }
                });
            };
            $scope.removeFoldersFromContact = function () {
                $scope.currentModal = $('#removeFoldersFromContactConfirm').modal({
                    onCancel:function () {
                        $scope.currentModal.modal('close');
                    },
                    onConfirm:function () {
                        sendRequest('/user/removeFoldersFromContact.action',{
                            id:$scope.selectedContactRow.id,
                            ids:$scope.selectedFolder.id
                        })
                    }
                });
            }


        }]);