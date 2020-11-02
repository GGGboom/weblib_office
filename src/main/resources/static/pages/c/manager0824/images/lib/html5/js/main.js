$(function () {
    String.prototype.getQueryString = function (name)//name 是URL的参数名字
    {
        var reg = new RegExp("(^|&|\\#)" + name + "=([^&]*)(&|$)"), r;
        if (r = this.match(reg)) return unescape(r[2]);
        return null;
    };
    var singleSize = parseInt(window.location.href.getQueryString("singleSize"));
    var groupId = parseInt(window.location.href.getQueryString("groupId"));
    var parentId = parseInt(window.location.href.getQueryString("parentId"));
    var leaveSpace = parseInt(window.location.href.getQueryString("leaveSpace"));
    singleSize = parseInt(window.location.href.getQueryString("singleSize"));
    groupId = parseInt(window.location.href.getQueryString("groupId"));
    parentId = parseInt(window.location.href.getQueryString("parentId"));
    leaveSpace = parseInt(window.location.href.getQueryString("leaveSpace"));
    $('#groupId').val(groupId);
    $('#parentId').val(parentId);
    $(window).bind("hashchange", function () {
        singleSize = parseInt(window.location.href.getQueryString("singleSize"));
        groupId = parseInt(window.location.href.getQueryString("groupId"));
        parentId = parseInt(window.location.href.getQueryString("parentId"));
        leaveSpace = parseInt(window.location.href.getQueryString("leaveSpace"));
        $('#groupId').val(groupId);
        $('#parentId').val(parentId);
    });


    $(".dragWrap").height($(window).height() - 70);

    $(".dragWrap").bind("click", function (e) {
        var target = $(e.target);
        if (target.is(".close_error")) {
            target.parent().remove();
        }
        if ($(".error").length == 0) {
            setTimeout(function () {
                $(".tips ").show();
            }, 100);
        }
    });
    $(".noscript").height($(window).height() - 60);
    $(window).resize(function () {
        $(".dragWrap").height($(window).height() - 90);
        $(".noscript").height($(window).height() - 60);
    });

    if (typeof($.browser.msie) != "undefined" && $.browser.msie) {
        if ($.browser.version == "10.0") {
            $(".container").show();
        } else {
            $(".noscript").show();
        }

    }
    if (typeof($.browser.chrome) != "undefined" && $.browser.chrome) {
        $(".container").show();
    }
    if (typeof($.browser.mozilla) != "undefined" && $.browser.mozilla) {
        $(".container").show();
    }
    if (typeof($.browser.safari) != "undefined" && $.browser.safari && $.browser.version.split(".")[0] >= 536) {
        $(".container").show();
    } else if ($.browser.safari && $.browser.version[0] < 6) {
        $(".noscript").show();
    }


    'use strict';
    var url = '/group/uploadResource.action';
    $('#fileupload').fileupload({
        url: '/group/uploadResource.action'
    });

    $('#fileupload').fileupload({
        url: url,
        dataType: 'json',
        autoUpload: true,
        acceptFileTypes: "",
        sequentialUploads: true,
       /* limitConcurrentUploads: 1,
        limitMultiFileUploads: 2,*/
        maxFileSize: singleSize / 1024 * 1000000,
        previewThumbnail: false,
        disableImageResize: /Android(?!.*Chrome)|Opera/
            .test(window.navigator.userAgent)

    }).on("fileuploadprogressall",function () {
            $(".tips ").hide();
        }).on("fileuploaddone", function () {
            setTimeout(function () {
                if ($(".upload-status").length == 0) {
                    parent.uploadComplete();
                    $(".tips ").show();
                }
            }, 1000)
        });
});
