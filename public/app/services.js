"use strict";
var services = angular.module("coupling.services", []);

services.service("Coupling", function ($http) {
    var Coupling = this;
    var makeErrorHandler = function (url) {
        return function (data, status, headers, config) {
            alert("There was a problem loading " + url + data + " " + status + " " + headers + " " + config);
            console.log(error);
        }
    };
    var requestPlayers = function (callback) {
        var url = '/api/players';
        $http.get(url).success(function (players) {
            Coupling.data.players = players;
            if (callback) {
                callback(players);
            }
        }).error(makeErrorHandler(url));
    };

    var requestHistory = function () {
        var url = '/api/history';
        $http.get(url).success(function (history) {
            Coupling.data.history = history;
            Coupling.data.currentPairAssignments = history[0];
        }).error(makeErrorHandler(url));
    };

    var post = function (url, player, callback) {
        var postPromise = $http.post(url, player);
        if (callback) {
            postPromise.success(callback);
        }
        postPromise.error(makeErrorHandler(url));
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

    this.findPlayerById = function (id, callback) {
        requestPlayers(function (players) {
            callback(_.findWhere(players, {_id: id}));
        });
    };

});