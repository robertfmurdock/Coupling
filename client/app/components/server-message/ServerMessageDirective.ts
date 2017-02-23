import {module} from "angular";
import * as template from "./template.pug";
import ITimeoutService = angular.ITimeoutService;

export class ServerMessageController {

    static $inject = ['$websocket', '$timeout'];

    private $websocket;
    private $timeout: ITimeoutService;
    liveSocket;
    message: string;

    constructor(private _$websocket_, _$timeout_: ITimeoutService) {
        this.$websocket = _$websocket_;
        this.$timeout = _$timeout_;
    }

    $onInit() {
        this.connectToWebsocket();
    }

    private connectToWebsocket() {
        this.liveSocket = this.$websocket(`ws://${window.location.host}/api/LOL/pairAssignments/current`);
        this.liveSocket.onMessage(message => this.message = message.data);
        this.liveSocket.onClose(() => this.handleSocketClose());
    }

    private handleSocketClose() {
        this.message = 'Not connected';
        this.$timeout(() => this.connectToWebsocket(), 10000);
    }
}

export default module('coupling.serverMessage', ['ngWebSocket'])
    .directive('serverMessage', function () {
        return {
            template: template,
            controller: ServerMessageController,
            controllerAs: 'socket',
            bindToController: true,
            scope: {}
        }
    });