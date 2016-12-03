"use strict";
var GameRunner = require('../lib/GameRunner');
var CouplingGameFactory = require('../lib/CouplingGameFactory').default;

var couplingGameFactory = new CouplingGameFactory();
var gameRunner = new GameRunner(couplingGameFactory);

module.exports = function (request, response) {
    var tribeId = request.params.tribeId;
    request.dataService.requestPinsAndHistory(tribeId).then(function (values) {
        var availablePlayers = request.body;
        var result = gameRunner.run(availablePlayers, values.pins, values.history);
        result.tribe = tribeId;
        response.send(result);
    }, response.send);
};