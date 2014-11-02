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
    var database = monk(mongoUrl);
    var playersCollection = database.get('players');
    var historyCollection = database.get('history');
    var tribesCollection = database.get('tribes');
    var pinCollection = database.get('pins');

    this.requestTribes = function () {
        return makeDocumentPromise(tribesCollection);
    };

    this.requestHistory = function (tribeId) {
        return makeDocumentPromise(historyCollection, {sort: {date: -1}}, {'tribe': tribeId, isDeleted: null});
    };

    this.requestPlayers = function (tribeId) {
        return makeDocumentPromise(playersCollection, {}, {'tribe': tribeId, isDeleted: null});
    };

    this.requestPins = function (tribeId) {
        return makeDocumentPromise(pinCollection, {}, {tribe: tribeId});
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

    this.savePlayer = function (player, callback) {
        if (player._id) {
            playersCollection.updateById(player._id, player, {upsert: true},
                makeUpdateByIdCallback('Player could not be updated because it could not be found.', callback));
        } else {
            playersCollection.insert(player, callback);
        }
    };

    this.savePin = function (pin, callback) {
        pinCollection.insert(pin, callback);
    };

    this.removePlayer = function (playerId, callback) {
        playersCollection.updateById(playerId, {isDeleted: true},
            makeUpdateByIdCallback('Failed to remove the player because it did not exist.', callback));
    };

    function makeUpdateByIdCallback(failureToUpdateMessage, done) {
        return function (error, modifiedRecordCount) {
            if (modifiedRecordCount == 0 && error == null) {
                error = {message: failureToUpdateMessage};
            }
            done(error);
        };
    }

    this.removePairAssignments = function (pairAssignmentsId, done) {
        historyCollection.updateById(pairAssignmentsId, {isDeleted: true},
            makeUpdateByIdCallback('Pair Assignments could not be deleted because they do not exist.', done));
    }
};

module.exports = CouplingDataService;