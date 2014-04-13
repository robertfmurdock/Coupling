var CouplingGame = require('../../lib/CouplingGame');
var PairHistoryReport = require('../../lib/PairHistoryReport');
var should = require('should');
var sinon = require('sinon');

describe("Coupling Game", function () {

    function badSpin(players) {
        return players[0];
    }

    it("with no players should return no pairs", function () {
        var game = new CouplingGame(badSpin);
        var players = [];

        var results = game.play(players);

        should(results).eql([]);
    });

    describe("with two players", function () {

        var player1 = {name: 'bill'};
        var player2 = {name: 'ted'};
        var allPlayers = [player1, player2];
        var spinFunction;
        var nextInSequenceFunction;
        var game;

        beforeEach(function () {
                spinFunction = sinon.stub();
                nextInSequenceFunction = sinon.stub();
                game = new CouplingGame({getNextInSequence: nextInSequenceFunction}, {spin: spinFunction});
                nextInSequenceFunction.returns(new PairHistoryReport(player2, [player1]));
                spinFunction.returns(player1);
            }
        );


        it("should remove a player from the wheel before each play", function () {
            game.play(allPlayers);

            should(nextInSequenceFunction.args[0]).eql([allPlayers]);
            should(spinFunction.args[0]).eql([
                [player1]
            ]);
        });

        it("should make one pair in order determined by the wheel", function () {
            var results = game.play(allPlayers);
            should(results).eql([
                [player2, player1]
            ]);
        });
    });

    describe("with three players in singled-out mode", function () {

        var player1 = {name: 'bill'};
        var player2 = {name: 'ted'};
        var player3 = {name: 'mozart'};
        var allPlayers = [player1, player2, player3];
        var spinFunction;
        var nextInSequenceFunction;
        var game;

        beforeEach(function () {
                spinFunction = sinon.stub();
                nextInSequenceFunction = sinon.stub();
                game = new CouplingGame({getNextInSequence: nextInSequenceFunction}, {spin: spinFunction});

                nextInSequenceFunction.onFirstCall().returns(new PairHistoryReport(player3, [player1, player2]));
                spinFunction.returns(player1);
                nextInSequenceFunction.onSecondCall().returns(new PairHistoryReport(player2, []));
            }
        );


        it("should remove a player from the wheel before each play", function () {
            game.play(allPlayers);

            should(nextInSequenceFunction.args[0]).eql([allPlayers]);
            should(spinFunction.args[0]).eql([
                [player1, player2]
            ]);
            should(nextInSequenceFunction.args[1]).eql([
                [ player2]
            ]);
        });

        it("should make two pairs in order determined by the wheel", function () {
            var results = game.play(allPlayers);
            should(results).eql([
                [player3, player1],
                [player2]
            ]);
        });
    });

    it("should one pair two players", function () {
        var nextInSequenceFunction = sinon.stub();
        var game = new CouplingGame({getNextInSequence: nextInSequenceFunction}, {spin: badSpin});

        var player1 = {name: 'bill'};
        var player2 = {name: 'ted'};
        var allPlayers = [player1, player2];

        nextInSequenceFunction.returns(new PairHistoryReport(player1, [player2]));

        var results = game.play(allPlayers);

        should(results).eql([
            [player1, player2]
        ]);
    });
});
