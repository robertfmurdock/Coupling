var CouplingGame = require('../../lib/CouplingGame');
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
        var game;

        beforeEach(function () {
                spinFunction = sinon.stub();
                game = new CouplingGame({spin: spinFunction});

                spinFunction.onFirstCall().returns(player2);
                spinFunction.onSecondCall().returns(player1);
            }
        );


        it("should remove a player from the wheel before each play", function () {
            game.play(allPlayers);

            should(spinFunction.args[0]).eql([allPlayers]);
            should(spinFunction.args[1]).eql([
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
        var game;

        beforeEach(function () {
                spinFunction = sinon.stub();
                game = new CouplingGame({spin: spinFunction});

                spinFunction.onFirstCall().returns(player3);
                spinFunction.onSecondCall().returns(player1);
                spinFunction.onThirdCall().returns(player2);
            }
        );


        it("should remove a player from the wheel before each play", function () {
            game.play(allPlayers);

            should(spinFunction.args[0]).eql([allPlayers]);
            should(spinFunction.args[1]).eql([
                [player1, player2]
            ]);
            should(spinFunction.args[2]).eql([
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
        var game = new CouplingGame({spin: badSpin});

        var player1 = {name: 'bill'};
        var player2 = {name: 'ted'};
        var allPlayers = [player1, player2];

        var results = game.play(allPlayers);

        should(results).eql([
            [player1, player2]
        ]);
    });
});
