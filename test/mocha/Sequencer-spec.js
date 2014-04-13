var Sequencer = require('../../lib/Sequencer');
var PairHistoryReport = require('../../lib/PairHistoryReport');
require('should');
var sinon = require('sinon');

describe('Sequencer', function () {

    var bill = "Bill";
    var ted = "Ted";
    var amadeus = "Mozart";
    var shorty = "Napoleon";

    it('will use the Pairing History to produce a wheel spin sequence in order of longest time since paired to shortest', function () {
        var players = [ bill, ted, amadeus, shorty];

        var getReportStub = sinon.stub();
        var pairingHistory = {
            getPairCandidateReport: getReportStub
        };

        var billsPairCandidates = new PairHistoryReport(bill, [], 3);
        getReportStub.withArgs(bill, [ted, amadeus, shorty]).returns(billsPairCandidates);
        var tedsPairCandidates = new PairHistoryReport(ted, [], 7);
        getReportStub.withArgs(ted, [bill, amadeus, shorty]).returns(tedsPairCandidates);
        var amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        getReportStub.withArgs(amadeus, [bill, ted, shorty]).returns(amadeusPairCandidates);
        var shortyPairCandidates = new PairHistoryReport(shorty, [], 5);
        getReportStub.withArgs(shorty, [bill, ted, amadeus]).returns(shortyPairCandidates);

        var sequencer = new Sequencer(pairingHistory);

        var next = sequencer.getNextInSequence(players);

        next.should.eql(tedsPairCandidates);
    });

    it('will use the Pairing History to produce a wheel spin sequence in order of longest time since paired to shortest', function () {
        var players = [ bill, amadeus, shorty];

        var getReportStub = sinon.stub();
        var pairingHistory = {
            getPairCandidateReport: getReportStub
        };

        var billsPairCandidates = new PairHistoryReport(bill, [], 3);
        getReportStub.withArgs(bill, [ amadeus, shorty]).returns(billsPairCandidates);
        var amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        getReportStub.withArgs(amadeus, [bill, shorty]).returns(amadeusPairCandidates);
        var shortyPairCandidates = new PairHistoryReport(shorty, [], 5);
        getReportStub.withArgs(shorty, [bill, amadeus]).returns(shortyPairCandidates);

        var sequencer = new Sequencer(pairingHistory);
        var next = sequencer.getNextInSequence(players);
        next.should.eql(shortyPairCandidates);
    });

    it('will use the Pairing History to get the next in sequence for when a player has never paired.', function () {
        var players = [ bill, amadeus, shorty];

        var getReportStub = sinon.stub();
        var pairingHistory = {
            getPairCandidateReport: getReportStub
        };

        var billsPairCandidates = new PairHistoryReport(bill, [], 3);
        getReportStub.withArgs(bill, [ amadeus, shorty]).returns(billsPairCandidates);
        var amadeusPairCandidates = new PairHistoryReport(amadeus, [], 4);
        getReportStub.withArgs(amadeus, [bill, shorty]).returns(amadeusPairCandidates);
        var shortyPairCandidates = new PairHistoryReport(shorty, [], null);
        getReportStub.withArgs(shorty, [bill, amadeus]).returns(shortyPairCandidates);

        var sequencer = new Sequencer(pairingHistory);
        var next = sequencer.getNextInSequence(players);
        next.should.eql(shortyPairCandidates);
    });

    it('will prioritize the report with fewest players when equal amounts of time.', function () {
        var players = [ bill, amadeus, shorty];

        var getReportStub = sinon.stub();
        var pairingHistory = {
            getPairCandidateReport: getReportStub
        };

        var billsPairCandidates = new PairHistoryReport(bill, [
            {},
            {},
            {}
        ], null);
        getReportStub.withArgs(bill, [ amadeus, shorty]).returns(billsPairCandidates);
        var amadeusPairCandidates = new PairHistoryReport(amadeus, [
            {}
        ], null);
        getReportStub.withArgs(amadeus, [bill, shorty]).returns(amadeusPairCandidates);
        var shortyPairCandidates = new PairHistoryReport(shorty, [
            {},
            {}
        ], null);
        getReportStub.withArgs(shorty, [bill, amadeus]).returns(shortyPairCandidates);

        var sequencer = new Sequencer(pairingHistory);
        var next = sequencer.getNextInSequence(players);
        next.should.eql(amadeusPairCandidates);
    });
});