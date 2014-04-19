"use strict";
var databaseAdapter = require('../lib/CouplingDatabaseAdapter');
var GameRunner = require('../lib/GameRunner');
var CouplingGameFactory = require('../lib/CouplingGameFactory');

var Game = function (mongoUrl) {
    var couplingGameFactory = new CouplingGameFactory();
    var gameRunner = new GameRunner(couplingGameFactory);

    return function (request, response) {
        var availablePlayers = request.body;
        console.info(availablePlayers);
        databaseAdapter(mongoUrl, function (players, history) {
            var result = gameRunner.run(availablePlayers, history);
            response.send(result);
        }, response.send);
    };
};

module.exports = Game;