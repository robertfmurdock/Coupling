import IRoute = angular.route.IRoute;
import * as services from "../services";
import pipe from "ramda/es/pipe";
import values from "ramda/es/values";
import filter from "ramda/es/filter";
import map from "ramda/es/map";
import Tribe from "../../../common/Tribe";
import Player from "../../../common/Player";
import PairAssignmentSet from "../../../common/PairAssignmentSet";

class NewPairAssignmentsRouteController {
    static $inject = ['requirements'];
    tribe: Tribe;
    players: Player[];
    pairAssignments: PairAssignmentSet;

    constructor(requirements) {
        this.tribe = requirements.tribe;
        this.players = requirements.players;
        this.pairAssignments = requirements.pairAssignments;
    }
}

const convertMapToSelectedPlayers = pipe(
    values,
    filter(selectable => selectable.isSelected),
    map(selectable => selectable.player)
);

const newPairAssignmentsRoute: IRoute = {
    template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.pairAssignments" is-new="true">',
    controllerAs: 'main',
    controller: NewPairAssignmentsRouteController,
    resolve: {
        requirements: ['$route', '$q', 'Coupling', function ($route: ng.route.IRouteService, $q: angular.IQService, Coupling: services.Coupling) {
            const tribeId = $route.current.params.tribeId;

            const promises: any = {
                tribe: Coupling.getTribe(tribeId),
                players: Coupling.getPlayers(tribeId),
                history: Coupling.getHistory(tribeId)
            };
            return $q.all(promises)
                .then(options => {
                    const players = options['players'] as Player[];
                    const history = options['history'];
                    const selectablePlayerMap = Coupling.getSelectedPlayers(players, history);
                    options['selectedPlayers'] = convertMapToSelectedPlayers(selectablePlayerMap);
                    return options;
                })
                .then(options => {
                    const selectedPlayers = options['selectedPlayers'];
                    options['pairAssignments'] = Coupling.spin(selectedPlayers, tribeId);
                    return $q.all(options);
                });
        }]
    }
};

export default newPairAssignmentsRoute;