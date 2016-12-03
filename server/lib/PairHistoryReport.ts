import Player from "../../common/Player";

export default class PairHistoryReport {

    public partnerCandidates: any;

    public constructor(public player: Player, partnerCandidates: Player[], public timeSinceLastPaired: number) {
        this.partnerCandidates = partnerCandidates ? partnerCandidates : [];
    }
}