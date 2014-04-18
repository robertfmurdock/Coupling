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
});