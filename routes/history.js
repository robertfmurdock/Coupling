"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);

    this.list = function (request, response) {
        dataService.requestHistory(request.params.tribeId, function (history) {
            response.send(history);
        }, response.send);
    };

    this.savePairs = function (request, response) {
        var pairs = request.body;
        if (pairs.date && pairs.pairs) {
            pairs.date = new Date(pairs.date);

            dataService.savePairAssignmentsToHistory(pairs, function () {
                response.send(pairs);
            });
        }
        else {
            response.statusCode = 400;
            response.send({error: 'Pairs were not valid.'});
        }
    };

    this.deleteMember = function (request, response) {
        dataService.removePairAssignments(request.params.id, function (error) {
            if (error) {
                response.statusCode = 404;
                response.send(error);
            } else {
                response.send({message: 'SUCCESS'});
            }
        });
    }
};