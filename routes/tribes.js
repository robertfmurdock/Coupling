"use strict";
var DataService = require('../lib/CouplingDataService');
var monk = require('monk');
var Promise = require('rsvp').Promise;
var _ = require('underscore');

function spread(extracted) {
    return function (results) {
        extracted.apply(null, results);
    };
}

module.exports = function (mongoUrl) {

    var database = monk(mongoUrl);
    var tribesCollection = database.get('tribes');
    var playersCollection = database.get('players');
    var usersCollection = database.get('users');

    var dataService = new DataService(mongoUrl);

    function findAuthorizedTribeIds(user) {
        return new Promise(function (resolve) {
            playersCollection.find({email: user.email})
                .success(function (documents) {
                    var allTribesThatHaveMembership = _.pluck(documents, 'tribe');
                    var authorizedTribes = _.union(user.tribes, allTribesThatHaveMembership);
                    resolve(authorizedTribes);
                });
        });
    }

    function findAuthorizedTribes(user) {
        return new Promise(function (resolve, reject) {
            var authorizedTribeIdsPromise = findAuthorizedTribeIds(user);

            var tribesPromise = new Promise(function (resolve, error) {
                dataService.requestTribes(resolve, error);
            });

            Promise.all([tribesPromise, authorizedTribeIdsPromise]).then(spread(function (tribes, authorizedTribes) {
                var filteredTribes = _.filter(tribes, function (value) {
                    return _.contains(authorizedTribes, value._id);
                });
                resolve(filteredTribes);
            }), reject)
        });
    }

    this.list = function (request, response) {
        findAuthorizedTribes(request.user)
            .then(function (authorizedTribes) {
                response.send(authorizedTribes);
            }, function (error) {
                response.statusCode = 500;
                response.send(error.message);
            });
    };

    this.save = function (request, response) {

        var tribeJSON = request.body;
        tribesCollection.updateById(tribeJSON._id, tribeJSON, function (error, modifiedRecordCount) {
            if (modifiedRecordCount != 0) {
                response.send(request.body);
            } else {
                tribesCollection.insert(tribeJSON, function () {
                    usersCollection.update({_id: request.user._id}, {$addToSet: {tribes: tribeJSON._id}});
                    response.send(request.body);
                });
            }
        });
    }
};