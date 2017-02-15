import CouplingGameFactory from "../lib/CouplingGameFactory";
import GameRunner from "../lib/GameRunner";
import * as Promise from "bluebird";
import Tribe from "../../common/Tribe";

const couplingGameFactory = new CouplingGameFactory();
const gameRunner = new GameRunner(couplingGameFactory);


interface GameData {
    pins: any[],
    history: any[],
    tribe: Tribe,
}

let getGameData = function (request, tribeId: string): Promise<GameData> {
    return Promise.props({
        pins: request.dataService.requestPins(tribeId),
        history: request.dataService.requestHistory(tribeId),
        tribe: request.dataService.requestTribe(tribeId),
    }) as Promise<GameData>;
};

export default function (request, response) {
    const tribeId = request.params.tribeId;
    const availablePlayers = request.body;
    getGameData(request, tribeId)
        .then(values => gameRunner.run(availablePlayers, values.pins, values.history, tribeId))
        .then(result => response.send(result), err => response.send(err));
};