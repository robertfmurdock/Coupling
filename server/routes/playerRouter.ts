import * as express from "express";
// @ts-ignore
import {playersQueryDispatcher} from "server"

class PlayerRoutes {
    listPlayers(request, response) {
        playersQueryDispatcher(request.dataService)
            .performQuery(request.params.tribeId)
            .then(function (players) {
                response.send(players);
            }, function (error) {
                console.log('error', error);
                response.send(error);
            })
    };

    savePlayer(request, response) {
        const player = request.body;
        request.dataService.savePlayer(player)
            .then(function () {
                response.send(player);
            })
            .catch(function (err) {
                response.send(err);
            });
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
        request.dataService.requestRetiredPlayers(request.params.tribeId).then(function (players) {
            response.send(players);
        }, function (error) {
            response.send(error);
        })
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