import Sequencer from "../../../server/lib/Sequencer";
import PairHistoryReport from "../../../server/lib/PairHistoryReport";
import * as sinon from "sinon";

describe('Sequencer', function () {

    const bill = {_id: "Bill", tribe: ''};
    const ted = {_id: "Ted", tribe: ''};
    const amadeus = {_id: "Mozart", tribe: ''};
    const shorty = {_id: "Napoleon", tribe: ''};

    it('will use the Pairing History to produce a wheel spin sequence in order of longest time since paired to shortest', function () {
        const players = [bill, ted, amadeus, shorty];

        const getReportStub = sinon.stub();
        const pairingHistory = {
            getPairCandidateReport: getReportStub
        };

        const billsPairCandidates = new PairHistoryReport(bill, [], 3);
        getReportStub.withArgs(bill, [ted, amadeus, shorty]).returns(billsPairCandidates);
        const tedsPairCandidates = new PairHistoryReport(ted, [], 7);
        getReportStub.withArgs(ted, [bill, amadeus, shorty]).returns(tedsPairCandidates);
        const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        getReportStub.withArgs(amadeus, [bill, ted, shorty]).returns(amadeusPairCandidates);
        const shortyPairCandidates = new PairHistoryReport(shorty, [], 5);
        getReportStub.withArgs(shorty, [bill, ted, amadeus]).returns(shortyPairCandidates);

        const sequencer = new Sequencer(pairingHistory);

        const next = sequencer.getNextInSequence(players);

        expect(next).toEqual(tedsPairCandidates);
    });

    it('will use the Pairing History to produce a wheel spin sequence in order of longest time since paired to shortest', function () {
        const players = [bill, amadeus, shorty];

        const getReportStub = sinon.stub();
        const pairingHistory = {
            getPairCandidateReport: getReportStub
        };

        const billsPairCandidates = new PairHistoryReport(bill, [], 3);
        getReportStub.withArgs(bill, [amadeus, shorty]).returns(billsPairCandidates);
        const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        getReportStub.withArgs(amadeus, [bill, shorty]).returns(amadeusPairCandidates);
        const shortyPairCandidates = new PairHistoryReport(shorty, [], 5);
        getReportStub.withArgs(shorty, [bill, amadeus]).returns(shortyPairCandidates);

        const sequencer = new Sequencer(pairingHistory);
        const next = sequencer.getNextInSequence(players);
        expect(next).toEqual(shortyPairCandidates);
    });

    it('will use the Pairing History to get the next in sequence for when a player has never paired.', function () {
        const players = [bill, amadeus, shorty];

        const getReportStub = sinon.stub();
        const pairingHistory = {
            getPairCandidateReport: getReportStub
        };

        const billsPairCandidates = new PairHistoryReport(bill, [], 3);
        getReportStub.withArgs(bill, [amadeus, shorty]).returns(billsPairCandidates);
        const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        getReportStub.withArgs(amadeus, [bill, shorty]).returns(amadeusPairCandidates);
        const shortyPairCandidates = new PairHistoryReport(shorty, [], null);
        getReportStub.withArgs(shorty, [bill, amadeus]).returns(shortyPairCandidates);

        const sequencer = new Sequencer(pairingHistory);
        const next = sequencer.getNextInSequence(players);
        expect(next).toEqual(shortyPairCandidates);
    });

    it('will prioritize the report with fewest players when equal amounts of time.', function () {
        const players = [bill, amadeus, shorty];

        const getReportStub = sinon.stub();
        const pairingHistory = {
            getPairCandidateReport: getReportStub
        };

        const billsPairCandidates = new PairHistoryReport(bill, [
            {_id: '', tribe: ''},
            {_id: '', tribe: ''},
            {_id: '', tribe: ''}
        ], null);
        getReportStub.withArgs(bill, [amadeus, shorty]).returns(billsPairCandidates);
        const amadeusPairCandidates = new PairHistoryReport(amadeus, [
            {_id: '', tribe: ''}
        ], null);
        getReportStub.withArgs(amadeus, [bill, shorty]).returns(amadeusPairCandidates);
        const shortyPairCandidates = new PairHistoryReport(shorty, [
            {_id: '', tribe: ''}, {_id: '', tribe: ''}
        ], null);
        getReportStub.withArgs(shorty, [bill, amadeus]).returns(shortyPairCandidates);

        const sequencer = new Sequencer(pairingHistory);
        const next = sequencer.getNextInSequence(players);
        expect(next).toEqual(amadeusPairCandidates);
    });
});