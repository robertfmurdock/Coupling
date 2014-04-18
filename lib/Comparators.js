"use strict";

var findPerson = function (target) {
    return function (person) {
        return Comparators.areEqualPlayers(target, person);
    };
};

var getIdString = function (id) {
    return (typeof id == 'string' || id instanceof String) ? id : id.toHexString();
};

var Comparators = {
    areEqualPairs: function (pairOne, pairTwo) {
        return pairOne.some(findPerson(pairTwo[0])) && pairOne.some(findPerson(pairTwo[1]));
    },
    areEqualPlayers: function (playerOne, playerTwo) {
        if (playerOne === playerTwo) {
            return true;
        } else if (playerOne._id && playerTwo._id) {
            return this.areEqualObjectIds(playerOne._id, playerTwo._id);
        } else
            return false;
    },
    areEqualObjectIds: function (id1, id2) {
        var id1String = getIdString(id1);
        var id2String = getIdString(id2);
        return id1String == id2String;
    }
};

module.exports = Comparators;