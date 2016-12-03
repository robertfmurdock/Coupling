import Player from '../../common/Player'

var findPerson = function (target: Player) {
    return function (person: Player) {
        return Comparators.areEqualPlayers(target, person);
    };
};

var getIdString = function (id: any) {
    return (typeof id == 'string' || id instanceof String) ? id : id.toHexString();
};

class Comparators {
    static areEqualPairs(pairOne, pairTwo) {
        return pairOne.some(findPerson(pairTwo[0])) && pairOne.some(findPerson(pairTwo[1]));
    }

    static areEqualPlayers(playerOne, playerTwo) {
        if (playerOne === playerTwo) {
            return true;
        } else if (playerOne && playerTwo && playerOne._id && playerTwo._id) {
            return this.areEqualObjectIds(playerOne._id, playerTwo._id);
        } else
            return false;
    }

    static areEqualObjectIds(id1, id2) {
        var id1String = getIdString(id1);
        var id2String = getIdString(id2);
        return id1String == id2String;
    }
}

export default Comparators;