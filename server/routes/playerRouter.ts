import * as express from "express";
// @ts-ignore
import {commandDispatcher} from "server"
import {handleRequest} from "./route-helper";

class PlayerRoutes {

    listPlayers = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performPlayersQuery(request.params.tribeId),
        (response, data) => response.send(data)
    );

    listRetiredMembers = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performRetiredPlayersQuery(request.params.tribeId),
        (response, data) => response.send(data)
    );

    savePlayer = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performSavePlayerCommand(request.body, request.params.tribeId),
        (response, data) => response.send(data)
    );

    removePlayer = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performDeletePlayerCommand(request.params.playerId),
        (response, data) => {
            if (data)
                response.send(data);
            else {
                response.statusCode = 404;
                response.send({message: 'Player could not be deleted because they do not exist.'})
            }
        }
    );

}

const players = new PlayerRoutes();
const router = express.Router({mergeParams: true});
router.route('/')
    .get(players.listPlayers)
    .post(players.savePlayer);
router.delete('/:playerId', players.removePlayer);
router.get('/retired', players.listRetiredMembers);

export default router;