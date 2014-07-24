"use strict";
var DataService = require('../lib/CouplingDataService');
var monk = require('monk');
var _ = require('underscore');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);
    this.list = function (request, response) {
        dataService.requestTribes(function (tribes) {
            var filteredTribes = _.filter(tribes, function (value) {
                return _.contains(request.user.tribes, value._id);
            });
            response.send(filteredTribes);
        }, function (error) {
            response.statusCode = 500;
            response.send(error.message);
        });
    };

    var database = monk(mongoUrl);
    var tribesCollection = database.get('tribes');
    var usersCollection = database.get('users');
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