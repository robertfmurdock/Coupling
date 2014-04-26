"use strict";
var monk = require('monk');
var Promise = require('rsvp').Promise;

var makeDocumentCallback = function (reject, resolve) {
    return function (error, documents) {
        if (error) {
            reject(Error(error));
        } else {
            resolve(documents);
        }
    };
};

var makeDocumentPromise = function (collection, options, filter) {
    return new Promise(function (resolve, reject) {
        collection.find(filter, options, makeDocumentCallback(reject, resolve));
    });
};

var CouplingDataService = function (mongoUrl) {
    var database = monk(mongoUrl);
    var playersCollection = database.get('players');
    var historyCollection = database.get('history');
    var tribesCollection = database.get('tribes');

    function makeTribesPromise() {
        return makeDocumentPromise(tribesCollection);
    }

    function makeHistoryPromise(tribeId) {
        return makeDocumentPromise(historyCollection, {sort: {date: -1}}, {'tribe': tribeId});
    }

    function makePlayersPromise(tribeId) {
        return makeDocumentPromise(playersCollection, {}, {'tribe': tribeId});
    }

    this.requestPlayersAndHistory = function (tribeId, dataIsAvailable, errorHandler) {
        Promise.all([makePlayersPromise(tribeId), makeHistoryPromise(tribeId)]).then(function (arrayOfResults) {
            dataIsAvailable(arrayOfResults[0], arrayOfResults[1]);
        }, function (error) {
            errorHandler({message: 'Could not read from MongoDB.', error: error});
        });
    };

    this.requestTribes = function (dataIsAvailable, errorHandler) {
        makeTribesPromise().then(dataIsAvailable, errorHandler);
    };

    this.requestHistory = function (tribeId, dataIsAvailable, errorHandler) {
        makeHistoryPromise(tribeId).then(dataIsAvailable, errorHandler);
    };

    this.requestPlayers = function (tribeId, dataIsAvailable, errorHandler) {
        makePlayersPromise(tribeId).then(dataIsAvailable, errorHandler);
    };

    this.savePairAssignmentsToHistory = function (pairs, callback) {
        historyCollection.insert(pairs, callback);
    };
    this.savePlayer = function (player, callback) {
        if (player._id) {
            playersCollection.updateById(player._id, player, callback());
        } else {
            playersCollection.insert(player, callback);
        }
    };
};

module.exports = CouplingDataService;