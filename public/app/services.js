"use strict";
var services = angular.module("coupling.services", []);

services.service("Coupling", ['$http', function($http) {
    var Coupling = this;

    function errorMessage(url, data, statusCode) {
        return "There was a problem loading " + url + "\n" +
            "Data: <" + data + ">\n" +
            "Status: " + statusCode;
    }

    var makeErrorHandler = function(url) {
        return function(data, statusCode) {
            var message = errorMessage(url, data, statusCode);
            console.log('ALERT!\n' + message);
            // alert(message);
        }
    };

    var requestTribes = function() {
        return new RSVP.Promise(function(resolve, reject) {
            var url = '/api/tribes';
            $http.get(url).success(function(tribes) {
                resolve(tribes);
            }).error(function(data, statusCode) {
                reject(errorMessage(url, data, statusCode));
            });
        });
    };

    var requestPlayers = function(tribeId, callback) {
        var url = '/api/' + tribeId + '/players';
        $http.get(url).success(function(players) {
            Coupling.data.players = players;
            if (callback) {
                callback(players);
            }
        }).error(makeErrorHandler(url));
    };

    var requestHistory = function(tribeId, callback) {
        var url = '/api/' + tribeId + '/history';
        $http.get(url).success(function(history) {
            Coupling.data.history = history;
            if (callback) {
                callback(history);
            }
        }).error(makeErrorHandler(url));
    };

    var post = function(url, player, callback) {
        var postPromise = $http.post(url, player);
        if (callback) {
            postPromise.success(callback);
        }
        postPromise.error(makeErrorHandler(url));
    };

    var httpDelete = function(url, callback) {
        var postPromise = $http.delete(url);
        if (callback) {
            postPromise.success(callback);
        }
        postPromise.error(makeErrorHandler(url));
    };

    this.spin = function(players) {
        post('/api/' + Coupling.data.selectedTribeId + '/spin', players, function(pairAssignmentDocument) {
            Coupling.data.currentPairAssignments = pairAssignmentDocument;
        });
    };

    this.saveCurrentPairAssignments = function() {
        post('/api/' + Coupling.data.selectedTribeId + '/history', Coupling.data.currentPairAssignments, function(updatedPairAssignmentDocument) {
            Coupling.data.currentPairAssignments = updatedPairAssignmentDocument;
        });
    };

    this.savePlayer = function(player, callback) {
        post('/api/' + Coupling.data.selectedTribeId + '/players', player, callback);
        requestPlayers(Coupling.data.selectedTribeId);
    };

    this.removePlayer = function(player, callback) {
        httpDelete('/api/' + Coupling.data.selectedTribeId + '/players/' + player._id, callback);
        requestPlayers(Coupling.data.selectedTribeId);
    };

    this.selectTribe = function(tribeId) {
        // var shouldReload = Coupling.data.selectedTribeId != tribeId || Coupling.data.players == null;
        // if (shouldReload) {
        //     Coupling.data.selectedTribeId = tribeId;

        //     Coupling.getTribes().then(function(tribes) {
        //         var found = _.findWhere(tribes, {
        //             _id: tribeId
        //         });
        //         if (found != undefined) {
        //             Coupling.data.selectedTribe = found;
        //         }
        //     });

        //     Coupling.data.players = null;
        //     Coupling.data.currentPairAssignments = null;
        //     Coupling.data.history = null;
        //     if (tribeId != null) {
        //         requestPlayers(tribeId, function(players) {
        //             requestHistory(tribeId, function(history) {
        //                 if (callbackWhenComplete) {
        //                     callbackWhenComplete(players, history);
        //                 }
        //             });
        //         });
        //     }
        // } else if (callbackWhenComplete) {
        //     callbackWhenComplete(Coupling.data.players, Coupling.data.history);
        // }
        Coupling.data.selectedTribeId = tribeId;
        Coupling.data.players = null;
        Coupling.data.currentPairAssignments = null;
        Coupling.data.history = null;
        return RSVP.hash({
            selectedTribe: Coupling.getTribes().then(function(tribes) {
                var found = _.findWhere(tribes, {
                    _id: tribeId
                });
                Coupling.data.selectedTribe = found;
                return new RSVP.Promise(function(resolve, reject) {
                    if (found) {
                        resolve(found);
                    } else {
                        reject("Tribe not found")
                    };
                });
            }),
            players: new RSVP.Promise(function(resolve, reject) {
                requestPlayers(tribeId, resolve);
            }).then(function(players) {
                Coupling.data.players = players;
            }),
            history: new RSVP.Promise(function(resolve, reject) {
                requestHistory(tribeId, resolve);
            }).then(function() {
                Coupling.data.history = history;
            })
        });
    };

    this.getTribes = function() {
        return requestTribes();
    };

    this.saveTribe = function(tribe, callback) {
        post('/api/tribes', tribe, callback);
    };

    this.promisePins = function(tribeId) {
        return new RSVP.Promise(function(resolve, reject) {
            var url = '/api/' + tribeId + '/pins';
            $http.get(url)
                .error(function(data, status) {
                    reject(errorMessage(url, data, status));
                })
                .then(function(response) {
                    return resolve(response.data);
                });
        });
    };
    this.findPlayerById = function(id, callback) {
        requestPlayers(Coupling.data.selectedTribeId, function(players) {
            callback(_.findWhere(players, {
                _id: id
            }));
        });
    };

    Coupling.data = {
        players: null,
        history: null
    };
}]);

services.service('randomizer', function() {
    this.next = function(maxValue) {
        var floatValue = Math.random() * maxValue;
        return Math.round(floatValue);
    }
});