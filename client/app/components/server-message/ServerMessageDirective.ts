import {module} from "angular";
import * as template from './template.pug';

export class ServerMessageController {

    static $inject = ['$websocket'];

    private $websocket;
    liveSocket;
    message: string;

    constructor(private _$websocket_) {
        this.$websocket = _$websocket_;
    }

    $onInit() {
        this.liveSocket = this.$websocket(`ws://${window.location.host}/api/LOL/pairAssignments/current`);
        this.liveSocket.onMessage(message => this.message = message.data);
        this.liveSocket.onClose(() => this.message = 'Not connected');
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