"use strict";
var DataService = require('../lib/CouplingDataService');
var monk = require('monk');
var Promise = require('rsvp').Promise;
var _ = require('underscore');

function spread(extracted) {
    return function (results) {
        return extracted.apply(null, results);
    };
}

module.exports = function (mongoUrl) {

    var database = monk(mongoUrl);
    var tribesCollection = database.get('tribes');
    var playersCollection = database.get('players');
    var usersCollection = database.get('users');

    var dataService = new DataService(mongoUrl);

    function loadAuthorizedTribeIds(user) {
        return playersCollection.find({email: user.email}).then(function (documents) {
            var allTribesThatHaveMembership = _.pluck(documents, 'tribe');
            return _.union(user.tribes, allTribesThatHaveMembership);
        });
    }

    function loadAuthorizedTribes(user) {
        var authorizedTribeIdsPromise = loadAuthorizedTribeIds(user);

        var tribesPromise = new Promise(function (resolve, error) {
            dataService.requestTribes(resolve, error);
        });

        return Promise.all([tribesPromise, authorizedTribeIdsPromise]).then(spread(function (tribes, authorizedTribes) {
            return _.filter(tribes, function (value) {
                return _.contains(authorizedTribes, value._id);
            });
        }));
    }

    this.list = function (request, response) {
        loadAuthorizedTribes(request.user).then(function (authorizedTribes) {
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