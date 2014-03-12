angular.module('traces.service').factory('Rest', ['$resource', function ($resource) {
    return $resource('iti/:id', {}, {
        query:  {method: 'GET', isArray: true},
        get:    {method: 'GET', params: {id: ':id'}},
        remove: {method: 'DELETE', params: {id: ':id'}},
        edit:   {method: 'PUT', params: {id: ':id'}},
        add:    {method: 'POST', headers: { 'Content-Type': 'application/json' }}
    });
}]);