import Comparators from "../../../common/Comparators";
import Pair from "../../../common/Pair";

const ObjectID = require('mongodb').ObjectID;

describe('Comparators', function () {
    describe("pairs", function () {
        it('are equal when using string ids', function () {

            const pair1: Pair = [
                {_id: '7'},
                {_id: '8'}
            ];
            const pair2: Pair = [
                {_id: String('8')},
                {_id: '7'}
            ];
            expect(Comparators.areEqualPairs(pair1, pair2)).toBe(true);
        });
    });


    describe("players", function () {
        it('player will not be equal to null', function () {
            expect(Comparators.areEqualPlayers({_id: "how", name: "duder"}, null)).toBe(false);
            expect(Comparators.areEqualPlayers(null, {_id: "why", name: "duder"})).toBe(false);
        });

        it("equal players with string ids are equal", function () {
            const batman = {name: 'Batman', _id: '5351790026c06ff51400000a', tribe: 'Gotham'};
            const anotherBatman = {name: 'Batman', _id: '5351790026c06ff51400000a', tribe: 'Gotham'};

            expect(Comparators.areEqualPlayers(batman, anotherBatman)).toBe(true);
            expect(Comparators.areEqualPlayers(anotherBatman, batman)).toBe(true);
        });

        it('equal players with entity ids are equal', function () {
            const batman = {
                name: 'Batman',
                _id: ObjectID.createFromHexString('000000079bb31fb01ee7834c'),
                tribe: 'what'
            };
            const anotherBatman = {
                name: 'Batman',
                _id: ObjectID.createFromHexString('000000079bb31fb01ee7834c'),
                tribe: 'what'
            };
            expect(Comparators.areEqualPlayers(batman, anotherBatman)).toBe(true);
        })
    });
});