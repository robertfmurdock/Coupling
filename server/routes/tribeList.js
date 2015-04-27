"use strict";
var express = require('express');
var monk = require('monk');
var Promise = require('rsvp').Promise;
var _ = require('underscore');
var config = require('../../config');

var TribeRoutes = function () {

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
        return Promise.all([dataService.requestTribes(), loadAuthorizedTribeIds(user, dataService.mongoUrl)])
            .then(function (results) {
                return function (tribes, authorizedTribes) {
                    return _.filter(tribes, function (value) {
                        return _.contains(authorizedTribes, value._id);
                    });
                }.apply(null, results);
            });
    }

    this.list = function (request, response) {
        requestAuthorizedTribes(request.user, request.dataService)
            .then(function (authorizedTribes) {
                console.log(authorizedTribes);
                response.send(authorizedTribes);
            })
            .catch(function (error) {
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

var tribes = new TribeRoutes();
var router = express.Router();
router.route('/')
    .get(tribes.list)
    .post(tribes.save);
module.exports = router;