"use strict";
var services = angular.module("coupling.services", []);

services.service("Coupling", ['$http', function ($http) {
  var Coupling = this;

  function errorMessage(url, data, statusCode) {
    return "There was a problem with request " + url + "\n" +
      "Data: <" + data + ">\n" +
      "Status: " + statusCode;
  }

  var makeErrorHandler = function (url) {
    return function (data, statusCode, headers, config) {
      var message = errorMessage(url, data, statusCode);
      console.error('ALERT!\n' + message);
      // alert(message);
    }
  };

  var requestTribes = function () {
    return new RSVP.Promise(function (resolve, reject) {
      var url = '/api/tribes';
      $http.get(url).success(function (tribes) {
        resolve(tribes);
      }).error(function (data, statusCode) {
        reject(errorMessage('GET ' + url, data, statusCode));
      });
    });
  };

  var requestPlayers = function (tribeId, callback) {
    var url = '/api/' + tribeId + '/players';
    $http.get(url).success(function (players) {
      Coupling.data.players = players;
      if (callback) {
        callback(players);
      }
    }).error(makeErrorHandler('GET ' + url));
  };

  var requestHistory = function (tribeId, callback) {
    var url = '/api/' + tribeId + '/history';
    $http.get(url).success(function (history) {
      Coupling.data.history = history;
      if (callback) {
        callback(history);
      }
    }).error(makeErrorHandler('GET ' + url));
  };

  var post = function (url, player, callback) {
    var postPromise = $http.post(url, player);
    if (callback) {
      postPromise.success(callback);
    }
    postPromise.error(makeErrorHandler('POST ' + url));
  };

  var httpDelete = function (url, callback) {
    var postPromise = $http.delete(url);
    if (callback) {
      postPromise.success(callback);
    }
    postPromise.error(makeErrorHandler(url));
  };

  this.spin = function (players) {
    post('/api/' + Coupling.data.selectedTribeId + '/spin', players, function (pairAssignmentDocument) {
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

  this.selectTribe = function (tribeId) {
    Coupling.data.selectedTribeId = tribeId;
    Coupling.data.players = null;
    Coupling.data.currentPairAssignments = null;
    Coupling.data.history = null;

    if (tribeId == null) {
      return new RSVP.Promise(function (resolve) {
        resolve({selectedTribe: null, players: null, history: null});
      });
    } else {
      return RSVP.hash({
        selectedTribe: Coupling.getTribes().then(function (tribes) {
          var found = _.findWhere(tribes, {
            _id: tribeId
          });
          Coupling.data.selectedTribe = found;
          return new RSVP.Promise(function (resolve, reject) {
            if (found) {
              resolve(found);
            } else {
              reject("Tribe not found")
            }
          });
        }),
        players: new RSVP.Promise(function (resolve, reject) {
          requestPlayers(tribeId, function (players) {
            Coupling.data.players = players;
            resolve(players);
          });
        }),
        history: new RSVP.Promise(function (resolve, reject) {
          requestHistory(tribeId, function (history) {
            Coupling.data.history = history;
            resolve(history);
          });
        })
      });
    }
  };

  this.getTribes = function () {
    return requestTribes();
  };

  this.saveTribe = function (tribe, callback) {
    post('/api/tribes', tribe, callback);
  };

  this.promisePins = function (tribeId) {
    return new RSVP.Promise(function (resolve, reject) {
      var url = '/api/' + tribeId + '/pins';
      $http.get(url)
        .error(function (data, status) {
          reject(errorMessage('GET ' +url, data, status));
        })
        .then(function (response) {
          return resolve(response.data);
        });
    });
  };
  this.findPlayerById = function (id, callback) {
    requestPlayers(Coupling.data.selectedTribeId, function (players) {
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

services.service('randomizer', function () {
  this.next = function (maxValue) {
    var floatValue = Math.random() * maxValue;
    return Math.round(floatValue);
  }
});