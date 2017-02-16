import PairHeatCalculator from "../../../server/lib/PairHeatCalculator";
import Pair from "../../../common/Pair";
import PairAssignmentDocument from "../../../common/PairAssignmentDocument";
import * as _ from "underscore";

const heatCalculator = new PairHeatCalculator();

describe('PairHeatCalculator', function () {

    const tribeId = "forge";

    it('will return 0 when the pair has never occurred', function () {
        const pair: Pair = [{_id: "bob", tribe: tribeId}, {_id: "fred", tribe: tribeId}];
        const history = [];
        const rotationPeriod = 60;

        const heat = heatCalculator.calculate(pair, history, rotationPeriod);

        expect(heat).toBe(0);
    });

    describe('when there is only one possible pair', function () {
        const rotationPeriod = 1;
        const player1 = {_id: "bob", tribe: tribeId};
        const player2 = {_id: "fred", tribe: tribeId};
        const pair: Pair = [player1, player2];

        it('will return 1 when the pair has one pairing', function () {
            checkHeatForRepeatedPair({expectedHeat: 1, numberOfDocs: 1});
        });

        it('will return 2.5 when the pair has two consecutive pairings', function () {
            checkHeatForRepeatedPair({expectedHeat: 2.5, numberOfDocs: 2});
        });

        it('will return 4.5 when the pair has three consecutive pairings', function () {
            checkHeatForRepeatedPair({expectedHeat: 4.5, numberOfDocs: 3});
        });

        it('will return 7 when the pair has four consecutive pairings', function () {
            checkHeatForRepeatedPair({expectedHeat: 7, numberOfDocs: 4});
        });

        it('will return 10 when the pair has five consecutive pairings', function () {
            checkHeatForRepeatedPair({expectedHeat: 10, numberOfDocs: 5});
        });

        it('will return 1 with one pairing and solo days', function () {
            const doc = makeSinglePairDocument(pair);
            const soloDoc1 = makeSinglePairDocument([player1]);
            const soloDoc2 = makeSinglePairDocument([player2]);
            const history = [soloDoc1, doc, soloDoc2];
            const heat = heatCalculator.calculate(pair, history, rotationPeriod);

            expect(heat).toBe(1);
        });

        function checkHeatForRepeatedPair(options) {
            const {expectedHeat, numberOfDocs} = options;
            const history = makePairDocumentList([pair], numberOfDocs);

            const heat = heatCalculator.calculate(pair, history, rotationPeriod);

            expect(heat).toBe(expectedHeat);
        }
    });

    describe('with three players', function () {
        const rotationPeriod = 3;
        const player1 = {_id: "bob", tribe: tribeId};
        const player2 = {_id: "fred", tribe: tribeId};
        const pair: Pair = [player1, player2];
        const player3 = {_id: "latisha", tribe: tribeId};

        it('will return 1 with one pairing in full rotation', function () {
            const expectedPairing = makePairDocument([pair, [player3]]);
            const alternatePairing1 = makePairDocument([[player1, player3], [player2]]);
            const alternatePairing2 = makePairDocument([[player2, player3], [player1]]);
            const history = [alternatePairing1, expectedPairing, alternatePairing2];
            const heat = heatCalculator.calculate(pair, history, rotationPeriod);

            expect(heat).toBe(1);
        });

        it('will return 0 when last pairing is older than five rotations', function () {
            const expectedPairing = makePairDocument([pair, [player3]]);
            const rotationHeatWindow = 5;
            const intervalsUntilCooling = rotationPeriod * rotationHeatWindow;
            const history = makePairDocumentList([[player2, player3], [player1]], intervalsUntilCooling)
                .concat(expectedPairing);

            const heat = heatCalculator.calculate(pair, history, rotationPeriod);
            expect(heat).toBe(0);
        });
    });

    describe('with five players', function () {
        const rotationPeriod = 5;
        const player1 = {_id: "bob", tribe: tribeId};
        const player2 = {_id: "fred", tribe: tribeId};
        const pair: Pair = [player1, player2];
        const player3 = {_id: "latisha", tribe: tribeId};
        const player4 = {_id: "jane", tribe: tribeId};
        const player5 = {_id: "fievel", tribe: tribeId};


        it('will return 1 when last pairing is almost older than five rotations', function () {
            const expectedPairing = makePairDocument([pair, [player3]]);
            const rotationHeatWindow = 5;
            const intervalsUntilCooling = rotationPeriod * rotationHeatWindow;
            const history = makePairDocumentList(
                [[player2, player3], [player1, player4], [player5]], intervalsUntilCooling - 1)
                .concat(expectedPairing);

            const heat = heatCalculator.calculate(pair, history, rotationPeriod);
            expect(heat).toBe(1);
        });
    });

    function makePairDocumentList(pairs: Pair[], intervalCount: number) {
        return _.map(Array.apply(null, {length: intervalCount}),
            (value, index) => makePairDocument(pairs)
        );
    }

    function makeSinglePairDocument(pair: Pair) {
        return makePairDocument([pair]);
    }

    function makePairDocument(pairs: Pair[]) {
        return new PairAssignmentDocument(new Date(2016, 3, 1), pairs, tribeId);
    }


});