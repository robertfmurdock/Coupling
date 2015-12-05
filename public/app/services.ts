/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />

class Player {
    _id:string;
    isAvailable:boolean
}

class CouplingData {
    players:[Player];
    history:[PairSet];
    selectedTribe:Tribe;
    selectedTribeId:String
}

class Tribe {
}

class PairSet {
}

class Coupling {
    static $inject = ['$http', '$resource', '$q'];

    data:CouplingData;
    Tribe:ng.resource.IResourceClass<Tribe>;

    constructor(public $http:angular.IHttpService, public $resource:ng.resource.IResourceService, public $q:angular.IQService) {
        this.Tribe = $resource('/api/tribes/:tribeId');
        this.data =  {
            players: null,
            history: null,
            selectedTribe: null,
            selectedTribeId: ''
        };
    }

    private static errorMessage(url, data, statusCode) {
        return "There was a problem with request " + url + "\n" +
            "Data: <" + data + ">\n" +
            "Status: " + statusCode;
    }

    private logAndRejectError(url) {
        var self = this;
        return function (response) {
            var data = response.data;
            var statusCode = response.status;
            var message = Coupling.errorMessage(url, data, statusCode);
            console.error('ALERT!\n' + message);
            return self.$q.reject(message);
        }
    }

    private post(url, player) {
        return this.$http.post(url, player)
            .then(function (result) {
                return result.data;
            },
            this.logAndRejectError('POST ' + url));
    }

    private httpDelete(url) {
        return this.$http.delete(url)
            .then(function () {
            },
            this.logAndRejectError(url));
    }

    getTribes() {
        var url = '/api/tribes';
        var self = this;
        return this.Tribe.query().$promise
            .catch(function (response) {
                console.info(response);
                return self.$q.reject(Coupling.errorMessage('GET ' + url, response.data, response.status));
            });
    }

    requestHistoryPromise(tribeId) {
        var url = '/api/' + tribeId + '/history';
        var self = this;
        return this.$http.get(url)
            .then((response:angular.IHttpPromiseCallbackArg<[PairSet]>) => {
                self.data.history = response.data;
                return response.data;
            },
            this.logAndRejectError('POST ' + url));
    }

    requestSpecificTribe(tribeId) {
        var self = this;
        return this.getTribes()
            .then(function (tribes) {
                var found = _.findWhere(tribes, {
                    _id: tribeId
                });
                self.data.selectedTribe = found;
                if (!found) {
                    return self.$q.reject("Tribe not found");
                }
                return found;
            })
    }

    private isInLastSetOfPairs(player, history) {
        var result = _.find(history[0].pairs, function (pairset:[{}]) {
            if (_.findWhere(pairset, {
                    _id: player._id
                })) {
                return true;
            }
        });
        return !!result;
    }

    requestPlayersPromise(tribeId, historyPromise:angular.IPromise<[PairSet]>) {
        var url = '/api/' + tribeId + '/players';
        var self = this;
        return this.$q.all({
                players: this.$http.get(url)
                    .then(function (response:angular.IHttpPromiseCallbackArg<[Player]>) {
                        return response.data;
                    },
                    function (response) {
                        var data = response.data;
                        var statusCode = response.status;
                        var message = Coupling.errorMessage(url, data, statusCode);
                        console.error('ALERT!\n' + message);
                        return self.$q.reject(message);
                    }),
                history: historyPromise
            }
        ).then(function (data:any) {
                var players:[Player] = data.players;
                var history:[PairSet] = data.history;
                _.each(players, function (player) {
                    if (history.length == 0) {
                        player.isAvailable = true;
                    } else {
                        player.isAvailable = self.isInLastSetOfPairs(player, history);
                    }
                });
                _.each(self.data.players, function (originalPlayer:Player) {
                    var newPlayer = _.findWhere(players, {
                        _id: originalPlayer._id
                    });
                    if (newPlayer) {
                        newPlayer.isAvailable = originalPlayer.isAvailable;
                    }
                });
                self.data.players = players;
                return players;
            })
    }

    spin(players, tribeId) {
        var url = '/api/' + tribeId + '/spin';
        return this.$http.post(url, players)
            .then(function (result) {
                return result.data;
            },
            this.logAndRejectError('POST ' + url));
    }

    saveCurrentPairAssignments(tribeId, pairAssignments) {
        var url = '/api/' + tribeId + '/history';
        return this.$http.post(url, pairAssignments)
            .then(function (result) {
                return result.data;
            },
            this.logAndRejectError('POST ' + url));
    }

    savePlayer(player) {
        return this.post('/api/' + player.tribe + '/players', player);
    }

    removePlayer(player) {
        return this.httpDelete('/api/' + this.data.selectedTribeId + '/players/' + player._id);
    }

    newTribe() {
        return new Tribe();
    }

    saveTribe(tribe) {
        return tribe.$save();
    }

    promisePins(tribeId) {
        var url = '/api/' + tribeId + '/pins';
        var self = this;
        return this.$http.get(url)
            .then(function (response) {
                return response.data;
            },
            function (response) {
                var data = response.data;
                var status = response.status;
                return self.$q.reject(Coupling.errorMessage('GET ' + url, data, status));
            });
    }
}

var services = angular.module("coupling.services", ['ngResource']);

services.service("Coupling", Coupling);

services.service('randomizer', function () {
    this.next = function (maxValue) {
        var floatValue = Math.random() * maxValue;
        return Math.round(floatValue);
    }
});