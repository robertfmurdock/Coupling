import IRoute = angular.route.IRoute;
import * as services from "../services";
import * as pipe from "ramda/src/pipe";
import * as values from "ramda/src/values";
import * as filter from "ramda/src/filter";
import * as map from "ramda/src/map";

class NewPairAssignmentsRouteController {
    static $inject = ['requirements'];
    tribe: services.Tribe;
    players: [services.Player];
    pairAssignments: services.PairAssignmentSet;

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
            return $q.all({
                tribe: Coupling.getTribe(tribeId),
                players: Coupling.getPlayers(tribeId),
                history: Coupling.getHistory(tribeId)
            })
                .then(options => {
                    const players: [services.Player] = options['players'];
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