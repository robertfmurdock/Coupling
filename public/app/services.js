"use strict";
var services = angular.module("coupling.services", []);

services.service("Coupling", function ($http) {
    var Coupling = this;

    var requestPlayers = function () {
        $http.get('/api/players').success(function (players) {
            Coupling.data.players = players;
        }).error(console.log);
    };

    var requestHistory = function () {
        $http.get('/api/history').success(function (history) {
            Coupling.data.history = history;
            Coupling.data.currentPairAssignments = history[0];
        }).error(console.log);
    };

    var post = function (url, player, callback) {
        var postPromise = $http.post(url, player);
        if (callback) {
            postPromise.success(callback);
        }
        postPromise.error(console.log);
    };

    this.spin = function (players) {
        post('/api/game', players, function (pairAssignmentDocument) {
            Coupling.data.currentPairAssignments = pairAssignmentDocument;
        });
    };

    this.saveCurrentPairAssignments = function () {
        post('/api/savePairs', Coupling.data.currentPairAssignments, function (updatedPairAssignmentDocument) {
            Coupling.data.currentPairAssignments = updatedPairAssignmentDocument;
        });
    };

    this.savePlayer = function (player, callback) {
        post('/api/savePlayer', player, callback);
        requestPlayers();
    };

    Coupling.data = {players: [], history: []};
    requestPlayers();
    requestHistory();
});