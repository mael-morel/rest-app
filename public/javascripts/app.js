angular.module('traces.service',['ngResource', 'ngRoute'])
angular.module('traces.controller',['ngResource', 'ngRoute'])

var app = angular.module('traces',['traces.service','traces.controller']);

//angular.module('traces', ['traces.service']).config(function ($httpProvider) {
//    $httpProvider.defaults.transformRequest = function (data) {
//        var str = [];
//        for (var p in data) {
//            data[p] !== undefined && str.push(encodeURIComponent(p) + '=' + encodeURIComponent(data[p]));
//        }
//        return str.join('&');
//    };
//   // $httpProvider.defaults.headers.put['Content-Type'] = $httpProvider.defaults.headers.post['Content-Type'] =
//     //   'application/x-www-form-urlencoded; charset=UTF-8';
//});

app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/add',      {templateUrl: 'assets/partials/add.html',  controller: 'traceCtrl'})
                  .when('/edit/:id', {templateUrl: 'assets/partials/edit.html', controller: 'traceCtrl'})
                  .when('/',         {templateUrl: 'assets/partials/list.html', controller: 'traceCtrl'});
}]);

