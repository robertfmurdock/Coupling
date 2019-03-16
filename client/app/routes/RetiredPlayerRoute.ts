import * as find from "ramda/src/find";
import * as propEq from "ramda/src/propEq";
import {tribeResolution, retiredPlayersResolution} from "./Resolutions";

import IRoute = angular.route.IRoute;

const retiredPlayerRoute: IRoute = {
    template: '<player-config player="self.player" players="$resolve.retiredPlayers" tribe="$resolve.tribe">',
    controller: ['players', '$route', function (players, $route) {
        const playerId = $route.current.params.id;
        this.player = find(propEq('_id', playerId), players);
    }],
    controllerAs: 'self',
    resolve: {
        tribe: tribeResolution,
        players: retiredPlayersResolution,
    }
};

export default retiredPlayerRoute;