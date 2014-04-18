"use strict";
var Comparators = require("../../lib/Comparators");

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

            Comparators.areEqualPairs(pair1, pair2).should.be.true;
        });
    });

    describe("players", function () {
        it('player will not be equal to null', function () {
            Comparators.areEqualPlayers({name: "duder"}, null).should.be.false;
            Comparators.areEqualPlayers(null, {name: "duder"}).should.be.false;
        });

        it("equal players with string ids are equal", function () {
            var batman = { name: 'Batman', _id: '5351790026c06ff51400000a' };
            var anotherBatman = { name: 'Batman', _id: '5351790026c06ff51400000a'};

            Comparators.areEqualPlayers(batman, anotherBatman).should.be.true;
            Comparators.areEqualPlayers(anotherBatman, batman).should.be.true;
        });
    });
});