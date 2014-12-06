"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function () {
    this.listTribeMembers = function (request, response) {
        request.dataService.requestPlayers(request.params.tribeId).then(function (players) {
            response.send(players);
        }, function (error) {
            response.send(error);
        })
    };
    this.savePlayer = function (request, response) {
        var player = request.body;
        request.dataService.savePlayer(player, function () {
            response.send(player);
        });
    };
    this.removePlayer = function (request, response) {
        request.dataService.removePlayer(request.params.playerId, function (error) {
            if (error) {
                response.statusCode = 404;
                response.send(error);
            } else {
                response.send({});
            }
        });
    }
};