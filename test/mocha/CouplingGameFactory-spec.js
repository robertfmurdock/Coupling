var CouplingGameFactory = require('../../server/lib/CouplingGameFactory');
var CouplingGame = require('../../server/lib/CouplingGame');
var CouplingWheel = require('../../server/lib/CouplingWheel');
var expect = require('chai').expect;

describe('Coupling Game Factory', function () {
    it('will construct a Coupling Game', function () {

        var players = null;
        var history = null;
        var factory = new CouplingGameFactory(players, history);

        var game = factory.buildGame();

        expect(game).to.be.instanceof(CouplingGame);

        expect(game.wheel).to.be.instanceof(CouplingWheel);
    });
});