import {tribeResolution, playersResolution} from "./Resolutions";
import IRoute = angular.route.IRoute;
import * as services from '../services';
import * as _ from 'underscore';

class EditPlayerRouteController {
    static $inject = ['$route', 'tribe', 'players'];
    tribe: services.Tribe;
    player: services.Player;
    players: [services.Player];

    constructor($route, tribe, players) {
        this.tribe = tribe;
        this.players = players;
        const playerId = $route.current.params.id;
        this.player = _.findWhere(this.players, {_id: playerId});
    }
}

const editPlayerRoute: IRoute = {
    template: '<player-config player="main.player" players="main.players" tribe="main.tribe">',
    controller: EditPlayerRouteController,
    controllerAs: 'main',
    resolve: {
        tribe: tribeResolution,
        players: playersResolution
    }
};

export default editPlayerRoute;