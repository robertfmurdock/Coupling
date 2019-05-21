import {module} from "angular";
import IController = angular.IController;
import Tribe from "../../../../common/Tribe";
import ReactTribeCard from './ReactTribeCard'
import {connectReactToNg} from "../ReactNgAdapter";

export class TribeCardController implements IController {
    static $inject = ['$location', '$element', '$scope'];
    public tribe: Tribe;
    size: number;

    constructor(public $location: angular.ILocationService, public element, public $scope) {
    }

    $onInit() {
        connectReactToNg({
            component: ReactTribeCard,
            props: () => ({
                tribe: this.tribe,
                size: this.size
            }),
            domNode: this.element[0],
            $scope: this.$scope,
            watchExpression: "tribe",
            $location: this.$location
        });
    }
}

export default module('coupling.tribeCard', [])
    .controller('TribeCardController', TribeCardController)
    .directive('tribecard', function () {
        return {
            controller: 'TribeCardController',
            controllerAs: 'tribecard',
            scope: {
                tribe: '=',
                size: '=?'
            },
            bindToController: true,
            restrict: 'E',
            template: "<div/>"
        }
    });