import * as express from "express";
// @ts-ignore
import {playersQueryDispatcher, savePlayerCommandDispatcher, deletePlayerCommandDispatcher} from "server"
import * as AuthorizedTribesFetcher from "../lib/AuthorizedTribesFetcher";

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
            playersQueryDispatcher(request.dataService)
                .performQuery(request.params.tribeId)
        );
    };

    savePlayer(request, response) {
        respond(
            response,
            savePlayerCommandDispatcher(request.dataService)
                .performCommand(request.body, request.params.tribeId)
        )
    };

    removePlayer(request, response) {
        respond(
            response,
            deletePlayerCommandDispatcher(request.dataService)
                .performCommand(request.params.playerId)
        );
    };

    listRetiredMembers(request, response) {
        respond(
            response,
            request.dataService.requestRetiredPlayers(request.params.tribeId)
        );
    };

}

const players = new PlayerRoutes();
const router = express.Router({mergeParams: true});
router.all('/*', async function (request, response, next) {
    const {isAuthorized} = await AuthorizedTribesFetcher.promiseTribeAndAuthorization(request);
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