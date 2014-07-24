"use strict";
var DataService = require('../lib/CouplingDataService');
var monk = require('monk');
var _ = require('underscore');

module.exports = function (mongoUrl) {

    var database = monk(mongoUrl);
    var tribesCollection = database.get('tribes');
    var playersCollection = database.get('players');
    var usersCollection = database.get('users');

    var dataService = new DataService(mongoUrl);
    this.list = function (request, response) {
        playersCollection.find({email: request.user.email}, function (error, documents) {
            var allTribesThatHaveMembership = _.pluck(documents, 'tribe');
            var authorizedTribes = _.union(request.user.tribes, allTribesThatHaveMembership);
            dataService.requestTribes(function (tribes) {
                var filteredTribes = _.filter(tribes, function (value) {
                    return _.contains(authorizedTribes, value._id);
                });

                response.send(filteredTribes);
            }, function (error) {
                response.statusCode = 500;
                response.send(error.message);
            });
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