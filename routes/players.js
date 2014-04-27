"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);

    this.listTribeMembers = function (request, response) {
        dataService.requestPlayers(request.params.tribeId, function (players) {
            response.send(players);
        }, function (error) {
            response.send(error);
        })
    };
    this.savePlayer = function (request, response) {
        var player = request.body;
        dataService.savePlayer(player, function () {
            response.send(player);
        });
    };
};