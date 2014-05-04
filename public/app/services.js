"use strict";
var services = angular.module("coupling.services", []);

services.service("Coupling", function ($http) {
    var Coupling = this;
    var makeErrorHandler = function (url) {
        return function (data, statusCode) {
            alert("There was a problem loading " + url + " " + data + " Got status code: " + statusCode);
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

    var requestHistory = function (tribeId, callback) {
        var url = '/api/' + tribeId + '/history';
        $http.get(url).success(function (history) {
            Coupling.data.history = history;
            if (callback) {
                callback(history);
            }
        }).error(makeErrorHandler(url));
    };

    var post = function (url, player, callback) {
        var postPromise = $http.post(url, player);
        if (callback) {
            postPromise.success(callback);
        }
        postPromise.error(makeErrorHandler(url));
    };

    var httpDelete = function (url, callback) {
        var postPromise = $http.delete(url);
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
        requestPlayers(Coupling.data.selectedTribeId);
    };

    this.removePlayer = function (player, callback) {
        httpDelete('/api/' + Coupling.data.selectedTribeId + '/players/' + player._id, callback);
        requestPlayers(Coupling.data.selectedTribeId);
    };

    this.selectTribe = function (tribeId, callbackWhenComplete) {
        var shouldReload = Coupling.data.selectedTribeId != tribeId || Coupling.data.players == null;
        if (shouldReload) {
            Coupling.data.selectedTribeId = tribeId;
            Coupling.data.selectedTribe = _.findWhere(Coupling.data.tribes, {_id: tribeId});
            Coupling.data.players = null;
            Coupling.data.currentPairAssignments = null;
            Coupling.data.history = null;
            if (tribeId != null) {
                requestPlayers(tribeId, function (players) {
                    requestHistory(tribeId, function (history) {
                        callbackWhenComplete(players, history);
                    });
                });
            }
        } else if (callbackWhenComplete) {
            callbackWhenComplete();
        }
    };

    this.saveTribe = function (tribe, callback) {
        post('/api/tribes', tribe, callback);
    };

    this.findPlayerById = function (id, callback) {
        requestPlayers(Coupling.data.selectedTribeId, function (players) {
            callback(_.findWhere(players, {_id: id}));
        });
    };

    Coupling.data = {players: null, history: null, tribes: null};
    requestTribes();
});