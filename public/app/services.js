"use strict";
var services = angular.module("coupling.services", []);

services.service("Coupling", function ($http) {
    var Coupling = this;
    var makeErrorHandler = function (url) {
        return function (data, status, headers, config) {
            alert("There was a problem loading " + url + data + " " + status + " " + headers + " " + config);
            console.log(data);
        }
    };

    var requestTribes = function (callback) {
        var url = '/api/tribes';
        $http.get(url).success(function (tribes) {
            Coupling.data.tribes = tribes;
            if (callback) {
                callback(tribes);
            }
        }).error(makeErrorHandler(url));
    };

    var requestPlayers = function (tribeId, callback) {
        var url = '/api/' + tribeId + '/players';
        $http.get(url).success(function (players) {
            Coupling.data.players = players;
            if (callback) {
                callback(players);
            }
        }).error(makeErrorHandler(url));
    };

    var requestHistory = function (tribeId) {
        var url = '/api/' + tribeId + '/history';
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
        post('/api/' + Coupling.data.selectedTribeId + '/game', players, function (pairAssignmentDocument) {
            Coupling.data.currentPairAssignments = pairAssignmentDocument;
        });
    };

    this.saveCurrentPairAssignments = function () {
        post('/api/' + Coupling.data.selectedTribeId + '/history', Coupling.data.currentPairAssignments, function (updatedPairAssignmentDocument) {
            Coupling.data.currentPairAssignments = updatedPairAssignmentDocument;
        });
    };

    this.savePlayer = function (player, callback) {
        post('/api/' + Coupling.data.selectedTribeId + '/players', player, callback);
        requestPlayers();
    };

    this.selectTribe = function (tribeId, callbackWhenComplete) {
        var shouldReload = Coupling.data.selectedTribeId != tribeId || Coupling.data.players == null;
        if (shouldReload) {
            Coupling.data.selectedTribeId = tribeId;
            Coupling.data.players = null;
            Coupling.data.currentPairAssignments = null;
            Coupling.data.history = [];
            requestPlayers(tribeId, callbackWhenComplete);
            requestHistory(tribeId, callbackWhenComplete);
        } else {
            callbackWhenComplete();
        }
    };

    this.findPlayerById = function (id, callback) {
        requestPlayers(Coupling.data.selectedTribeId, function (players) {
            callback(_.findWhere(players, {_id: id}));
        });
    };

    Coupling.data = {players: null, history: [], tribes: []};
    requestPlayers();
    requestHistory();
    requestTribes();
});