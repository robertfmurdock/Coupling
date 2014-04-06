var PairHistoryReport = function (player, partnerCandidates, timeSinceLastPaired) {
    return  {
        player: player,
        partnerCandidates: partnerCandidates,
        timeSinceLastPaired: timeSinceLastPaired
    };
};

module.exports = PairHistoryReport;
