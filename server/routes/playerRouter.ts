import * as express from "express";

class PlayerRoutes {
    listTribeMembers(request, response) {
        request.dataService.requestPlayers(request.params.tribeId).then(function (players) {
            response.send(players);
        }, function (error) {
            response.send(error);
        })
    };
    savePlayer(request, response) {
        var player = request.body;
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
    }
}

var players = new PlayerRoutes();
var router = express.Router({mergeParams: true});
router.route('/')
    .get(players.listTribeMembers)
    .post(players.savePlayer);
router.delete('/:playerId', players.removePlayer);

export default router;