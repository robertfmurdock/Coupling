"use strict";

var findPerson = function (target) {
    return function (person) {
        return Comparators.areEqualPlayers(target, person);
    };
};

var Comparators = {
    areEqualPairs: function (pairOne, pairTwo) {
        return pairOne.some(findPerson(pairTwo[0])) && pairOne.some(findPerson(pairTwo[1]));
    },
    areEqualPlayers: function (playerOne, playerTwo) {
        if (playerOne === playerTwo) {
            return true;
        } else if (playerOne._id && playerTwo._id) {
            return playerOne._id.toHexString() == playerTwo._id.toHexString();
        } else
            return false;
    }
};

module.exports = Comparators;