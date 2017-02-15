import Sequencer from "../../../server/lib/Sequencer";
import PairHistoryReport from "../../../server/lib/PairHistoryReport";

describe('Sequencer', function () {

    const bill = {_id: "Bill", tribe: ''};
    const ted = {_id: "Ted", tribe: ''};
    const amadeus = {_id: "Mozart", tribe: ''};
    const shorty = {_id: "Napoleon", tribe: ''};

    it('will use the Pairing History to produce a wheel spin sequence in order of longest time since paired to shortest', function () {
        const players = [bill, ted, amadeus, shorty];

        const billsPairCandidates = new PairHistoryReport(bill, [], 3);
        const tedsPairCandidates = new PairHistoryReport(ted, [], 7);
        const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        const shortyPairCandidates = new PairHistoryReport(shorty, [], 5);

        const reportProvider = {
            getPairHistoryReports: function(players){return [billsPairCandidates,tedsPairCandidates,amadeusPairCandidates,shortyPairCandidates];}
        };

        const sequencer = new Sequencer(reportProvider);

        const next = sequencer.getNextInSequence(players);

        expect(next).toEqual(tedsPairCandidates);
    });

    it('a person who just paired has lower priority than someone who has not paired in a long time', function() {
        const players = [bill, ted, amadeus, shorty];

        const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 5);
        const shortyPairCandidates = new PairHistoryReport(shorty, [], 0);

        const reportProvider = {
            getPairHistoryReports: function(players){
                return [amadeusPairCandidates,shortyPairCandidates];
            }
        };

        const sequencer = new Sequencer(reportProvider);

        const next = sequencer.getNextInSequence(players);

        expect(next).toEqual(amadeusPairCandidates);
    });

    it('will use the Pairing History to produce a wheel spin sequence in order of longest time since paired to shortest', function () {
        const players = [bill, amadeus, shorty];

        const billsPairCandidates = new PairHistoryReport(bill, [], 3);
        const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        const shortyPairCandidates = new PairHistoryReport(shorty, [], 5);

        const reportProvider = {
            getPairHistoryReports: function(players){return [billsPairCandidates,amadeusPairCandidates,shortyPairCandidates];}
        };

        const sequencer = new Sequencer(reportProvider);
        const next = sequencer.getNextInSequence(players);
        expect(next).toEqual(shortyPairCandidates);
    });

    it('will use the Pairing History to get the next in sequence for when a player has never paired.', function () {
        const players = [bill, amadeus, shorty];

        const billsPairCandidates = new PairHistoryReport(bill, [], 3);
        const amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        const shortyPairCandidates = new PairHistoryReport(shorty, [], null);

        const reportProvider = {
            getPairHistoryReports: function(players){return [billsPairCandidates,amadeusPairCandidates,shortyPairCandidates];}
        };

        const sequencer = new Sequencer(reportProvider);
        const next = sequencer.getNextInSequence(players);
        expect(next).toEqual(shortyPairCandidates);
    });

    it('will prioritize the report with fewest players when equal amounts of time.', function () {
        const players = [bill, amadeus, shorty];

        const billsPairCandidates = new PairHistoryReport(bill, [
            {_id: '', tribe: ''},
            {_id: '', tribe: ''},
            {_id: '', tribe: ''}
        ], null);
        const amadeusPairCandidates = new PairHistoryReport(amadeus, [
            {_id: '', tribe: ''}
        ], null);
        const shortyPairCandidates = new PairHistoryReport(shorty, [
            {_id: '', tribe: ''}, {_id: '', tribe: ''}
        ], null);

        const reportProvider = {
            getPairHistoryReports: function(players){return [billsPairCandidates,amadeusPairCandidates,shortyPairCandidates];}
        };

        const sequencer = new Sequencer(reportProvider);
        const next = sequencer.getNextInSequence(players);
        expect(next).toEqual(amadeusPairCandidates);
    });
});
