"use strict";
var controllers = angular.module('coupling.controllers', []);

controllers.controller('CouplingController', ['$scope', '$http', '$location', function (scope, http, location) {
    var formatDate = function (date) {
        return date.getMonth() + 1 + '/' + date.getDate() + "/" + date.getFullYear();
    };

    function putPairAssignmentDocumentOnScope(pairAssignmentDocument) {
        scope.formattedDate = formatDate(new Date(pairAssignmentDocument.date));
        scope.pairAssignmentDocument = pairAssignmentDocument;
    }

    scope.spin = function () {
        location.path("/pairAssignments/new");

        http.get('/api/game').success(function (pairAssignmentDocument) {
            putPairAssignmentDocumentOnScope(pairAssignmentDocument);
        }).error(function (error) {
            console.log(error)
        });
    };

    scope.save = function () {
        var postPromise = http.post('/api/savePairs', scope.pairAssignmentDocument);
        postPromise.success(function (updatedPairAssignmentDocument) {
            putPairAssignmentDocumentOnScope(updatedPairAssignmentDocument);
        });
        postPromise.error(function (error) {
            console.log(error);
        });
    }
}]);

controllers.controller('PairAssignmentsController', ['$scope', '$routeParams', function (scope, params) {
    console.info(params.pairAssignmentsId);
}]);