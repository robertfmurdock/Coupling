import * as express from "express";
import {handleRequest} from "./route-helper";

class PlayerRoutes {
    listPlayers = handleRequest('performPlayersQuery');
    listRetiredMembers = handleRequest('performRetiredPlayersQuery');
    savePlayer = handleRequest('performSavePlayerCommand');
    removePlayer = handleRequest('performDeletePlayerCommand');
}

const players = new PlayerRoutes();
const router = express.Router({mergeParams: true});
router.route('/')
    .get(players.listPlayers)
    .post(players.savePlayer);
router.delete('/:playerId', players.removePlayer);
router.get('/retired', players.listRetiredMembers);

export default router;