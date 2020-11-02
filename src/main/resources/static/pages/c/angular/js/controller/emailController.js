/**
 * Created by lfn on 2015/12/2.
 */
angular.module("emailController", ["am.modal", "ng.ueditor"])
  .controller('emailController', ["$scope", "$rootScope", '$modalInstance', 'items', "httpRequest.sendRequest", function ($scope, $rootScope, $modalInstance, items, sendRequest) {
    $scope.config = {
      elementPathEnabled: false,
      initialFrameHeight: 100,
      scaleEnabled: true,
      toolbars: [
        ['bold', 'italic', 'underline', 'forecolor', 'backcolor', 'fontsize', 'fontfamily', 'justifyleft', 'justifyright',
          'justifycenter', 'justifyjustify', 'insertorderedlist', 'insertunorderedlist', 'formatmatch', 'blockquote', 'source']
      ]
    };
    $scope.data = {};
    $scope.data.emailContent = "";
    $scope.pagingOptions = {
      pageSize: 30,
      currentPage: 1,
      start: 0,
      totalPage: 0
    };


    $scope.data.sender = $rootScope.status.name;
    $scope.data.EmailTitle = "";
    $scope.data.recipient = "";
    $scope.emailfile = [];
    $scope.emailfile = items.slice();
    $scope.getAccounts = function (pageSize, page) {
      sendRequest("/user/getAccounts.action",
        { limit: pageSize, start: page }, function (data) {
          $scope.accounts = data.accounts;
          $scope.totalItems = data.totalCount;
          $scope.pagingOptions.totalPage = Math.ceil($scope.totalItems / $scope.pagingOptions.pageSize);
        });
    };
    $scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
      if (newVal !== oldVal && $scope.pagingOptions.currentPage > 0) {
        $scope.getAccounts($scope.pagingOptions.pageSize, ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize);
      }
    });
    $scope.searchBtn = function () {
      if (document.getElementById("searchPerson").value == "") {
        $scope.getAccounts($scope.pagingOptions.pageSize, 0);
      } else {
        sendRequest("/user/advancedSearch.action",
          { account: document.getElementById("searchPerson").value, limit: 30, start: 0 }, function (data) {
            $scope.accounts = data.accounts;
            $scope.totalItems = data.totalCount;
            $scope.pagingOptions.totalPage = Math.ceil($scope.totalItems / $scope.pagingOptions.pageSize);
          });
      }
    };
    $scope.selectRecipient = function (x) {
      if (x.email) {
        if ($scope.data.recipient.indexOf(x.email) >= 0) {
          toastr["info"]("该用户已添加！");
        } else {
          $scope.data.recipient = $scope.data.recipient + x.email + ";";
        }

      } else    toastr["info"]("该用户未设置邮箱！");
    };
    $scope.deleteBtn = function (x) {
      $scope.emailfile.splice(x, 1);
    };
    $scope.sent = function () {
      if ($scope.data.recipient == "") {
        toastr["warning"]("请添加收件人！");
      }
      else if ($scope.emailfile.length == 0) {
        toastr["warning"]("请添加文件！");
      }
      else {
        var fileId = [];
        var num = [];
        for (var x in $scope.emailfile) {
          fileId.push($scope.emailfile[x].id);
          num.push(1);
        }
        if ($scope.data.EmailTitle == "") $scope.data.EmailTitle = $scope.data.sender + "向您共享了资源";
        // $rootScope.progressbar.start();
        sendRequest("/webmail/sendEmail.action",
          {
            id: fileId,
            setCode: num,
            sender: $scope.data.sender,
            recipient: $scope.data.recipient,
            title: $scope.data.EmailTitle,
            content: $scope.data.emailContent
          }, function (data) {
            // $rootScope.progressbar.complete();
            if (data.type == "success") {
              toastr["success"]("发送成功！");
            } else   toastr["error"]("发送失败！");
          });
        $modalInstance.close();
      }
    };
    $scope.cancel = function () {
      $modalInstance.close();//或者$scope.$close()
    };
    $scope.prev = function () {
      if ($scope.pagingOptions.currentPage <= 1)
        return false;
      $scope.pagingOptions.currentPage--;
    };
    $scope.next = function () {
      if ($scope.pagingOptions.currentPage == $scope.pagingOptions.totalPage)
        return false;
      $scope.pagingOptions.currentPage++;
    };
    $scope.first = function () {
      $scope.pagingOptions.currentPage = 1;
    };
    $scope.end = function () {
      $scope.pagingOptions.currentPage = $scope.pagingOptions.totalPage;
    };
    $scope.getAccounts($scope.pagingOptions.pageSize, 0);
  }])
  .filter('displayName', function () {
    return function (displayName) {
      displayName = displayName.replace(/(&nbsp;)/g, " ");
      return displayName;
    }
  }).filter('fileicon', function ($rootScope) {
  return function (icon) {
    if (icon.type == 1) {
      return "images/folder.png"
    } else if (icon.type == 2) {
      var end = icon.displayName.substring(icon.displayName.lastIndexOf(".") + 1,
        icon.displayName.length).toLowerCase();
      if (end == "zip" || end == "rar" || end == ("jar") || end == ("7z") || end == ("gz") || end == ("iso")) {
        return "images/zip.png";
      } else if (end == ("jpg") || end == ("gif") || end == ("png") || end == ("jpeg") || end == ("bmp")) {
        if ($rootScope.thumbnail.length > 0) {
          for (var x in $rootScope.thumbnail) {
            if (icon.id == $rootScope.thumbnail[x].id) {
              return $rootScope.path + $rootScope.thumbnail[x].thumbUrl;
            }
          }
        }
        else return "images/image.png";
      } else if (end == ("m4a") || end == ("mp3") || end == ("mid") || end == ("xmf") || end == ("ogg") || end == ("wav")) {
        return "images/audio.png";
      } else if (end == "txt") {
        return "images/txt.png";
      } else if (end == ("pdf")) {
        return "images/pdf.png";
      } else if (end == ("doc") || end == ("docx")) {
        return "images/doc.png";
      } else if (end == ("xls") || end == ("xlsx")) {
        return "images/xls.png";
      } else if (end == ("ppt") || end == ("pptx")) {
        return "images/ppt.png";
      } else if (end == ("apk")) {
        return "images/apk.png";
      } else if (end == ("3gp") || end == ("mp4") || end == ("avi") || end == ("rmvb") || end == ("wmv") || end == ("mkv") || end == ("flv") || end == ("mpg")) {
        return "images/video.png"
      } else if (end == ("exe")) {
        return "images/exe.png";
      }
      else    return "images/default.png";
    }
    else if (icon.type == 3) {
      return "images/link.png";
    }
    else return "images/default.png";
  }
});