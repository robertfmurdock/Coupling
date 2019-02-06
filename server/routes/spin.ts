export default function (request, response) {
    const tribeId = request.params.tribeId;
    const availablePlayers = request.body;

    request.commandDispatcher.performProposeNewPairsCommand(tribeId, availablePlayers)
        .then(result => response.send(result), err => {
            console.log('Err!', err);
            response.send(err)
        });
};
