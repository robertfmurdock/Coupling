// @ts-ignore
import {spinContext2} from "engine";
// @ts-ignore
import {performProposeNewPairsCommand} from "server";

export default function (request, response) {
    const tribeId = request.params.tribeId;
    const availablePlayers = request.body;

    const context = spinContext2(request.dataService);

    performProposeNewPairsCommand(context, tribeId, availablePlayers)
        .then(result => response.send(result), err => {
            console.log('Err!', err);
            response.send(err)
        });
};
