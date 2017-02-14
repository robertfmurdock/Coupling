import CouplingGameFactory from "../lib/CouplingGameFactory";
import GameRunner from "../lib/GameRunner";

const couplingGameFactory = new CouplingGameFactory();
const gameRunner = new GameRunner(couplingGameFactory);

export default function (request, response) {
    const tribeId = request.params.tribeId;
    const availablePlayers = request.body;
    request.dataService.requestPinsAndHistory(tribeId)
        .then(function (values) {
            return gameRunner.run(availablePlayers, values.pins, values.history, tribeId);
        })
        .then(result => response.send(result), err => response.send(err));
};