import * as Promise from "bluebird";
import GameRunner from "../lib/GameRunner";
import Tribe from "../../common/Tribe";

const gameRunner = new GameRunner();

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
        .then(values => gameRunner.run(availablePlayers, values.pins, values.history, values.tribe))
        .then(result => response.send(result), err => {
            console.log('Err!', err);
            response.send(err)
        });
};
