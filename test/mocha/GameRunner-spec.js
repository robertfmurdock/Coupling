var CouplingGameFactory = require('../../lib/CouplingGameFactory');
var GameRunner = require('../../lib/GameRunner');
var sinon = require('sinon');
var should = require('should');

describe('Game Runner', function () {

    it('will build a game, run with all available players, and then save the results to history', sinon.test(function () {
        this.clock.tick(2039810);
        var players = [];
        var history = [];

        var insertStub = this.stub();
        var historyCollection = {insert: insertStub};

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

        var result = gameRunner.run(players, history, historyCollection);

        var lastInsertedPairingAssignments = insertStub.args[0][0];
        lastInsertedPairingAssignments.date.should.eql(new Date());
        should(lastInsertedPairingAssignments.pairs).equal(pairingAssignments);
        result.should.equal(pairingAssignments);
    }));
});