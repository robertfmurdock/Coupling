"use strict";

var should = require('should');

var PinAssigner = require('../../lib/PinAssigner');

describe('PinAssigner', function () {

    it('will do the obvious thing when there is only one pin and one player', function () {
        var pinAssigner = new PinAssigner();
        var pins = [
            {name: "Lucky"}
        ];
        var player = {name: "Pete"};
        var players = [player];

        pinAssigner.assignPins(pins, players);

        player.pins.should.eql(pins);
    });

    it('will assign no pins when there are no players', function () {
        var pinAssigner = new PinAssigner();
        var pins = [
            {name: "Lucky"}
        ];
        var players = [];

        pinAssigner.assignPins(pins, players);

        players.length.should.eql(0);
    });

});