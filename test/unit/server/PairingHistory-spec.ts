"use strict";
import PairingHistory from "../../../server/lib/PairingHistory";
import Player from "../../../common/Player";

var PairAssignmentDocument = require('../../../server/lib/PairAssignmentDocument');
var ObjectID = require('mongodb').ObjectID;

describe('Pairing History', function () {
    it('should be retrievable', function () {
        var historyDocuments = [
            new PairAssignmentDocument(new Date(2014, 3, 30), []), [
                [
                    {name: 'bill'},
                    {name: 'Ted'}
                ]
            ]
        ];

        var pairingHistory = new PairingHistory(historyDocuments);

        expect(historyDocuments).toEqual(pairingHistory.historyDocuments);
    });

    it('should return empty array when no partners are available', function () {
        var historyDocuments = [];
        var pairingHistory = new PairingHistory(historyDocuments);
        var report = pairingHistory.getPairCandidateReport({_id: '', tribe: '', name: 'player'} as Player, []);
        expect(report.partnerCandidates).toEqual([]);
    });

    describe('should determine possible partners for a player by choosing a partner', function () {
        var bruce = {name: 'Batman', _id: ObjectID.createFromHexString('000000079bb31fb01ee7834c'), tribe: ''} as Player;

        var jezebel = {_id: 'Jezebel Jett', tribe: ''};
        var talia = {_id: 'Talia', tribe: ''};
        var selena = {_id: 'Catwoman', tribe: ''};
        var availableOtherPlayers = [
            selena, talia, jezebel
        ];

        describe('who has never paired', function () {
            it('with no history', function () {
                var historyDocuments = [];
                var pairingHistory = new PairingHistory(historyDocuments);
                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);
                expect(availableOtherPlayers).toEqual(report.partnerCandidates);
                expect(report.timeSinceLastPaired).toBe(undefined);

                expect(bruce).toEqual(report.player);
            });

            it('with history document that has no pairs', function () {
                var historyDocuments = [
                    {pairs: null}
                ];
                var pairingHistory = new PairingHistory(historyDocuments);
                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);
                expect(availableOtherPlayers).toEqual(report.partnerCandidates);
                expect(report.timeSinceLastPaired).toBe(undefined);

                expect(bruce).toEqual(report.player);
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
                expect(availableOtherPlayers).toEqual(report.partnerCandidates);
                expect(report.timeSinceLastPaired).toBe(undefined);
                expect(bruce).toEqual(report.player);
            });

            it('with only the person you were with last time', function () {
                var historyDocuments = [
                    new PairAssignmentDocument(new Date(2014, 3, 30), [
                        [bruce, selena]
                    ])
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, [selena]);
                expect([selena]).toEqual(report.partnerCandidates);
                expect(report.timeSinceLastPaired).toEqual(0);
                expect(bruce).toEqual(report.player);
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

                expect(report.partnerCandidates).toEqual([expectedPartner]);
                expect(report.timeSinceLastPaired).toEqual(2);
                expect(bruce).toEqual(report.player);
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
                            expectedPartner, {
                            name: 'Batman',
                            _id: ObjectID.createFromHexString('000000079bb31fb01ee7834c')
                        }
                        ]
                    ])
                ];
                var pairingHistory = new PairingHistory(historyDocuments);

                var report = pairingHistory.getPairCandidateReport(bruce, availableOtherPlayers);

                expect(report.partnerCandidates).toEqual([expectedPartner]);
                expect(report.timeSinceLastPaired).toEqual(2);
                expect(bruce).toEqual(report.player);
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

                expect(report.partnerCandidates).toEqual([talia, jezebel]);
                expect(report.timeSinceLastPaired).toBe(undefined);
                expect(bruce).toEqual(report.player);
            });

        });
    });


});
