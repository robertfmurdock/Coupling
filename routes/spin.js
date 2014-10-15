"use strict";
var DataService = require('../lib/CouplingDataService');
var GameRunner = require('../lib/GameRunner');
var CouplingGameFactory = require('../lib/CouplingGameFactory');

var Game = function (mongoUrl) {
    var couplingGameFactory = new CouplingGameFactory();
    var gameRunner = new GameRunner(couplingGameFactory);
    var dataService = new DataService(mongoUrl);
    return function (request, response) {
        var tribeId = request.params.tribeId;
        dataService.requestPinsAndHistory(tribeId).then(function (values) {
            var availablePlayers = request.body;
            var result = gameRunner.run(availablePlayers, values.pins, values.history);
            result.tribe = tribeId;
            response.send(result);
        }, response.send);
    };
};

module.exports = Game;