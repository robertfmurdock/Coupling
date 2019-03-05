import {tribeResolution, playersResolution} from "./Resolutions";
import IRoute = angular.route.IRoute;
// @ts-ignore
import * as client from 'client'
// @ts-ignore

const commandDispatcher = client.commandDispatcher();

const newPlayerRoute: IRoute = {
    template: '<player-config player="self.player" players="$resolve.players" tribe="$resolve.tribe">',
    controller: ['tribe', 'players', function (tribe, players) {
        let player = {_id: undefined, tribe: tribe.id, callSignAdjective: null, callSignNoun: null};

        const callSign = commandDispatcher.performFindCallSignAction(players, player);

        player.callSignAdjective = callSign.adjective;
        player.callSignNoun = callSign.noun;

        this.player = player;
    }],
    controllerAs: 'self',
    resolve: {
        tribe: tribeResolution,
        players: playersResolution
    }
};

export default newPlayerRoute;