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

var makeDocumentPromise = function (collection, options) {
    return new Promise(function (resolve, reject) {
        collection.find({}, options, makeDocumentCallback(reject, resolve));
    });
};

var CouplingDataService = function (mongoUrl) {
    var database = monk(mongoUrl);
    var playersCollection = database.get('players');
    var historyCollection = database.get('history');

    function makeHistoryPromise() {
        return makeDocumentPromise(historyCollection, {sort: {date: -1}});
    }

    function makePlayersPromise() {
        return makeDocumentPromise(playersCollection, {});
    }

    this.requestPlayersAndHistory = function (dataIsAvailable, errorHandler) {
        Promise.all([makePlayersPromise(), makeHistoryPromise()]).then(function (arrayOfResults) {
            dataIsAvailable(arrayOfResults[0], arrayOfResults[1]);
        }, function (error) {
            errorHandler({message: 'Could not read from MongoDB.', error: error});
        });
    };

    this.requestHistory = function (dataIsAvailable, errorHandler) {
        makeHistoryPromise().then(dataIsAvailable, errorHandler);
    };

    this.requestPlayers = function (dataIsAvailable, errorHandler) {
        makePlayersPromise().then(dataIsAvailable, errorHandler);
    };

    this.savePairAssignmentsToHistory = function (pairs, callback) {
        historyCollection.insert(pairs, callback);
    };
    this.savePlayer = function (player, callback) {
        playersCollection.insert(player, callback);
    };
};

module.exports = CouplingDataService;