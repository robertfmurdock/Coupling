"use strict";
var monk = require('monk');
var Promise = require('rsvp').Promise;
var _ = require('underscore');

var handleMongoError = function (error) {
  return {message: 'Could not read from MongoDB.', error: Error(error)};
};

var makeDocumentPromise = function (collection, options, filter) {
  return new Promise(function (resolve, reject) {
    collection.find(filter, options).then(resolve, reject)
  }).catch(handleMongoError, "Wrapping error");
};

var CouplingDataService = function (mongoUrl) {
  this.mongoUrl = mongoUrl;
  var database = monk(mongoUrl);
  var playersCollection = database.get('players');
  var historyCollection = database.get('history');
  var tribesCollection = database.get('tribes');
  var pinCollection = database.get('pins');

  this.requestTribes = function () {
    return makeDocumentPromise(tribesCollection);
  };

  this.requestTribe = function (tribeId) {
    return new Promise(function (resolve, reject) {
      tribesCollection.findOne({id: tribeId}).then(resolve, reject)
    }).catch(handleMongoError, "Wrapping error");
  };

  this.requestHistory = function (tribeId) {
    return makeDocumentPromise(historyCollection, {sort: {date: -1}}, {'tribe': tribeId, isDeleted: null});
  };

  this.requestPlayers = function (tribeId) {
    return makeDocumentPromise(playersCollection, {}, {'tribe': tribeId, isDeleted: null});
  };

  this.requestPins = function (tribeId) {
    return makeDocumentPromise(pinCollection, {}, {tribe: tribeId, isDeleted: null});
  };

  this.combine = function (promiseMap) {
    return Promise.all(_.values(promiseMap)).then(function (resultArray) {
      return _.object(_.keys(promiseMap), resultArray);
    });
  };

  this.requestPinsAndHistory = function (tribeId) {
    return this.combine({
      pins: this.requestPins(tribeId),
      history: this.requestHistory(tribeId)
    });
  };

  this.requestPlayersAndHistory = function (tribeId) {
    return this.combine({
      players: this.requestPlayers(tribeId),
      history: this.requestHistory(tribeId)
    });
  };

  this.savePairAssignmentsToHistory = function (pairs, callback) {
    historyCollection.insert(pairs, callback);
  };

  this.savePlayer = function (player) {
    if (player._id) {
      return playersCollection.update(player._id, player, {upsert: true})
        .then(function (result) {
          var failureToUpdateMessage = 'Player could not be updated because it could not be found.';
          if (result.nModified === 0 && result.n === 0) {
            throw new Error({message: failureToUpdateMessage});
          }
        });
    } else {
      return playersCollection.insert(player);
    }
  };

  this.savePin = function (pin, callback) {
    pinCollection.insert(pin, callback);
  };

  this.removePlayer = function (playerId, callback) {
    playersCollection.update(playerId, {$set: {isDeleted: true}},
      makeUpdateByIdCallback('Failed to remove the player because it did not exist.', callback));
  };

  this.removePin = function (pinId, callback) {
    pinCollection.update(pinId, {$set: {isDeleted: true}},
      makeUpdateByIdCallback('Failed to remove the pin because it did not exist.', callback));
  };

  function makeUpdateByIdCallback(failureToUpdateMessage, done) {
    return function (error, result) {
      if (result.nModified == 0 && error == null) {
        error = {message: failureToUpdateMessage};
      }
      done(error);
    };
  }

  this.removePairAssignments = function (pairAssignmentsId) {
    return historyCollection.update({_id: pairAssignmentsId}, {isDeleted: true})
      .then(function (results) {
        if (results.nModified === 0) {
          throw new Error('Pair Assignments could not be deleted because they do not exist.');
        }
      });
  };
  this.database = database;
};

module.exports = CouplingDataService;