import {tribeResolution, playersResolution} from "./Resolutions";
import IRoute = angular.route.IRoute;

const newPlayerRoute: IRoute = {
    template: '<player-config player="self.player" players="$resolve.players" tribe="$resolve.tribe">',
    controller: ['tribe', function (tribe) {
        this.player = {_id: undefined, tribe: tribe.id};
    }],
    controllerAs: 'self',
    resolve: {
        tribe: tribeResolution,
        players: playersResolution
    }
};

export default newPlayerRoute;