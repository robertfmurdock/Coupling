import Comparators from "../../../server/lib/Comparators";

describe('Comparators', function () {
    describe("pairs", function () {
        it('are equal when using string ids', function () {

            var pair1 = [
                {_id: '7'},
                {_id: '8'}
            ];
            var pair2 = [
                {_id: String('8')},
                {_id: '7'}
            ];

            expect(Comparators.areEqualPairs(pair1, pair2)).toBe(true);
        });
    });

    describe("players", function () {
        it('player will not be equal to null', function () {
            expect(Comparators.areEqualPlayers({name: "duder"}, null)).toBe(false);
            expect(Comparators.areEqualPlayers(null, {name: "duder"})).toBe(false);
        });

        it("equal players with string ids are equal", function () {
            var batman = { name: 'Batman', _id: '5351790026c06ff51400000a' };
            var anotherBatman = { name: 'Batman', _id: '5351790026c06ff51400000a'};

            expect(Comparators.areEqualPlayers(batman, anotherBatman)).toBe(true);
            expect(Comparators.areEqualPlayers(anotherBatman, batman)).toBe(true);
        });
    });
});