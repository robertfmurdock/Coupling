import {IController, module} from "angular";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactLoginChooser from "./ReactLoginChooser";

export class LoginChooserController implements IController {

    static $inject = ['$location', '$scope', '$element'];
    styles: any;

    constructor(public $location, $scope, element) {
        connectReactToNg({
            component: ReactLoginChooser,
            props: () => ({}),
            domNode: element[0],
            $scope: $scope,
            watchExpression: "",
            $location: $location
        });
    }
}

export default module('coupling.loginChooser', [])
    .controller('LoginChooserController', LoginChooserController)
    .directive('loginchooser', () => {
        return {
            template: "<div/>",
            restrict: 'E',
            controller: 'LoginChooserController',
            controllerAs: 'loginChooser',
            bindToController: true
        }
    });