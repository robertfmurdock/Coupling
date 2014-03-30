"use strict";

define(['players'], function (players) {
    describe('players', function () {

        it('is a function on exports', function () {
            expect(exports.players).toBeDefined();
        });

        it('should render the players page given a database', function () {
            var database = {};
            var getSpy = jasmine.createSpy("get");
            database.get = getSpy;

            var playersRequestHandler = exports.players(database);

            expect(getSpy.calls.count()).toEqual(0);

            var playersCollection = {};
            var findSpy = jasmine.createSpy("find");
            playersCollection.find = findSpy;
            getSpy.and.returnValue(playersCollection);
            var request = {};
            var response = {};
            playersRequestHandler(request, response);

            var findCallArgs = findSpy.calls.argsFor(0);
            expect(findCallArgs[0]).toBeTruthy();
            expect(findCallArgs[1]).toBeTruthy();
            var findCallback = findCallArgs[2];

            var renderSpy = jasmine.createSpy("render");
            response.render = renderSpy;

            var docs = 'uniqueData';

            findCallback(null, docs);
            expect(renderSpy.calls.count()).toEqual(1);
            expect(renderSpy.calls.argsFor(0)).toEqual(['playerRoster', {'players': docs}]);

        });

    });
});