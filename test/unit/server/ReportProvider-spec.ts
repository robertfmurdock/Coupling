import ReportProvider from "../../../server/lib/ReportProvider";
import PairHistoryReport from "../../../server/lib/PairHistoryReport";
import Badge from "../../../common/Badge";
import * as sinon from "sinon";
import PairingRule from "../../../common/PairingRule";

describe('ReportProvider', function () {

    const bill = {_id: "Bill", tribe: '', badge: Badge.Default};
    const ted = {_id: "Ted", tribe: '', badge: Badge.Default};
    const amadeus = {_id: "Mozart", tribe: '', badge: Badge.Default};
    const shorty = {_id: "Napoleon", tribe: '', badge: Badge.Default};
    const amadeusAlternate = {_id: "Mozart", tribe: '', badge: Badge.Alternate};
    const shortyAlternate = {_id: "Napoleon", tribe: '', badge: Badge.Alternate};

    describe('when the tribe prefers pairing with different badges', function() {
        it('will return all reports for the players with same badge', function () {
            const players = [bill, ted, amadeus, shorty];

            const getReportStub = sinon.stub();
            const pairingHistory = {
                getPairCandidateReport: getReportStub
            };

            const billsPairCandidates = new PairHistoryReport(bill, [], 1);
            getReportStub.withArgs(bill, [ted, amadeus, shorty]).returns(billsPairCandidates);
            const tedsPairCandidates = new PairHistoryReport(ted, [], 1);
            getReportStub.withArgs(ted, [bill, amadeus, shorty]).returns(tedsPairCandidates);
            const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 1);
            getReportStub.withArgs(amadeus, [bill, ted, shorty]).returns(amadeusPairCandidates);
            const shortyPairCandidates = new PairHistoryReport(shorty, [], 1);
            getReportStub.withArgs(shorty, [bill, ted, amadeus]).returns(shortyPairCandidates);

            const reportProvider = new ReportProvider(pairingHistory);

            const reports = reportProvider.getPairHistoryReports(players, PairingRule.PreferDifferentBadge);

            expect(reports).toEqual([billsPairCandidates,tedsPairCandidates,amadeusPairCandidates,shortyPairCandidates]);
        });

        it('will return filter candidates by unlike badge', function () {
            const players = [bill, ted, amadeusAlternate, shortyAlternate];

            const getReportStub = sinon.stub();
            const pairingHistory = {
                getPairCandidateReport: getReportStub
            };

            const billsPairCandidates = new PairHistoryReport(bill, [], 1);
            getReportStub.withArgs(bill, [amadeusAlternate, shortyAlternate]).returns(billsPairCandidates);
            const tedsPairCandidates = new PairHistoryReport(ted, [], 1);
            getReportStub.withArgs(ted, [amadeusAlternate, shortyAlternate]).returns(tedsPairCandidates);
            const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 1);
            getReportStub.withArgs(amadeusAlternate, [bill, ted]).returns(amadeusPairCandidates);
            const shortyPairCandidates = new PairHistoryReport(shorty, [], 1);
            getReportStub.withArgs(shortyAlternate, [bill, ted]).returns(shortyPairCandidates);

            const reportProvider = new ReportProvider(pairingHistory);

            const reports = reportProvider.getPairHistoryReports(players, PairingRule.PreferDifferentBadge);

            expect(reports).toEqual([billsPairCandidates,tedsPairCandidates,amadeusPairCandidates,shortyPairCandidates]);
        });
    });

    describe('when the tribe prefers only longest time for determining pairs', function() {

        it('', function() {
            const players = [bill, ted, amadeusAlternate, shortyAlternate];
            const getReportStub = sinon.stub();
            const pairingHistory = {
                getPairCandidateReport: getReportStub
            };

            const billsPairCandidates = new PairHistoryReport(bill, [], undefined);
            getReportStub.withArgs(bill, [ted, amadeusAlternate, shortyAlternate]).returns(billsPairCandidates);

            const tedsPairCandidates = new PairHistoryReport(ted, [], undefined);
            getReportStub.withArgs(ted, [bill, amadeusAlternate, shortyAlternate]).returns(tedsPairCandidates);

            const amadeusPairCandidates = new PairHistoryReport(amadeusAlternate, [], undefined);
            getReportStub.withArgs(amadeusAlternate, [bill, ted, shortyAlternate]).returns(amadeusPairCandidates);

            const shortysPairCandidates = new PairHistoryReport(shortyAlternate, [], undefined);
            getReportStub.withArgs(shortyAlternate, [bill, ted, amadeusAlternate]).returns(shortysPairCandidates);

            const reportProvider = new ReportProvider(pairingHistory);
            const reports = reportProvider.getPairHistoryReports(players, PairingRule.LongestTime);
            expect(reports).toEqual([billsPairCandidates, tedsPairCandidates, amadeusPairCandidates, shortysPairCandidates]);
        });

    });
});
