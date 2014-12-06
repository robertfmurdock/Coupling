var PairHistoryReport = function (player, partnerCandidates, timeSinceLastPaired) {
    return  {
        player: player,
        partnerCandidates: partnerCandidates ? partnerCandidates : [],
        timeSinceLastPaired: timeSinceLastPaired
    };
};

module.exports = PairHistoryReport;
