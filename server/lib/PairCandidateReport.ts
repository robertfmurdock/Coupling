import Player from "./common/Player";

export default class PairCandidateReport {

    public partnerCandidates: any;

    public constructor(public player: Player, partnerCandidates: Player[], public timeSinceLastPaired: number) {
        this.partnerCandidates = partnerCandidates ? partnerCandidates : [];
    }
}