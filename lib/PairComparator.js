var PairComparator = {
    areEqual: function (pairOne, pairTwo) {
        var findPerson = function (target) {
            return function (person) {
                if (target === person) {
                    return true;
                }
                if (target._id && person._id) {
                    return target._id.toHexString() == person._id.toHexString();
                }
            };
        };

        return pairOne.some(findPerson(pairTwo[0])) && pairOne.some(findPerson(pairTwo[1]));
    }
};

module.exports = PairComparator;