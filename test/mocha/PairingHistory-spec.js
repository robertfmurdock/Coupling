"use strict";
var PairingHistory = require('../../server/lib/PairingHistory');
var PairAssignmentDocument = require('../../server/lib/PairAssignmentDocument');
var ObjectID = require('mongodb').ObjectID;
var expect = require('chai').expect;

describe('Pairing History', function () {
    it('should be retrievable', function () {
        var historyDocuments = [
            new PairAssignmentDocument(new Date(2014, 3, 30)), [
                [
                    {name: 'bill'},
                    {name: 'Ted'}
                ]
            ]
        ];

        var pairingHistory = new PairingHistory(historyDocuments);

        expect(historyDocuments).eql(pairingHistory.historyDocuments);
    });

    it('should return empty array when no partners are available', function () {
        var historyDocuments = [];
        var pairingHistory = new PairingHistory(historyDocuments);
        var report = pairingHistory.getPairCandidateReport({name: 'player'}, []);
        expect(report.partnerCandidates).eql([]);
    });

    describe('should determine possible partners for a player by choosing a partner', function () {
        var bruce = {name: 'Batman', _id: ObjectID.createFromHexString('000000079bb31fb01ee7834c')};

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
                expect(availableOtherPlayers).eql(report.partnerCandidates);
                expect(availableOtherPlayers.timeSinceLastPaired).to.not.exist;

                expect(bruce).to.equal(report.player);
            });

            it('with history document that has no pairs', function () {
                var historyDocuments = [
                    {pairs: null}
                ];
                var pairingHistory = new PairingHistory(historyDocuments);
                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);
                expect(availableOtherPlayers).to.eql(report.partnerCandidates);
                expect(availableOtherPlayers.timeSinceLastPaired).to.not.exist;

                expect(bruce).to.equal(report.player);
            });

            it('with plenty of history', function () {
                var historyDocuments = [
                    new PairAssignmentDocument(new Date(2014, 3, 30), [
                        [
                            bruce, {name: 'Batgirl'}
                        ]
                    ]), new PairAssignmentDocument(new Date(2014, 3, 29), [
                        [
                            bruce, {name: 'Robin'}
                        ]
                    ])
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);
                expect(availableOtherPlayers).to.eql(report.partnerCandidates);
                expect(report.timeSinceLastPaired).to.not.exist;
                expect(bruce).to.eql(report.player);
            });

            it('with only the person you were with last time', function () {
                var historyDocuments = [
                    new PairAssignmentDocument(new Date(2014, 3, 30), [
                        [bruce, selena]
                    ])
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, [selena]);
                expect([selena]).to.eql(report.partnerCandidates);
                expect(report.timeSinceLastPaired).to.equal(0);
                expect(bruce).to.equal(report.player);
            });
        });

        describe('who has not paired recently', function () {
            it('when there is clearly someone who has been the longest', function () {
                var expectedPartner = jezebel;
                var historyDocuments = [
                    new PairAssignmentDocument(new Date(2014, 3, 30), [
                        [
                            bruce, selena
                        ]
                    ]),
                    new PairAssignmentDocument(new Date(2014, 3, 29), [
                        [
                            bruce, talia
                        ]
                    ]),
                    new PairAssignmentDocument(new Date(2014, 1, 30), [
                        [
                            expectedPartner, bruce
                        ]
                    ])
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);

                expect(report.partnerCandidates).to.eql([expectedPartner]);
                expect(report.timeSinceLastPaired).to.eql(2);
                expect(bruce).to.equal(report.player);
            });

            it('when there is clearly someone who has been the longest and they are not the same object instances so you have to match with id', function () {
                var expectedPartner = jezebel;
                var historyDocuments = [
                    new PairAssignmentDocument(new Date(2014, 3, 30), [
                        [
                            {name: 'Batman', _id: ObjectID.createFromHexString('000000079bb31fb01ee7834c')},
                            selena
                        ]
                    ]),
                    new PairAssignmentDocument(new Date(2014, 3, 29), [
                        [
                            {name: 'Batman', _id: ObjectID.createFromHexString('000000079bb31fb01ee7834c')},
                            talia
                        ]
                    ]),
                    new PairAssignmentDocument(new Date(2014, 1, 30), [
                        [
                            expectedPartner, {name: 'Batman', _id: ObjectID.createFromHexString('000000079bb31fb01ee7834c')}
                        ]
                    ])
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);

                expect(report.partnerCandidates).to.eql([expectedPartner]);
                expect(report.timeSinceLastPaired).to.eql(2);
                expect(bruce).to.equal(report.player);
            });

            it('when there is one person who has paired but no one else', function () {
                var historyDocuments = [
                    new PairAssignmentDocument(new Date(2014, 3, 30), [
                        [
                            bruce, selena
                        ]
                    ])
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);

                expect(report.partnerCandidates).to.eql([talia, jezebel]);
                expect(report.timeSinceLastPaired).to.not.exist;
                expect(bruce).to.equal(report.player);
            });

        });
    });


});
