import Tribe from "../../../../common/Tribe";
import Player from "../../../../common/Player";
import {module} from "angular";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactTribeStatistics from "./ReactTribeStatistics";

export class StatisticsController {
    public tribe: Tribe;
    public players: Player[];
    public history;

    static $inject = ['$location', '$scope', '$element'];

    constructor(public $location, $scope, element) {
        connectReactToNg({
            component: ReactTribeStatistics,
            props: () => ({
                tribe: this.tribe,
                players: this.players,
                history: this.history,
            }),
            domNode: element[0],
            $scope: $scope,
            watchExpression: "",
            $location: $location
        });
    }

}

export default module('coupling.statistics', [])
    .directive('statistics', function () {
        return {
            controllerAs: 'self',
            controller: StatisticsController,
            bindToController: true,
            scope: {
                tribe: '=',
                players: '=',
                history: '='
            },
            template: "<div/>"
        }
    });