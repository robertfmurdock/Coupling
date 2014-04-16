"use strict";
var databaseAdapter = require('../lib/CouplingDatabaseAdapter');
var GameRunner = require('../lib/GameRunner');
var CouplingGameFactory = require('../lib/CouplingGameFactory');

var Game = function (mongoUrl) {
    var couplingGameFactory = new CouplingGameFactory();
    var gameRunner = new GameRunner(couplingGameFactory);

    return function (request, response) {
        databaseAdapter(mongoUrl, function (players, history, historyCollection) {
            var result = gameRunner.run(players, history, historyCollection);
            response.send(result);
        });

    };
};

module.exports = Game;