"use strict";
var DataService = require('../lib/CouplingDataService');
var monk = require('monk');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);
    this.list = function (request, response) {
        dataService.requestTribes(function (tribes) {
            response.send(tribes);
        }, function (error) {
            response.statusCode = 500;
            response.send(error.message);
        });
    };

    var database = monk(mongoUrl);
    var tribesCollection = database.get('tribes');
    this.save = function (request, response) {

        var tribeJSON = request.body;
        tribesCollection.updateById(tribeJSON._id, tribeJSON, function (error, modifiedRecordCount) {
            if (modifiedRecordCount != 0) {
                response.send(request.body);
            } else {
                tribesCollection.insert(tribeJSON, function () {
                    response.send(request.body);
                });
            }
        });
    }
};