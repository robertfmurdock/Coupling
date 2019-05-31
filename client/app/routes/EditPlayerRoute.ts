import find from "ramda/es/find";
import propEq from "ramda/es/propEq";
import {tribeResolution, playersResolution} from "./Resolutions";

import IRoute = angular.route.IRoute;

const editPlayerRoute: IRoute = {
    template: '<player-config player="self.player" players="$resolve.players" tribe="$resolve.tribe">',
    controller: ['players', '$route', function (players, $route) {
        const playerId = $route.current.params.id;
        this.player = find(propEq('_id', playerId), players);
    }],
    controllerAs: 'self',
    resolve: {
        tribe: tribeResolution,
        players: playersResolution,
    }
};

export default editPlayerRoute;