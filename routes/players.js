"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function (mongoUrl) {
    return function (request, response) {
        var dataService = new DataService(mongoUrl);
        dataService.requestPlayers(null, function (players) {
            response.send(players);
        }, function (error) {
            response.send(error);
        });
    };
};