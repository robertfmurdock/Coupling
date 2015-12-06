/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
var Player = (function () {
    function Player() {
    }
    return Player;
})();
var CouplingData = (function () {
    function CouplingData() {
    }
    return CouplingData;
})();
var PairSet = (function () {
    function PairSet() {
    }
    return PairSet;
})();
var Pin = (function () {
    function Pin() {
    }
    return Pin;
})();
var SelectablePlayer = (function () {
    function SelectablePlayer(isSelected, player) {
        this.isSelected = isSelected;
        this.player = player;
    }
    return SelectablePlayer;
})();
var Coupling = (function () {
    function Coupling($http, $q, $resource) {
        this.$http = $http;
        this.$q = $q;
        this.Tribe = Coupling.buildTribeResource($resource);
        this.data = new CouplingData();
        this.data.selectedTribeId = '';
        this.data.selectablePlayers = {};
    }
    Coupling.buildTribeResource = function ($resource) {
        return $resource('/api/tribes/:tribeId');
    };
    Coupling.errorMessage = function (url, data, statusCode) {
        return "There was a problem with request " + url + "\n" +
            "Data: <" + data + ">\n" +
            "Status: " + statusCode;
    };
    Coupling.prototype.logAndRejectError = function (url) {
        var self = this;
        return function (response) {
            var data = response.data;
            var statusCode = response.status;
            var message = Coupling.errorMessage(url, data, statusCode);
            console.error('ALERT!\n' + message);
            return self.$q.reject(message);
        };
    };
    Coupling.prototype.post = function (url, object) {
        return this.$http.post(url, object)
            .then(function (result) {
            return result.data;
        }, this.logAndRejectError('POST ' + url));
    };
    Coupling.prototype.httpDelete = function (url) {
        return this.$http.delete(url)
            .then(function () {
        }, this.logAndRejectError(url));
    };
    Coupling.prototype.getTribes = function () {
        var url = '/api/tribes';
        var self = this;
        return this.Tribe.query().$promise
            .catch(function (response) {
            console.info(response);
            return self.$q.reject(Coupling.errorMessage('GET ' + url, response.data, response.status));
        });
    };
    Coupling.prototype.getHistory = function (tribeId) {
        var url = '/api/' + tribeId + '/history';
        var self = this;
        return this.$http.get(url)
            .then(function (response) {
            self.data.history = response.data;
            return response.data;
        }, this.logAndRejectError('POST ' + url));
    };
    Coupling.prototype.requestSpecificTribe = function (tribeId) {
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
        });
    };
    Coupling.prototype.isInLastSetOfPairs = function (player, history) {
        var result = _.find(history[0].pairs, function (pairset) {
            if (_.findWhere(pairset, {
                _id: player._id
            })) {
                return true;
            }
        });
        return !!result;
    };
    Coupling.prototype.getSelectedPlayers = function (players, history) {
        var _this = this;
        var selectablePlayers = _.map(players, function (player) {
            var selected = _this.playerShouldBeSelected(player, history);
            return [player._id, new SelectablePlayer(selected, player)];
        });
        this.data.selectablePlayers = _.object(selectablePlayers);
        return this.data.selectablePlayers;
    };
    Coupling.prototype.playerShouldBeSelected = function (player, history) {
        if (this.data.selectablePlayers[player._id]) {
            return this.data.selectablePlayers[player._id].isSelected;
        }
        else if (history.length > 0) {
            return this.isInLastSetOfPairs(player, history);
        }
        else {
            return true;
        }
    };
    Coupling.prototype.getPlayers = function (tribeId) {
        var url = '/api/' + tribeId + '/players';
        var self = this;
        return this.$http.get(url)
            .then(function (response) {
            return response.data;
        }, function (response) {
            var data = response.data;
            var statusCode = response.status;
            var message = Coupling.errorMessage(url, data, statusCode);
            console.error('ALERT!\n' + message);
            return self.$q.reject(message);
        });
    };
    Coupling.prototype.spin = function (players, tribeId) {
        var url = '/api/' + tribeId + '/spin';
        return this.$http.post(url, players)
            .then(function (result) {
            return result.data;
        }, this.logAndRejectError('POST ' + url));
    };
    Coupling.prototype.saveCurrentPairAssignments = function (tribeId, pairAssignments) {
        var url = '/api/' + tribeId + '/history';
        return this.$http.post(url, pairAssignments)
            .then(function (result) {
            return result.data;
        }, this.logAndRejectError('POST ' + url));
    };
    Coupling.prototype.savePlayer = function (player) {
        return this.post('/api/' + player.tribe + '/players', player);
    };
    Coupling.prototype.removePlayer = function (player) {
        return this.httpDelete('/api/' + player.tribe + '/players/' + player._id);
    };
    Coupling.prototype.getPins = function (tribeId) {
        var url = '/api/' + tribeId + '/pins';
        var self = this;
        return this.$http.get(url)
            .then(function (response) {
            return response.data;
        }, function (response) {
            var data = response.data;
            var status = response.status;
            return self.$q.reject(Coupling.errorMessage('GET ' + url, data, status));
        });
    };
    Coupling.$inject = ['$http', '$q', '$resource'];
    return Coupling;
})();
var Randomizer = (function () {
    function Randomizer() {
    }
    Randomizer.prototype.next = function (maxValue) {
        var floatValue = Math.random() * maxValue;
        return Math.round(floatValue);
    };
    return Randomizer;
})();
angular.module("coupling.services", ['ngResource'])
    .service("Coupling", Coupling)
    .service('randomizer', Randomizer);
//# sourceMappingURL=services.js.map