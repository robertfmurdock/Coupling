import * as express from "express";
// @ts-ignore
import {playersQueryDispatcher, savePlayerCommandDispatcher} from "server"

function respond(response, promise) {
    promise
        .then(function (players) {
            response.send(players);
        }, function (error) {
            console.log('error', error);
            response.statusCode = 500;
            response.send(error);
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
                .performCommand(request.body)
        )
    };

    removePlayer(request, response) {
        request.dataService.removePlayer(request.params.playerId, function (error) {
            if (error) {
                response.statusCode = 404;
                response.send(error);
            } else {
                response.send({});
            }
        });
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
router.route('/')
    .get(players.listPlayers)
    .post(players.savePlayer);
router.delete('/:playerId', players.removePlayer);
router.get('/retired', players.listRetiredMembers);

export default router;