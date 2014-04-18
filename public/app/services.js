"use strict";
var services = angular.module("coupling.services", []);

services.service("Coupling", function ($http) {
    var Coupling = this;
    this.spin = function (callback) {
        var getPromise = $http.get('/api/game');
        getPromise.success(function (pairAssignmentDocument) {
            Coupling.currentPairAssignments = pairAssignmentDocument;
            callback();
        }).error(console.log);
    };

    this.saveCurrentPairAssignments = function (callback) {
        var postPromise = $http.post('/api/savePairs', Coupling.currentPairAssignments);
        postPromise.success(function (updatedPairAssignmentDocument) {
            Coupling.currentPairAssignments = updatedPairAssignmentDocument;
            callback();
        }).error(console.log);
    };

    this.getPlayers = function (callback) {
        var getPlayersPromise = $http.get('/api/players');
        getPlayersPromise.success(function (players) {
            callback(players);
        }).error(console.log);
    }
});