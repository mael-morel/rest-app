angular.module('traces.controller').controller('traceCtrl',['$scope','$resource','$routeParams','$location', 'Rest', function ($scope, $resource, $routeParams, $location, Rest) {

    if ($routeParams.id) {
        $scope.trace = Rest.get({id: $routeParams.id});
    }

    if ($location.path() === '/') {
        $scope.traces = Rest.query();
    }

    $scope.add = function () {
        Rest.add({},$scope.newTrace, function (data) {
            $location.path('/');
        });
    };

    $scope.delete = function (trace) {
        if (!confirm('Confirm delete')) {
            return;
        }

        Rest.remove({id: trace.id.$oid}, {}, function () {
            $location.path('/');
        });
    };

    $scope.save = function (trace) {
        Rest.edit({id: trace.id.$oid}, $scope.trace, function () {
            $location.path('/');
        });
    };
}]);