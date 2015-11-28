"use strict";
var services = angular.module("coupling.services", ['ngResource']);

services.service("Coupling", ['$http', '$resource', '$q', function ($http, $resource, $q) {
  var Coupling = this;

  function errorMessage(url, data, statusCode) {
    return "There was a problem with request " + url + "\n" +
      "Data: <" + data + ">\n" +
      "Status: " + statusCode;
  }

  var logAndRejectError = function (url) {
    return function (response) {
      var data = response.data;
      var statusCode = response.status;
      var message = errorMessage(url, data, statusCode);
      console.error('ALERT!\n' + message);
      return $q.reject(message);
    }
  };

  var post = function (url, player) {
    return $http.post(url, player)
      .then(function (result) {
        return result.data;
      },
      logAndRejectError('POST ' + url));
  };

  var httpDelete = function (url) {
    return $http.delete(url)
      .then(function () {
      },
      logAndRejectError(url));
  };

  var Tribe = $resource('/api/tribes/:tribeId');
  this.Tribe = Tribe;

  this.getTribes = function () {
    var url = '/api/tribes';
    return Tribe.query().$promise
      .catch(function (response) {
        console.info(response);
        return $q.reject(errorMessage('GET ' + url, response.data, response.status));
      });
  };

  this.requestHistoryPromise = function (tribeId) {
    var url = '/api/' + tribeId + '/history';
    return $http.get(url)
      .then(function (response) {
        Coupling.data.history = response.data;
        return response.data;
      },
      logAndRejectError('POST ' + url));
  };

  this.requestSpecificTribe = function (tribeId) {
    return Coupling.getTribes()
      .then(function (tribes) {
        var found = _.findWhere(tribes, {
          _id: tribeId
        });
        Coupling.data.selectedTribe = found;
        if (!found) {
          return $q.reject("Tribe not found");
        }
        return found;
      })
  };

  var isInLastSetOfPairs = function (player, history) {
    var result = _.find(history[0].pairs, function (pairset) {
      if (_.findWhere(pairset, {
          _id: player._id
        })) {
        return true;
      }
    });
    return !!result;
  };

  this.requestPlayersPromise = function (tribeId, historyPromise) {
    var url = '/api/' + tribeId + '/players';
    return $q.all({
        players: $http.get(url)
          .then(function (response) {
            return response.data;
          },
          function (response) {
            var data = response.data;
            var statusCode = response.status;
            var message = errorMessage(url, data, statusCode);
            console.error('ALERT!\n' + message);
            return $q.reject(message);
          }),
        history: historyPromise
      }
    ).then(function (data) {
        var players = data.players;
        var history = data.history;
        _.each(players, function (player) {
          if (history.length == 0) {
            player.isAvailable = true;
          } else {
            player.isAvailable = isInLastSetOfPairs(player, history);
          }
        });
        _.each(Coupling.data.players, function (originalPlayer) {
          var newPlayer = _.findWhere(players, {
            _id: originalPlayer._id
          });
          if (newPlayer) {
            newPlayer.isAvailable = originalPlayer.isAvailable;
          }
        });
        Coupling.data.players = players;
        return players;
      })
  };

  this.spin = function (players, tribeId) {
    var url = '/api/' + tribeId + '/spin';
    return $http.post(url, players)
      .then(function (result) {
        return result.data;
      },
      logAndRejectError('POST ' + url));
  };

  this.saveCurrentPairAssignments = function (tribeId, pairAssignments) {
    var url = '/api/' + tribeId + '/history';
    return $http.post(url, pairAssignments)
      .then(function (result) {
        return result.data;
      },
      logAndRejectError('POST ' + url));
  };

  this.savePlayer = function (player) {
    return post('/api/' + player.tribe + '/players', player);
  };

  this.removePlayer = function (player) {
    return httpDelete('/api/' + Coupling.data.selectedTribeId + '/players/' + player._id);
  };

  this.newTribe = function () {
    return new Tribe();
  };

  this.saveTribe = function (tribe) {
    return tribe.$save();
  };

  this.promisePins = function (tribeId) {
    var url = '/api/' + tribeId + '/pins';
    return $http.get(url)
      .then(function (response) {
        return response.data;
      },
      function (response) {
        var data = response.data;
        var status = response.status;
        return $q.reject(errorMessage('GET ' + url, data, status));
      });
  };
  Coupling.data = {
    players: null,
    history: null
  };
}])
;

services.service('randomizer', function () {
  this.next = function (maxValue) {
    var floatValue = Math.random() * maxValue;
    return Math.round(floatValue);
  }
});