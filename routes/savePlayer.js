"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);
    return function (request, response) {
        var player = request.body;
        dataService.savePlayer(player, function () {
            response.send(player);
        });
    };
};