var CouplingGame = require('../../../server/lib/CouplingGame');
var PairHistoryReport = require('../../../server/lib/PairHistoryReport').default;

describe("Coupling Game", function () {
  function badSpin(players) {
    return players[0];
  }

  it("with no players should return no pairs", function () {
    var game = new CouplingGame(badSpin);
    var players = [];

    var results = game.play(players);
    expect(results).toEqual([]);
  });

  describe("with two players", function () {
    var player1 = {name: 'bill'};
    var player2 = {name: 'ted'};

    var allPlayers = [player1, player2];
    var spinFunction;
    var nextInSequenceFunction;
    var game;

    beforeEach(function () {
        spinFunction = jasmine.createSpy('spin');
        nextInSequenceFunction = jasmine.createSpy('next');
        game = new CouplingGame({getNextInSequence: nextInSequenceFunction}, {spin: spinFunction});
        nextInSequenceFunction.and.returnValue(new PairHistoryReport(player2, [player1]));
        spinFunction.and.returnValue(player1);
      }
    );


    it("should remove a player from the wheel before each play", function () {
      game.play(allPlayers);

      expect(nextInSequenceFunction).toHaveBeenCalledWith(allPlayers);
      expect(spinFunction).toHaveBeenCalledWith([player1]);
    });

    it("should make one pair in order determined by the wheel", function () {
      var results = game.play(allPlayers);
      expect(results).toEqual([
        [player2, player1]
      ]);
    });
  });

  describe("with three players in singled-out mode", function () {

    var player1 = {name: 'bill'};
    var player2 = {name: 'ted'};
    var player3 = {name: 'mozart'};
    var allPlayers = [player1, player2, player3];
    var spinFunction;
    var nextInSequenceFunction;
    var game;

    beforeEach(function () {
        spinFunction = jasmine.createSpy('spin');
        spinFunction.and.returnValue(player1);
        nextInSequenceFunction = jasmine.createSpy('next');
        nextInSequenceFunction.and.returnValues(
          new PairHistoryReport(player3, [player1, player2]),
          new PairHistoryReport(player2, [])
        );

        game = new CouplingGame({getNextInSequence: nextInSequenceFunction}, {spin: spinFunction});
      }
    );

    it("should remove a player from the wheel before each play", function () {
      game.play(allPlayers);

      expect(nextInSequenceFunction.calls.argsFor(0)).toEqual([allPlayers]);
      expect(spinFunction.calls.argsFor(0)).toEqual([
        [player1, player2]
      ]);
      expect(nextInSequenceFunction.calls.argsFor(1)).toEqual([
        [player2]
      ]);
    });

    it("should make two pairs in order determined by the wheel", function () {
      var results = game.play(allPlayers);
      expect(results).toEqual([
        [player3, player1],
        [player2]
      ]);
    });
  });

  it("should one pair two players", function () {
    var nextInSequenceFunction = jasmine.createSpy('next');
    var game = new CouplingGame({getNextInSequence: nextInSequenceFunction}, {spin: badSpin});

    var player1 = {name: 'bill'};
    var player2 = {name: 'ted'};
    var allPlayers = [player1, player2];

    nextInSequenceFunction.and.returnValue(new PairHistoryReport(player1, [player2]));

    var results = game.play(allPlayers);

    expect(results).toEqual([
      [player1, player2]
    ]);
  });
});
