import * as express from "express";
// @ts-ignore
import {commandDispatcher} from "server"

function respond(response, promise) {
    promise
        .then(function (players) {
            response.send(players);
        }, function (error) {
            console.log('error', error);
            response.statusCode = 500;
            response.send({message: error.message});
        })
}

class PlayerRoutes {
    listPlayers(request, response) {
        respond(
            response,
            request.commandDispatcher
                .performPlayersQuery(request.params.tribeId)
        );
    };

    savePlayer(request, response) {
        respond(
            response,
            request.commandDispatcher
                .performSavePlayerCommand(request.body, request.params.tribeId)
        )
    };

    removePlayer(request, response) {
        respond(
            response,
            request.commandDispatcher
                .performDeletePlayerCommand(request.params.playerId)
        );
    };

    listRetiredMembers(request, response) {
        respond(
            response,
            request.commandDispatcher.performRetiredPlayersQuery(request.params.tribeId)
        );
    };

}

const players = new PlayerRoutes();
const router = express.Router({mergeParams: true});
router.all('/*', async function (request, response, next) {
    // @ts-ignore
    const isAuthorized = await request.commandDispatcher.performUserIsAuthorizedAction(request.params.tribeId);
    if (isAuthorized) {
        next()
    } else {
        response.sendStatus(404);
    }
});
router.route('/')
    .get(players.listPlayers)
    .post(players.savePlayer);
router.delete('/:playerId', players.removePlayer);
router.get('/retired', players.listRetiredMembers);

export default router;