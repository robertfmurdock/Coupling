import CouplingGameFactory from "../lib/CouplingGameFactory";
import GameRunner from "../lib/GameRunner";

var couplingGameFactory = new CouplingGameFactory();
var gameRunner = new GameRunner(couplingGameFactory);

export default function (request, response) {
    var tribeId = request.params.tribeId;
    var availablePlayers = request.body;
    request.dataService.requestPinsAndHistory(tribeId)
        .then(function (values) {
            return gameRunner.run(availablePlayers, values.pins, values.history, tribeId);
        })
        .then(result => response.send(result), err => response.send(err));
};