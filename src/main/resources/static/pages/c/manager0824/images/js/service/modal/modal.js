/**
 * Created by dcampus2011 on 15/9/18.
 */

angular.module("am.modal", [])
    .factory('$$stackedMap', function () {
        return {
            createNew: function () {
                var stack = [];

                return {
                    add: function (key, value) {
                        stack.push({
                            key: key,
                            value: value
                        });
                    },
                    get: function (key) {
                        for (var i = 0; i < stack.length; i++) {
                            if (key == stack[i].key) {
                                return stack[i];
                            }
                        }
                    },
                    keys: function () {
                        var keys = [];
                        for (var i = 0; i < stack.length; i++) {
                            keys.push(stack[i].key);
                        }
                        return keys;
                    },
                    top: function () {
                        return stack[stack.length - 1];
                    },
                    remove: function (key) {
                        var idx = -1;
                        for (var i = 0; i < stack.length; i++) {
                            if (key == stack[i].key) {
                                idx = i;
                                break;
                            }
                        }
                        return stack.splice(idx, 1)[0];
                    },
                    removeTop: function () {
                        return stack.splice(stack.length - 1, 1)[0];
                    },
                    length: function () {
                        return stack.length;
                    }
                };
            }
        };
    })
    .factory('$$multiMap', function () {
        return {
            createNew: function () {
                var map = {};

                return {
                    entries: function () {
                        return Object.keys(map).map(function (key) {
                            return {
                                key: key,
                                value: map[key]
                            };
                        });
                    },
                    get: function (key) {
                        return map[key];
                    },
                    hasKey: function (key) {
                        return !!map[key];
                    },
                    keys: function () {
                        return Object.keys(map);
                    },
                    put: function (key, value) {
                        if (!map[key]) {
                            map[key] = [];
                        }

                        map[key].push(value);
                    },
                    remove: function (key, value) {
                        var values = map[key];

                        if (!values) {
                            return;
                        }

                        var idx = values.indexOf(value);

                        if (idx !== -1) {
                            values.splice(idx, 1);
                        }

                        if (!values.length) {
                            delete map[key];
                        }
                    }
                };
            }
        };
    })

    .factory("windowManager", ['$rootScope', "$compile", "$document", '$timeout', '$controller',
        function ($rootScope, $compile, $document, $timeout, $controller) {

            var windowManager = {};
            windowManager.open = function (instance, modalOptions) {
                var modalId = 'id_' + new Date().getTime();
                instance.modalId = modalId;
                instance.deferred = modalOptions.deferred;
                var body = angular.element('body').eq(0);
                var modalScope = (modalOptions.scope || $rootScope).$new();
                modalScope.closeClicked = function () {//console.log("closeClicked")
                    if (modalOptions.onCloseClicked) {
                        modalOptions.onCloseClicked();
                    } else {
                        instance.close(true);
                    }

                }
                var angularDomEl = angular.element('<am-window id="' + modalId + '" modal-title="' + modalOptions.title + '" close-clicked="closeClicked()"></am-window>');
                var templateContent = modalOptions.content;
                angularDomEl.attr({
//                    'template-url': modal.windowTemplateUrl,
//                    'window-class': modal.windowClass,
//                    'window-top-class': modal.windowTopClass,
//                    'size': modal.size,
//                    'index': openedWindows.length() - 1,
//                    'animate': 'animate'
                }).html(templateContent);


                var modalDomEl = $compile(angularDomEl)(modalScope);

                instance.modalDomEl = modalDomEl;

                instance.modalScope = modalScope;
                body.append(modalDomEl);


                $timeout(function () {
                    var $modal = $('#' + modalId);
                    var amOption = {closeViaDimmer: 0};
                    if (modalOptions.width) {
                        amOption.width = modalOptions.width;
                    }
                    if (modalOptions.height) {
                        amOption.height = modalOptions.height;
                    }
                    $modal.modal(amOption);
                }, 100);
            };

            windowManager.hide = function (modalInstance, result) {

                var $modal = $('#' + modalInstance.modalId);
                $modal.modal('close');
                modalInstance.deferred.resolve(result);
                return true;
            };
            windowManager.show = function (modalInstance, modalOpenedDeferred) {

                var $modal = $('#' + modalInstance.modalId);
                $modal.modal('open');
                modalOpenedDeferred.resolve(true);
                return true;
            };
            windowManager.close = function (modalInstance, result) {
                var $modal = $('#' + modalInstance.modalId);
                $modal.modal('close');
                $modal.on('closed.modal.amui', function () {
//                    console.log('完成关闭动画');
                    modalInstance.modalDomEl.remove();
                    modalInstance.modalScope.$destroy();
                });
                modalInstance.deferred.resolve(result);
                return true;
            };

            windowManager.dismiss = function (modalInstance, reason) {
                var $modal = $('#' + modalInstance.modalId);
                $modal.modal('close');

                modalInstance.deferred.reject(reason);
                return true;
            };

            return windowManager;
        }])

    .directive('amWindow', ['$q',
        function ($q) {

            return {
                transclude: true,
                restrict: 'EA',
                replace: true,
                scope: {
                    title: '@modalTitle',
                    closeClicked: '&'
                },
                templateUrl: 'js/service/modal/amModalWindow.html',
                link: function (scope, element, attrs) {
//                    scope.close = function(evt) {
//                        var modal = $modalStack.getTop();
//                        if (modal && modal.value.backdrop && modal.value.backdrop !== 'static' && (evt.target === evt.currentTarget)) {
//                            evt.preventDefault();
//                            evt.stopPropagation();
//                            $modalStack.dismiss(modal.key, 'backdrop click');
//                        }
//                    };
//
//                    // moved from template to fix issue #2280
//                    element.on('click', scope.close);

                    // This property is only added to the scope for the purpose of detecting when this directive is rendered.
                    // We can detect that by using this property in the template associated with this directive and then use
                    // {@link Attribute#$observe} on it. For more details please see {@link TableColumnResize}.
                    scope.$isRendered = true;

                    // Deferred object that will be resolved when this modal is render.
                    var modalRenderDeferObj = $q.defer();
                    // Observe function will be called on next digest cycle after compilation, ensuring that the DOM is ready.
                    // In order to use this way of finding whether DOM is ready, we need to observe a scope property used in modal's template.
                    attrs.$observe('modalRender', function (value) {
                        if (value == 'true') {
                            modalRenderDeferObj.resolve();
                        }
                    });

                    modalRenderDeferObj.promise.then(function () {

                        $q.when(null).then(function () {
                            var inputsWithAutofocus = element[0].querySelectorAll('[autofocus]');
                            /**
                             * Auto-focusing of a freshly-opened modal element causes any child elements
                             * with the autofocus attribute to lose focus. This is an issue on touch
                             * based devices which will show and then hide the onscreen keyboard.
                             * Attempts to refocus the autofocus element via JavaScript will not reopen
                             * the onscreen keyboard. Fixed by updated the focusing logic to only autofocus
                             * the modal element if the modal does not contain an autofocus element.
                             */
                            if (inputsWithAutofocus.length) {
                                inputsWithAutofocus[0].focus();
                            } else {
                                element[0].focus();
                            }
                        });

                    });
                }
            };
        }])

    .provider("modalWindow", function () {
        var $modalProvider = {
            options: {},
            $get: ['$injector', '$rootScope', '$http', '$q', '$templateCache', '$controller', 'windowManager',
                function ($injector, $rootScope, $http, $q, $templateCache, $controller, windowManager) {
                    var $modal = {};

                    getTemplatePromise = function (options) {
                        fromString = function (template) {
                            return angular.isFunction(template) ? template() : template;
                        };

                        fromUrl = function (url) {
                            if (angular.isFunction(url)) url = url();
                            if (url == null) return null;
                            else return $http
                                .get(url, {cache: $templateCache, headers: {Accept: 'text/html'}})
                                .then(function (response) {
                                    return response.data;
                                });
                        };

                        var templateStr = angular.isDefined(options.template) ? fromString(options.template) :
                            angular.isDefined(options.templateUrl) ? fromUrl(options.templateUrl) :
                                "";
                        return $q.when(templateStr);
                    }

                    function getResolvePromises(resolves) {
                        var promisesArr = [];
                        angular.forEach(resolves, function (value) {
                            if (angular.isFunction(value) || angular.isArray(value)) {
                                promisesArr.push($q.when($injector.invoke(value)));
                            } else if (angular.isString(value)) {
                                promisesArr.push($q.when($injector.get(value)));
                            } else {
                                promisesArr.push($q.when(value));
                            }
                        });
                        return promisesArr;
                    }

                    var promiseChain = null;
                    $modal.getPromiseChain = function () {
                        return promiseChain;
                    };

                    $modal.open = function (modalOptions) {

                        var modalResultDeferred = $q.defer();
                        var modalOpenedDeferred = $q.defer();
                        var modalRenderDeferred = $q.defer();

                        //prepare an instance of a modal to be injected into controllers and returned to a caller
                        var modalInstance = {
                            result: modalResultDeferred.promise,
                            opened: modalOpenedDeferred.promise,
                            rendered: modalRenderDeferred.promise,
                            close: function (result) {
                                return windowManager.close(modalInstance, result);
                            },
                            dismiss: function (reason) {
                                return windowManager.dismiss(modalInstance, reason);
                            },
                            hide: function (result) {
                                return windowManager.hide(modalInstance, result);
                            },
                            show: function () {
                                return windowManager.show(modalInstance, modalOpenedDeferred);
                            }
                        };
                        //merge and clean up options
                        modalOptions = angular.extend({}, $modalProvider.options, modalOptions);
                        modalOptions.resolve = modalOptions.resolve || {};

                        //verify options
                        if (!modalOptions.template && !modalOptions.templateUrl) {
                            throw new Error('One of template or templateUrl options is required.');
                        }

                        var templateAndResolvePromise =
                            $q.all([getTemplatePromise(modalOptions)].concat(getResolvePromises(modalOptions.resolve)));

                        // Wait for the resolution of the existing promise chain.
                        // Then switch to our own combined promise dependency (regardless of how the previous modal fared).
                        // Then add to $modalStack and resolve opened.
                        // Finally clean up the chain variable if no subsequent modal has overwritten it.
                        var samePromise;
                        samePromise = promiseChain = $q.all([promiseChain])
                            .then(function () {
                                return templateAndResolvePromise;
                            }, function () {
                                return templateAndResolvePromise;
                            })
                            .then(function resolveSuccess(tplAndVars) {

                                var modalScope = (modalOptions.scope || $rootScope).$new();
                                modalScope.$close = modalInstance.close;
                                modalScope.$dismiss = modalInstance.dismiss;


                                var ctrlInstance, ctrlLocals = {};
                                var resolveIter = 1;

                                //controllers
                                if (modalOptions.controller) {
                                    ctrlLocals.$scope = modalScope;
                                    ctrlLocals.$modalInstance = modalInstance;
                                    angular.forEach(modalOptions.resolve, function (value, key) {
                                        ctrlLocals[key] = tplAndVars[resolveIter++];
                                    });

                                    ctrlInstance = $controller(modalOptions.controller, ctrlLocals);
                                    if (modalOptions.controllerAs) {
                                        if (modalOptions.bindToController) {
                                            angular.extend(ctrlInstance, modalScope);
                                        }

                                        modalScope[modalOptions.controllerAs] = ctrlInstance;
                                    }
                                }

                                windowManager.open(modalInstance, {
                                    scope: modalScope,
                                    deferred: modalResultDeferred,
                                    renderDeferred: modalRenderDeferred,
                                    content: tplAndVars[0],
                                    animation: modalOptions.animation,
                                    backdrop: modalOptions.backdrop,
                                    keyboard: modalOptions.keyboard,
                                    backdropClass: modalOptions.backdropClass,
                                    windowTopClass: modalOptions.windowTopClass,
                                    windowClass: modalOptions.windowClass,
                                    windowTemplateUrl: modalOptions.windowTemplateUrl,
                                    size: modalOptions.size,
                                    openedClass: modalOptions.openedClass,

                                    title: modalOptions.title,
                                    width: modalOptions.width,
                                    height: modalOptions.height,
                                    onCloseClicked: modalOptions.onCloseClicked
                                });
                                modalOpenedDeferred.resolve(true);

                            }, function resolveError(reason) {
                                modalOpenedDeferred.reject(reason);
                                modalResultDeferred.reject(reason);
                            })
                            .finally(function () {
                                if (promiseChain === samePromise) {
                                    promiseChain = null;
                                }
                            });

                        return modalInstance;
                    };

                    return $modal;
                }]
        };

        return $modalProvider;
    })