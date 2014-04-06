var PairingHistory = require('../../lib/PairingHistory');
var should = require('should');

describe('Pairing History', function () {

    it('should be retrievable', function () {
        var historyDocuments = [
            makePairListDocument([
                {name: 'bill'},
                {name: 'Ted'}
            ], new Date(2014, 3, 30))
        ];

        var pairingHistory = new PairingHistory(historyDocuments);

        should(historyDocuments).eql(pairingHistory.historyDocuments);
    });

    function makePairListDocument(pairs, date) {
        return {pairs: pairs, date: date};
    }


    describe('should determine possible partners for a player by choosing a partner', function () {
        var bruce = {name: 'Batman'};

        var jezebel = {name: 'Jezebel Jett'};
        var talia = {name: 'Talia'};
        var selena = {name: 'Catwoman'};
        var availableOtherPlayers = [
            selena, talia, jezebel
        ];

        describe('who has never paired', function () {
            it('with no history', function () {
                var historyDocuments = [];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);
                availableOtherPlayers.should.eql(report.partnerCandidates);
                should.not.exist(availableOtherPlayers.timeSinceLastPaired);

                bruce.should.equal(report.player);
            });

            it('with plenty of history', function () {
                var historyDocuments = [
                    makePairListDocument([
                        bruce, {name: 'Batgirl'}
                    ], new Date(2014, 3, 30)), makePairListDocument([
                        bruce, {name: 'Robin'}
                    ], new Date(2014, 3, 29))
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);
                availableOtherPlayers.should.eql(report.partnerCandidates);
                should.not.exist(report.timeSinceLastPaired);
                bruce.should.equal(report.player);
            });
        });

        describe('who has not paired recently', function () {
            it('when there is clearly someone who has been the longest', function () {
                var expectedPartner = jezebel;
                var historyDocuments = [
                    makePairListDocument([
                        bruce, selena
                    ], new Date(2014, 3, 30)),
                    makePairListDocument([
                        bruce, talia
                    ], new Date(2014, 3, 29)),
                    makePairListDocument([
                        expectedPartner, bruce
                    ], new Date(2014, 1, 30))
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);

                report.partnerCandidates.should.eql([expectedPartner]);
                report.timeSinceLastPaired.should.eql(2);
                bruce.should.equal(report.player);
            });
        });
    });


});
