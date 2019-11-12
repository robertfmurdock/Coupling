import * as express from "express";
import {handleRequest} from "./route-helper";

class PlayerRoutes {

    listPlayers = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performPlayersQuery(request, response),
        () => {
        }
    );

    listRetiredMembers = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performRetiredPlayersQuery(request, response),
        () => {
        }
    );

    savePlayer = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performSavePlayerCommand(request, response),
        () => {
        }
    );

    removePlayer = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performDeletePlayerCommand(request, response),
        () => {
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