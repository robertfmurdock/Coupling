"use strict";
var DataService = require('../lib/CouplingDataService');
var monk = require('monk');
var Promise = require('rsvp').Promise;
var _ = require('underscore');
var config = require('../../config');

function requestAll(promiseArray, callback) {
    return Promise.all(promiseArray).then(function (results) {
        return callback.apply(null, results);
    });
}

module.exports = function () {

    function loadAuthorizedTribeIds(user, mongoUrl) {
        var database = monk(mongoUrl);
        var playersCollection = database.get('players');
        var email = user.email;
        var tempSuffixIndex = email.indexOf('._temp');
        if (tempSuffixIndex != -1) {
            email = email.substring(0, tempSuffixIndex);
        }

        return playersCollection.find({email: email}).then(function (documents) {
            var allTribesThatHaveMembership = _.pluck(documents, 'tribe');
            return _.union(user.tribes, allTribesThatHaveMembership);
        });
    }

    function requestAuthorizedTribes(user, dataService) {
        return requestAll([dataService.requestTribes(), loadAuthorizedTribeIds(user, dataService.mongoUrl)], function (tribes, authorizedTribes) {
            return _.filter(tribes, function (value) {
                return _.contains(authorizedTribes, value._id);
            });
        });
    }

    this.list = function (request, response) {
        requestAuthorizedTribes(request.user, request.dataService).then(function (authorizedTribes) {
            response.send(authorizedTribes);
        }).catch(function (error) {
            response.statusCode = 500;
            response.send(error.message);
        });
    };

    this.save = function (request, response) {
        var database = monk(request.dataService.mongoUrl);
        var tribesCollection = database.get('tribes');
        var usersCollection = monk(config.mongoUrl).get('users');
        var tribeJSON = request.body;
        tribesCollection.updateById(tribeJSON._id, tribeJSON, {upsert: true}, function () {
            usersCollection.update({_id: request.user._id}, {$addToSet: {tribes: tribeJSON._id}});
            response.send(request.body);
        });
    }
};