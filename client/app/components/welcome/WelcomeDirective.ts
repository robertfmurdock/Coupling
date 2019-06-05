import {module} from "angular";
import * as services from "../../services";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactWelcomeView from "./ReactWelcomeView";
export class WelcomeController {

    static $inject = ['randomizer', '$scope', '$element'];

    constructor(randomizer: services.Randomizer, public $scope, $element) {
        connectReactToNg({
            component: ReactWelcomeView,
            props: () => ({
                randomizer:randomizer
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: ""
        });
    }
}

export default module('coupling.welcome', [])
    .controller('WelcomeController', WelcomeController)
    .directive('welcomepage', function () {
        return {
            restrict: 'E',
            controller: 'WelcomeController',
            controllerAs: 'welcome',
            template: '<div/>'
        }
    });