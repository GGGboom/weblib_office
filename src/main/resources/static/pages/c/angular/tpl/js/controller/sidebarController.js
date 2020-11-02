angular.module("sidebarController", ["ui.tree", "httpRequest", "ng-context-menu"])
    .controller("sidebarController", ["$scope", "$state", "$cookies", "global.staticInfo", "httpRequest.sendRequest","$timeout",
        function ($scope, $state, $cookies, staticInfo, sendRequest,$timeout) {
            $scope.data = [];
            $scope.data1 = [];
            $scope.data2 = [{displayName: '搜索结果（0）',type: "searchResult"}];
            $scope.event = true;
            $scope.searchGroups="";
            $scope.hasSearch=false;
            $scope.visible = function (item) {
                return !($scope.query && $scope.query.length > 0
                && item.displayName.indexOf($scope.query) == -1);
            };
            $scope.onRightClick = function () {
                $scope.event = false;
            };
            $scope.onClose = function () {
                $scope.event = true;
            };
            $scope.findNodes = function (x) {
                return (x.type == "group" || x.type == "groupCollected");
            };
            $scope.searchMyGroups=function(){
                if($scope.searchGroups!=""){
                    sendRequest("/group/searchMyGroups.action", {displayName: $scope.searchGroups},
                        function (data) {
                            for (var x in data.groups) {
                                data.groups[x].type = "group";
                            }
                            $scope.data2[0].displayName="搜索结果（"+data.groups.length+"）";
                            $scope.data2[0].nodes=data.groups;
                            $scope.hasSearch=true;
                        });
                }else{
                    $scope.hasSearch=false;
                }

            };
            $scope.getWatch = function () {
                sendRequest("/user/getWatches.action", {type: "group", start: 0, limit: 1000},
                    function (data) {
                        for (var x in data.watches) {
                            data.watches[x].type = "groupCollected";
                        }
                        $scope.data1[0].nodes=data.watches;
                    });
            };
            $scope.collect = function (x) {
                if (x.type == 'group') {
                    sendRequest("/user/addWatch.action", {id: x.id, type: x.type}, function (data) {
                        $scope.getWatch();
                    });
                }
                else if (x.type == 'groupCollected') {
                    sendRequest("/user/deleteWatch.action", {id: x.watchId}, function (data) {
                        $scope.getWatch();
                    });
                }
            };
            $scope.selectImg = function (type, collapsed) {
                if (type == "group" || type == "groupCollected") {
                    return "group";
                } else if (type == "collect")
                    return "collect";
                else if (collapsed) return "yes";
                else return "no";
            };
            $scope.selectTitle = function (x) {
                if (x.type == 'groupCollected') {
                    return "移除收藏";
                }
                else if (x.type == 'group') {
                    return "添加收藏";
                }
                else return '';
            };
            /**顶层树节点**/
            var paramsObj = {containPersonGroup: false, containAblumCategory: false};
            sendRequest("/group/trees.action", paramsObj,
                function (data) {
                    $scope.data.push({displayName: '公共资源库', nodes: data.children})
                });
            sendRequest("/user/getWatches.action", {type: "group", start: 0, limit: 1000},
                function (data) {
                    for (var x in data.watches) {
                        data.watches[x].type = "groupCollected";
                    }
                    $scope.data1.push({displayName: '我的收藏', nodes: data.watches, type: "collect"})
                });
            $timeout(function() {
                angular.element("#searchResult").find(".tree-node").triggerHandler("click")
            });
            $scope.openTree = function (scope) {
                if ($scope.event) {
                    var nodeData = scope.$modelValue;
                    if (nodeData.nodes != undefined && nodeData.type != "group" && nodeData.type != "groupCollected") {
                        scope.toggle();
                        return false;
                    }
                    nodeData.nodes = [];
                    if (nodeData.type == "group") {
                        $state.go("main.public", {groupId: nodeData.id});
                    }
                    else if (nodeData.type == "groupCollected") {
                        $state.go("main.public", {groupId: nodeData.groupId});
                    } else if(nodeData.type == "searchResult"){
                        scope.toggle();
                    }
                    else {
                        sendRequest("/group/trees.action",
                            {
                                containPersonGroup: false,
                                containAblumCategory: false,
                                categoryId: nodeData.id
                            }, function (data) {
                                nodeData.nodes=data.children;
                                scope.toggle();
                            });
                    }
                }
            }
        }])
    .filter('treeImg', function () {
        return function (img) {
            if (img == "group") return "images/leaf.png";
            else if (img == "collect") return "images/favorites.png";
            else if (img == "yes") {
                return "images/category_open.png";
            }
            else if (img == "no") return "images/category_closed.png";
        }
    });