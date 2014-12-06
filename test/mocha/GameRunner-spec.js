var CouplingGameFactory = require('../../server/lib/CouplingGameFactory');
var GameRunner = require('../../server/lib/GameRunner');
var sinon = require('sinon');
var should = require('should');

describe('Game Runner', function () {

    it('will build a game, run with all available players, and then return the results', sinon.test(function () {
        this.clock.tick(2039810);
        var players = [];
        var history = [];

        var couplingGameFactory = new CouplingGameFactory();
        var buildStub = this.stub(couplingGameFactory, 'buildGame');
        var playStub = this.stub();
        buildStub.returns({play: playStub});

        var pairingAssignments = [
            {},
            {}
        ];
        playStub.returns(pairingAssignments);
        var gameRunner = new GameRunner(couplingGameFactory);

        var result = gameRunner.run(players, history);

        result.date.should.eql(new Date());
        should(result.pairs).equal(pairingAssignments);
    }));
});