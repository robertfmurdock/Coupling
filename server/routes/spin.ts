// @ts-ignore
import {performProposeNewPairsCommand, proposeNewPairsCommandDispatcher} from "server";

export default function (request, response) {
    const tribeId = request.params.tribeId;
    const availablePlayers = request.body;

    proposeNewPairsCommandDispatcher(request.dataService)
        .performCommand(tribeId, availablePlayers)
        .then(result => response.send(result), err => {
            console.log('Err!', err);
            response.send(err)
        });
};
