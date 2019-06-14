import {module} from "angular";
import IController = angular.IController;
import {connectReactToNg} from "../ReactNgAdapter";
import ReactServerMessage from "./ReactServerMessage";

export class ServerMessageController implements IController {

    static $inject = ['$scope', '$element'];

    tribeId: string;

    constructor($scope, $element) {
        connectReactToNg({
            component: ReactServerMessage,
            props: () => ({
                tribeId: this.tribeId,
                useSsl: 'https:' === window.location.protocol,
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "tribeId"
        });
    }
}

export default module('coupling.serverMessage', [])
    .directive('serverMessage', function () {
        return {
            template: '<div/>',
            controller: ServerMessageController,
            controllerAs: 'socket',
            bindToController: true,
            scope: {
                tribeId: '='
            }
        }
    });