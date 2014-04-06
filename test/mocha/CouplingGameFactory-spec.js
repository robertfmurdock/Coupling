var CouplingGameFactory = require('../../lib/CouplingGameFactory');
var CouplingGame = require('../../lib/CouplingGame');
var CouplingWheel = require('../../lib/CouplingWheel');
require('should');

describe('Coupling Game Factory', function () {
    it('will construct a Coupling Game', function () {

        var players = null;
        var history = null;
        var factory = new CouplingGameFactory(players, history);


        var game = factory.buildGame();

        game.should.be.instanceof(CouplingGame);

        game.wheel.should.be.instanceof(CouplingWheel);
    });
});