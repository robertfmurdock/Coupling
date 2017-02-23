import "angular-websocket/dist/angular-websocket-mock";

describe('Server message directive', function () {
    let $websocketBackend;

    beforeEach(angular.mock.module('coupling', 'ngWebSocket', 'ngWebSocketMock'));

    beforeEach(inject(function (_$websocketBackend_) {
        $websocketBackend = _$websocketBackend_;
        $websocketBackend.mock();
    }));

    afterEach(function () {
        $websocketBackend.verifyNoOutstandingExpectation();
        $websocketBackend.verifyNoOutstandingRequest();
    });

    function buildDirective($compile: angular.ICompileService, $rootScope) {
        const element = angular.element('<server-message>');
        const scope = $rootScope.$new();
        const directive = $compile(element)(scope);

        scope.$digest();
        return {rootScope: scope, directive};
    }

    it('connects to current pair assignments websockets', inject(function ($compile, $rootScope) {
        $websocketBackend.expectConnect(`ws://${window.location.host}/api/LOL/pairAssignments/current`);
        const directive = buildDirective($compile, $rootScope);
        expect(directive).toBeDefined();
        $websocketBackend.flush();
    }));

    it('displays server message', inject(function ($compile, $rootScope) {
        $websocketBackend.expectConnect(`ws://${window.location.host}/api/LOL/pairAssignments/current`);
        const {rootScope, directive} = buildDirective($compile, $rootScope);

        let scope: any = directive.isolateScope();
        const expectedMessage = "Hi it me";
        scope.socket.liveSocket._onMessageHandler({data: expectedMessage});

        rootScope.$digest();

        expect(scope.socket.message).toEqual(expectedMessage);

        const messageElement = directive.find('.message');
        expect(messageElement.text()).toEqual(expectedMessage);
        $websocketBackend.flush();
    }));

    it('displays not connected message when socket is closed', inject(function ($compile, $rootScope) {
        $websocketBackend.expectConnect(`ws://${window.location.host}/api/LOL/pairAssignments/current`);
        const {rootScope, directive} = buildDirective($compile, $rootScope);

        let scope: any = directive.isolateScope();
        const expectedMessage = "Not connected";
        scope.socket.liveSocket._onCloseHandler({});


        rootScope.$digest();

        expect(scope.socket.message).toEqual(expectedMessage);

        const messageElement = directive.find('.message');
        expect(messageElement.text()).toEqual(expectedMessage);
        $websocketBackend.flush();
    }));

    it('will reconnect after close after 10 second delay', inject(function ($compile, $rootScope, $timeout) {
        $websocketBackend.expectConnect(`ws://${window.location.host}/api/LOL/pairAssignments/current`);
        const {rootScope, directive} = buildDirective($compile, $rootScope);

        let scope: any = directive.isolateScope();
        const originalLiveSocket = scope.socket.liveSocket;
        originalLiveSocket._onCloseHandler({});

        const messageElement = directive.find('.message');
        rootScope.$digest();
        expect(messageElement.text()).toEqual("Not connected");

        $timeout.flush(9000);

        rootScope.$digest();
        expect(messageElement.text()).toEqual("Not connected");

        $websocketBackend.expectConnect(`ws://${window.location.host}/api/LOL/pairAssignments/current`);

        expect(originalLiveSocket).toBe(scope.socket.liveSocket);

        $timeout.flush(1001);

        expect(originalLiveSocket).not.toBe(scope.socket.liveSocket);
        const expectedMessage = "Hi it me";
        scope.socket.liveSocket._onMessageHandler({data: expectedMessage});
        rootScope.$digest();

        expect(messageElement.text()).toEqual(expectedMessage);
        $websocketBackend.flush();
    }));
});