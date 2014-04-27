"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);
    return {
        listTribeMembers: function (request, response) {
            dataService.requestPlayers(request.params.tribeId, function (players) {
                response.send(players);
            }, function (error) {
                response.send(error);
            })
        },
        savePlayer: function (request, response) {
            var player = request.body;
            dataService.savePlayer(player, function () {
                response.send(player);
            });
        }
    };
};