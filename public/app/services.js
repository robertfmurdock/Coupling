"use strict";
var services = angular.module("coupling.services", []);

services.service("Coupling", function ($http) {
    var Coupling = this;

    var requestPlayers = function () {
        $http.get('/api/players').success(function (players) {
            Coupling.data.players = players;
        }).error(console.log);
    };

    this.spin = function (players, callback) {
        var getPromise = $http.post('/api/game', players);
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

    this.savePlayer = function (player, callback) {
        callback = callback ? callback : function () {
        };
        var postPromise = $http.post('/api/savePlayer', player);
        postPromise.success(callback).error(console.log);
        requestPlayers();
    };

    Coupling.data = {players: []};
    requestPlayers();

    this.getHistory = function (callback) {
        $http.get('/api/history').success(callback).error(console.log);
    };
});