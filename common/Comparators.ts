import Player from "./Player";
import Pair from "./Pair";

const findPerson = function (target: Player) {
    return function (person: Player) {
        return Comparators.areEqualPlayers(target, person);
    };
};

const getIdString = function (id: any) {
    return (typeof id == 'string' || id instanceof String) ? id : id.toHexString();
};

class Comparators {
    static areEqualPairs(pairOne: Pair, pairTwo: Pair) {
        return pairOne.some(findPerson(pairTwo[0])) && pairOne.some(findPerson(pairTwo[1]));
    }

    static areEqualPlayers(playerOne: Player, playerTwo: Player) {
        if (playerOne === playerTwo) {
            return true;
        } else if (playerOne && playerTwo && playerOne._id && playerTwo._id) {
            return this.areEqualObjectIds(playerOne._id, playerTwo._id);
        } else
            return false;
    }

    static areEqualObjectIds(id1, id2) {
        const id1String = getIdString(id1);
        const id2String = getIdString(id2);
        return id1String == id2String;
    }

    static areEqualPairsSyntax (couplingPair1, couplingPair2) {
        return Comparators.areEqualPairs(couplingPair1.asArray(), couplingPair2.asArray());
    }
}

export default Comparators;