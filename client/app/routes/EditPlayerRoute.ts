import {tribeResolution, playersResolution} from "./Resolutions";
import * as _ from "underscore";
import IRoute = angular.route.IRoute;

const editPlayerRoute: IRoute = {
    template: '<player-config player="self.player" players="$resolve.players" tribe="$resolve.tribe">',
    controller: ['players', '$route', function (players, $route) {
        const playerId = $route.current.params.id;
        this.player = _.findWhere(players, {_id: playerId});
    }],
    controllerAs: 'self',
    resolve: {
        tribe: tribeResolution,
        players: playersResolution,
    }
};

export default editPlayerRoute;