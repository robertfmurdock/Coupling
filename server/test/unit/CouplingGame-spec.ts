import PairHistoryReport from "../../lib/PairCandidateReport";
import CouplingGame from "../../lib/CouplingGame";
import PairingRule from "../../../common/PairingRule";

// @ts-ignore
import {actionDispatcherMock} from "engine_test"

const stub = {reportProvider: {pairingHistory: {historyDocuments: []}}};

describe("Coupling Game", function () {
    function badSpin(players) {
        return players[0];
    }

    it("with no players should return no pairs", function () {
        var game = new CouplingGame(stub, null);
        var players = [];

        var results = game.play(players, PairingRule.LongestTime);
        expect(results).toEqual([]);
    });

    describe("with two players", function () {
        var player1 = {_id: 'bill', tribe: ''};
        var player2 = {_id: 'ted', tribe: ''};

        var allPlayers = [player1, player2];
        var spinFunction;
        var game;
        let mock;

        beforeEach(function () {
            spinFunction = jasmine.createSpy('spin');

            mock = actionDispatcherMock();

            game = new CouplingGame(stub, {spin: spinFunction}, mock);
            mock.setNextPairCandidateReportsToReturn([new PairHistoryReport(player2, [player1], 0)]);
            spinFunction.and.returnValue(player1);
        });


        it("should remove a player from the wheel before each play", function () {
            game.play(allPlayers);

            expect(mock.getPlayersReturnedFromGetNextPairActionAtIndex(0)).toEqual(allPlayers);
            expect(spinFunction).toHaveBeenCalledWith([player1]);
        });

        it("should make one pair in order determined by the wheel", function () {
            var results = game.play(allPlayers);
            expect(results).toEqual([
                [player2, player1]
            ]);
        });
    });

    describe("with three players in singled-out mode", function () {

        var player1 = {_id: 'bill', tribe: ''};
        var player2 = {_id: 'ted', tribe: ''};
        var player3 = {_id: 'mozart', tribe: ''};
        var allPlayers = [player1, player2, player3];
        var spinFunction;
        var game;
        let mock;

        beforeEach(function () {
                spinFunction = jasmine.createSpy('spin');
                spinFunction.and.returnValues(player1, null);

                mock = actionDispatcherMock();
                mock.setNextPairCandidateReportsToReturn([
                        new PairHistoryReport(player3, [player1, player2], 0),
                        new PairHistoryReport(player2, [], 0)
                    ]
                );

                game = new CouplingGame(stub, {spin: spinFunction}, mock);
            }
        );

        it("should remove a player from the wheel before each play", function () {
            game.play(allPlayers);

            expect(mock.getPlayersReturnedFromGetNextPairActionAtIndex(0))
                .toEqual(allPlayers);
            expect(spinFunction.calls.argsFor(0)).toEqual([[player1, player2]]);
            expect(mock.getPlayersReturnedFromGetNextPairActionAtIndex(1))
                .toEqual([player2]);
        });

        it("should make two pairs in order determined by the wheel", function () {
            var results = game.play(allPlayers);
            expect(results).toEqual([
                [player3, player1],
                [player2]
            ]);
        });
    });

    it("should one pair two players", function () {
        const mock = actionDispatcherMock();

        var game = new CouplingGame(stub, {spin: badSpin}, mock);

        var player1 = {_id: 'bill', tribe: ''};
        var player2 = {_id: 'ted', tribe: ''};
        var allPlayers = [player1, player2];

        mock.setNextPairCandidateReportsToReturn([new PairHistoryReport(player1, [player2], 0)]);

        var results = game.play(allPlayers, PairingRule.LongestTime);

        expect(results).toEqual([
            [player1, player2]
        ]);
    });
});
