import {module} from "angular";
import * as template from "./template.pug";
import * as _ from 'underscore'
import ITimeoutService = angular.ITimeoutService;
import ILocationService = angular.ILocationService;
import IController = angular.IController;

export class ServerMessageController implements IController {

    static $inject = ['$websocket', '$timeout', '$location'];

    private $websocket;
    private $timeout: ITimeoutService;
    private $location: ILocationService;
    liveSocket;
    message: string;

    constructor(private _$websocket_, _$timeout_: ITimeoutService, $location: ILocationService) {
        this.$websocket = _$websocket_;
        this.$timeout = _$timeout_;
        this.$location = $location;
    }

    $onInit() {
        this.connectToWebsocket();
    }

    $onDestroy(): void {
        this.liveSocket.onCloseCallbacks = _.without(this.liveSocket.onCloseCallbacks, this.handleSocketClose);
        this.liveSocket.close();
    }

    private connectToWebsocket() {
        this.liveSocket = this.$websocket(this.buildSocketUrl());
        this.liveSocket.onMessage(message => this.message = message.data);
        this.liveSocket.onClose(this.handleSocketClose);
    }

    private buildSocketUrl() {
        const protocol = 'https' === this.$location.protocol() ? 'wss' : 'ws';
        return `${protocol}://${window.location.host}/api/LOL/pairAssignments/current`;
    }

    private handleSocketClose = () => {
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