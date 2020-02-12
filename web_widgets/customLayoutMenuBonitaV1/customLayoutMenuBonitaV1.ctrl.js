function WidgetlivingApplicationMenuController($scope, $http, $window, $location, $timeout, modalService) {
    var ctrl = this;
    
    function parseCurrentURL() {
        var pathArray = $window.location.pathname.split( '/' );
        ctrl.applicationToken = pathArray[pathArray.length-3];
        ctrl.pageToken = pathArray[pathArray.length-2];
    }
    
    //handle the browser back button
    $window.addEventListener('popstate', function(e) {
        parseCurrentURL();
        //make sure the user is still logged in before refreshing the iframe
        getSession().then(setTargetedUrl, refreshPage);
    });
    
    function getApplication() {
        return $http.get('../API/living/application/?c=1&f=token%3D'+ctrl.applicationToken);
    }
    
    function getSession() {
        return $http.get('../API/system/session/unusedId');
    }
    
    this.filterChildren = function (parentId) {
        return (ctrl.applicationMenuList||[]).filter(function(menu){
            return menu.parentMenuId === '' + parentId;
        });
        
    }
   
    function setApplicationMenuList(application) {
        
        return $http.get('../API/living/application-menu/?c=100&f=applicationId%3D'+application.id+'&d=applicationPageId&o=menuIndex+ASC')
            .success(function(data) { 
                ctrl.applicationMenuList = data;
            });
    }

    function setTargetedUrl() {
      // angular hack to force the variable bound to refresh
      // so we change it's value to undefined and then delay to the correct value
      $scope.properties.targetUrl = undefined;
        $timeout(function(){
            $scope.properties.targetUrl = "../../../portal/resource/app/"+ctrl.applicationToken+"/"+ ctrl.pageToken+"/content/"+ $window.location.search + ctrl.searchSeparator() + "app=" + ctrl.applicationToken;
        }, 0);
    }
    
    function refreshPage() {
        $window.location.reload();
    }

    ctrl.searchSeparator = function() {
        return $window.location.search ? "&" : "?";
    };

    ctrl.isParentMenu = function(menu) {
        return menu.parentMenuId==-1 && menu.applicationPageId==-1;
    };
    
    ctrl.displayPage = function(token) {
        var previousToken = ctrl.pageToken;
        var previousPath = window.location.pathname;
        
        ctrl.pageToken = token;
        var urlPath = previousPath.substring(0, previousPath.length-previousToken.length-1) + token + '/' + $window.location.search;
        
        var stateObject = { title: "" + token + "", url: "" +  urlPath  + ""};
        if (typeof ($window.history.pushState) != "undefined") {
            $window.history.pushState(stateObject, stateObject.title, stateObject.url );
        } else {
            alert("Browser does not support HTML5.");
        }
        //make sure the user is still logged in before refreshing the iframe
        getSession().then(setTargetedUrl, refreshPage);
    };
    
    ctrl.openCurrentSessionModal = function() {
        modalService.open($scope.properties.currentSessionModalId);
    };
    
    ctrl.openAppSelectionModal = function() {
        modalService.open($scope.properties.appSelectionModalId);
    };
    
    parseCurrentURL();
    setTargetedUrl();
    getApplication().then(function(response) {
        var application = response.data[0];
        ctrl.applicationName = application.displayName;
        $window.document.title = application.displayName;
        setApplicationMenuList(application);
    });
    
    getSession().then(function(session) {
        var userIdentity = '../API/identity/user/' + session.data.user_id;
        return $http.get(userIdentity);
    }).then(function(identity) {
        if (identity && identity.data.firstname && identity.data.lastname) {
            ctrl.userName = identity.data.firstname + ' ' + identity.data.lastname;
        } else if (identity && identity.data.userName) {
            ctrl.userName = identity.data.userName;
        }
    });
}