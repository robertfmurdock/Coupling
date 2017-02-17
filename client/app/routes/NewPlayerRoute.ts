import {tribeResolution, playersResolution} from "./Resolutions";
import IRoute = angular.route.IRoute;
import * as services from "../services";

class NewPlayerRouteController {
    static $inject = ['tribe', 'players'];
    tribe: services.Tribe;
    player: services.Player;
    players: [services.Player];

    constructor(tribe, players) {
        this.tribe = tribe;
        this.players = players;
        this.player = {_id: undefined, tribe: tribe.id};
    }
}

const newPlayerRoute: IRoute = {
    template: '<player-config player="main.player" players="main.players" tribe="main.tribe">',
    controller: NewPlayerRouteController,
    controllerAs: 'main',
    resolve: {
        tribe: tribeResolution,
        players: playersResolution
    }
};

export default newPlayerRoute;