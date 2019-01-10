import ReportProvider from "../../lib/ReportProvider";
import PairHistoryReport from "../../lib/PairCandidateReport";
import Badge from "../../../common/Badge";
import PairingRule from "../../../common/PairingRule";
// @ts-ignore
import {actionDispatcherMock} from "engine_test"

describe('ReportProvider', function () {

    const bill = {_id: "Bill", tribe: '', badge: Badge.Default};
    const ted = {_id: "Ted", tribe: '', badge: Badge.Default};
    const amadeus = {_id: "Mozart", tribe: '', badge: Badge.Default};
    const shorty = {_id: "Napoleon", tribe: '', badge: Badge.Default};
    const amadeusAlternate = {_id: "Mozart", tribe: '', badge: Badge.Alternate};
    const shortyAlternate = {_id: "Napoleon", tribe: '', badge: Badge.Alternate};

    describe('when the tribe prefers pairing with different badges', function () {
        it('will return all reports for the players with same badge', function () {
            const mock = actionDispatcherMock();

            const players = [bill, ted, amadeus, shorty];

            const pairingHistory = {historyDocuments: []};

            const reportProvider = new ReportProvider(pairingHistory, mock);

            const billsPairCandidates = new PairHistoryReport(bill, [], 1);
            mock.whenGiven(bill, [ted, amadeus, shorty], billsPairCandidates);
            const tedsPairCandidates = new PairHistoryReport(ted, [], 1);
            mock.whenGiven(ted, [bill, amadeus, shorty], tedsPairCandidates);
            const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 1);
            mock.whenGiven(amadeus, [bill, ted, shorty], amadeusPairCandidates);
            const shortyPairCandidates = new PairHistoryReport(shorty, [], 1);
            mock.whenGiven(shorty, [bill, ted, amadeus], shortyPairCandidates);

            const reports = reportProvider.getPairHistoryReports(players, PairingRule.PreferDifferentBadge);

            expect(reports).toEqual([billsPairCandidates, tedsPairCandidates, amadeusPairCandidates, shortyPairCandidates]);
        });

        it('will return filter candidates by unlike badge', function () {
            const mock = actionDispatcherMock();
            const players = [bill, ted, amadeusAlternate, shortyAlternate];

            const pairingHistory = {historyDocuments: []};

            const billsPairCandidates = new PairHistoryReport(bill, [], 1);
            mock.whenGiven(bill, [amadeusAlternate, shortyAlternate], billsPairCandidates);
            const tedsPairCandidates = new PairHistoryReport(ted, [], 1);
            mock.whenGiven(ted, [amadeusAlternate, shortyAlternate], tedsPairCandidates);
            const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 1);
            mock.whenGiven(amadeusAlternate, [bill, ted], amadeusPairCandidates);
            const shortyPairCandidates = new PairHistoryReport(shorty, [], 1);
            mock.whenGiven(shortyAlternate, [bill, ted], shortyPairCandidates);

            const reportProvider = new ReportProvider(pairingHistory, mock);

            const reports = reportProvider.getPairHistoryReports(players, PairingRule.PreferDifferentBadge);

            expect(reports).toEqual([billsPairCandidates, tedsPairCandidates, amadeusPairCandidates, shortyPairCandidates]);
        });

        it('will return a report for one player', function () {
            const players = [bill];

            const mock = actionDispatcherMock();
            const pairingHistory = {historyDocuments: []};

            const billsPairCandidates = new PairHistoryReport(bill, [], 1);
            mock.whenGiven(bill, [], billsPairCandidates);

            const reportProvider = new ReportProvider(pairingHistory, mock);

            const reports = reportProvider.getPairHistoryReports(players, PairingRule.PreferDifferentBadge);

            expect(reports).toEqual([billsPairCandidates]);
        });
    });

    describe('when the tribe prefers only longest time for determining pairs', function () {

        it('', function () {
            const players = [bill, ted, amadeusAlternate, shortyAlternate];
            const mock = actionDispatcherMock();
            const pairingHistory = {historyDocuments: []};

            const billsPairCandidates = new PairHistoryReport(bill, [], undefined);
            mock.whenGiven(bill, [ted, amadeusAlternate, shortyAlternate], billsPairCandidates);

            const tedsPairCandidates = new PairHistoryReport(ted, [], undefined);
            mock.whenGiven(ted, [bill, amadeusAlternate, shortyAlternate], tedsPairCandidates);

            const amadeusPairCandidates = new PairHistoryReport(amadeusAlternate, [], undefined);
            mock.whenGiven(amadeusAlternate, [bill, ted, shortyAlternate], amadeusPairCandidates);

            const shortysPairCandidates = new PairHistoryReport(shortyAlternate, [], undefined);
            mock.whenGiven(shortyAlternate, [bill, ted, amadeusAlternate], shortysPairCandidates);

            const reportProvider = new ReportProvider(pairingHistory, mock);
            const reports = reportProvider.getPairHistoryReports(players, PairingRule.LongestTime);
            expect(reports).toEqual([billsPairCandidates, tedsPairCandidates, amadeusPairCandidates, shortysPairCandidates]);
        });

    });
});
