import {module} from "angular";
import {connectReactToNg} from "../ReactNgAdapter";
import StatisticsPage from "./StatisticsPage";

export class StatisticsController {
    public tribeId: string;

    static $inject = ['$location', '$scope', '$element', 'Coupling'];

    constructor(public $location, $scope, element, coupling) {
        connectReactToNg({
            component: StatisticsPage,
            props: () => ({
                tribeId: this.tribeId,
                coupling
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
            scope: {tribeId: '='},
            template: "<div/>"
        }
    });