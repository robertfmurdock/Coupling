import Comparators from "../../../server/lib/Comparators";
import Pair from "../../../common/Pair";

describe('Comparators', function () {
    describe("pairs", function () {
        it('are equal when using string ids', function () {

            const pair1: Pair = [
                {_id: '7', tribe: "what"},
                {_id: '8', tribe: "what"}
            ];
            const pair2: Pair = [
                {_id: String('8'), tribe: "what"},
                {_id: '7', tribe: "what"}
            ];
            expect(Comparators.areEqualPairs(pair1, pair2)).toBe(true);
        });
    });


    describe("players", function () {
        it('player will not be equal to null', function () {
            expect(Comparators.areEqualPlayers({_id: "how", name: "duder", tribe: "what"}, null)).toBe(false);
            expect(Comparators.areEqualPlayers(null, {_id: "why", name: "duder", tribe: "what"})).toBe(false);
        });

        it("equal players with string ids are equal", function () {
            const batman = {name: 'Batman', _id: '5351790026c06ff51400000a', tribe: 'Gotham'};
            const anotherBatman = {name: 'Batman', _id: '5351790026c06ff51400000a', tribe: 'Gotham'};

            expect(Comparators.areEqualPlayers(batman, anotherBatman)).toBe(true);
            expect(Comparators.areEqualPlayers(anotherBatman, batman)).toBe(true);
        });
    });
});