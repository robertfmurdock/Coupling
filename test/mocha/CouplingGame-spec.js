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
        var wheelSpinner;
        var game;

        beforeEach(function () {
                wheelSpinner = sinon.stub();
                game = new CouplingGame(wheelSpinner);

                wheelSpinner.onFirstCall().returns(player2);
                wheelSpinner.onSecondCall().returns(player1);
            }
        );


        it("should remove a player from the wheel before each play", function () {
            game.play(allPlayers);

            should(wheelSpinner.args[0]).eql([allPlayers]);
            should(wheelSpinner.args[1]).eql([
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
        var wheelSpinner;
        var game;

        beforeEach(function () {
                wheelSpinner = sinon.stub();
                game = new CouplingGame(wheelSpinner);

                wheelSpinner.onFirstCall().returns(player3);
                wheelSpinner.onSecondCall().returns(player1);
                wheelSpinner.onThirdCall().returns(player2);
            }
        );


        it("should remove a player from the wheel before each play", function () {
            game.play(allPlayers);

            should(wheelSpinner.args[0]).eql([allPlayers]);
            should(wheelSpinner.args[1]).eql([
                [player1, player2]
            ]);
            should(wheelSpinner.args[2]).eql([
                [ player2]
            ]);
        });

        it("should make two pairs in order determined by the wheel", function () {
            var results = game.play(allPlayers);
            should(results).eql([
                [player3, player1], [player2]
            ]);
        });
    });

    it("should one pair two players", function () {
        var wheelSpinner = badSpin;

        var game = new CouplingGame(wheelSpinner);


        var player1 = {name: 'bill'};
        var player2 = {name: 'ted'};
        var allPlayers = [player1, player2];

        var results = game.play(allPlayers);

        should(results).eql([
            [player1, player2]
        ]);
    });
});
