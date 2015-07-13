"use strict";
var services = angular.module("coupling.services", ['ngResource']);

services.service("Coupling", ['$http', '$resource', '$q', function ($http, $resource, $q) {
  var Coupling = this;

  function errorMessage(url, data, statusCode) {
    return "There was a problem with request " + url + "\n" +
      "Data: <" + data + ">\n" +
      "Status: " + statusCode;
  }

  var makeErrorHandler = function (url) {
    return function (data, statusCode) {
      var message = errorMessage(url, data, statusCode);
      console.error('ALERT!\n' + message);
    }
  };

  var post = function (url, player) {
    return $http.post(url, player)
      .error(makeErrorHandler('POST ' + url))
      .then(function (result) {
        return result.data;
      });
  };

  var httpDelete = function (url) {
    return $http.delete(url).error(makeErrorHandler(url));
  };

  var Tribe = $resource('/api/tribes/:tribeId');
  this.Tribe = Tribe;

  this.getTribes = function () {
    var url = '/api/tribes';
    return $q(function (resolve, reject) {
      Tribe.query()
        .$promise
        .catch(function (response) {
          console.info(response);
          reject(errorMessage('GET ' + url, response.data, response.status));
        }).then(function (response) {
          resolve(response);
        })
    });
  };

  this.requestHistoryPromise = function (tribeId) {
    var url = '/api/' + tribeId + '/history';
    return $q(function (resolve) {
      $http.get(url)
        .error(makeErrorHandler('GET ' + url))
        .then(function (response) {
          Coupling.data.history = response.data;
          resolve(response.data);
        });
    });
  };

  this.requestSpecificTribe = function (tribeId) {
    return Coupling.getTribes().then(function (tribes) {
      var found = _.findWhere(tribes, {
        _id: tribeId
      });
      Coupling.data.selectedTribe = found;
      return $q(function (resolve, reject) {
        if (found) {
          resolve(found);
        } else {
          reject("Tribe not found")
        }
      });
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
          .error(function (data, statusCode) {
            var message = errorMessage(url, data, statusCode);
            console.error('ALERT!\n' + message);
            return message;
          }).then(function (response) {
            return response.data;
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
    var postPromise = $http.post(url, players);
    postPromise.error(makeErrorHandler('POST ' + url));
    return postPromise.then(function (result) {
      return result.data;
    });
  };

  this.saveCurrentPairAssignments = function (tribeId, pairAssignments) {
    var url = '/api/' + tribeId + '/history';
    var postPromise = $http.post(url, pairAssignments);
    postPromise.error(makeErrorHandler('POST ' + url));
    return postPromise.then(function (result) {
      return result.data;
    });
  };

  this.savePlayer = function (player) {
    return post('/api/' + player.tribe + '/players', player);
  };

  this.removePlayer = function (player) {
    return httpDelete('/api/' + Coupling.data.selectedTribeId + '/players/' + player._id);
  };

  this.newTribe = function(){
    return new Tribe();
  };

  this.saveTribe = function (tribe) {
    return tribe.$save();
    //return post('/api/tribes', tribe);
  };

  this.promisePins = function (tribeId) {
    return $q(function (resolve, reject) {
      var url = '/api/' + tribeId + '/pins';
      $http.get(url)
        .error(function (data, status) {
          reject(errorMessage('GET ' + url, data, status));
        })
        .then(function (response) {
          return resolve(response.data);
        });
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