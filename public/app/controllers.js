"use strict";
var controllers = angular.module('coupling.controllers', []);

controllers.controller('CouplingController', ['$scope', '$http', '$location', function (scope, http, location) {
    var formatDate = function (date) {
        return date.getMonth() + 1 + '/' + date.getDate() + "/" + date.getFullYear();
    };

    function putPairAssignmentDocumentOnScope(pairAssignmentDocument) {
        scope.date = formatDate(new Date(pairAssignmentDocument.date));
        scope.pairs = pairAssignmentDocument.pairs;
        scope.pairAssignmentDocument = pairAssignmentDocument;
    }

    scope.spin = function () {
        http.get('/api/game').success(function (pairAssignmentDocument) {
            putPairAssignmentDocumentOnScope(pairAssignmentDocument);
        }).error(function (error) {
            console.log(error)
        });
        location.path("/pairAssignments");
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

controllers.controller('PairAssignmentsController', ['$scope', function (scope) {
}]);

