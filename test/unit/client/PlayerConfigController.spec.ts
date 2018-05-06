import {PlayerConfigController} from "../../../client/app/components/player-config/player-config";
import "angular-route";
import * as angular from 'angular'
import * as Promise from 'bluebird'
import Player from "../../../common/Player";
import Badge from "../../../common/Badge";
import * as _ from "underscore";

const defer = function () {
    const defer = {
        promise: null,
        resolve: null,
        reject: null
    };
    defer.promise = new Promise((resolve, reject) => {
        defer.resolve = resolve;
        defer.reject = reject;
    });
    return defer;
};

let initPlayerController = function (controller, $scope, Coupling, $location,
                                     $route: angular.route.IRouteService,
                                     tribe: {name: string; id: string; _id: string},
                                     alternatePlayer: Player) {
    controller = new PlayerConfigController($scope, Coupling, $location, $route);
    _.extend(controller, {tribe, player: alternatePlayer});
    controller.$onInit();
    return controller;
};

describe('PlayerConfigController', function () {

    const tribe = {
        name: 'Party tribe.',
        id: 'party',
        _id: 'hmm'
    };

    let player: Player;

    let Coupling, $location, $scope;

    beforeEach(angular.mock.module('coupling.playerConfig'));

    beforeEach(function () {
        $location = {
            path: jasmine.createSpy('path')
        };

        Coupling = {
            spin: jasmine.createSpy('spin'),
            savePlayer: jasmine.createSpy('save'),
            removePlayer: jasmine.createSpy('remove')
        };
        $scope = {};
        $scope.data = Coupling.data;
        $scope.$on = jasmine.createSpy('on');
        player = {_id: 'blarg', tribe: tribe.id};
    });

    it('when the given player has no badge, will use default badge', function () {
        const $route = {
            updateParams(){
            },
            reload(){
            }, routes: {}
        };
        controller = initPlayerController(controller, $scope, Coupling, $location, $route, tribe, player);
        expect(controller.player.badge).toBe(Badge.Default);
    });

    it('when the given player has alt badge, will not modify player', function () {
        const $route = {
            updateParams(){
            },
            reload(){
            }, routes: {}
        };
        const alternatePlayer: Player = {_id: '', badge: Badge.Alternate, tribe: tribe.id};
        controller = initPlayerController(controller, $scope, Coupling, $location, $route, tribe, alternatePlayer);
        expect(alternatePlayer.badge).toBe(Badge.Alternate);
    });

    let controller;
    it('can save player using Coupling service and then reloads', function (done) {
        const $route = {
            current: {params: {id: player._id}, locals: null},
            routes: '',
            reload: jasmine.createSpy('path')
        };

        inject(function ($q, $rootScope, _$controller_) {
            const saveDefer = defer();
            Coupling.savePlayer.and.returnValue(saveDefer.promise);
            controller = _$controller_('PlayerConfigController', {
                $scope: $scope,
                Coupling,
                $location: $location,
                $route
            }, {tribe, player});

            controller.player.name = 'nonsense';
            controller.savePlayer();
            expect(Coupling.savePlayer).toHaveBeenCalledWith(controller.player);
            saveDefer.resolve();


            saveDefer.promise
                .then(function () {
                    expect($route.reload).toHaveBeenCalled();
                    done();
                })
                .catch(function (err) {
                    done.fail(err);
                });
            $rootScope.$apply();
        });
    });

    it('remove player will remove and reroute to current pair assignments when confirmed',
        inject(function ($controller, $q, $rootScope, _$controller_) {
            const confirmSpy = spyOn(window, 'confirm');

            const deleteDefer = $q.defer();
            Coupling.removePlayer.and.returnValue(deleteDefer.promise);


            const $route = {
                current: {params: {id: player._id}, locals: null},
                routes: '',
                reload: jasmine.createSpy('path')
            };
            const controller = _$controller_('PlayerConfigController', {$scope, Coupling, $location, $route},
                {tribe, player});

            confirmSpy.and.returnValue(true);
            controller.removePlayer();
            expect(Coupling.removePlayer).toHaveBeenCalled();
            const argsFor = Coupling.removePlayer.calls.argsFor(0);
            expect(argsFor[0]).toBe(controller.player);

            expect($location.path).not.toHaveBeenCalledWith('/' + tribe.id + '/pairAssignments/current');
            deleteDefer.resolve();
            $rootScope.$apply();
            expect($location.path).toHaveBeenCalledWith('/' + tribe.id + '/pairAssignments/current');
        }));

    it('remove player will do nothing when not confirmed', inject(function (_$controller_) {
        const confirmSpy = spyOn(window, 'confirm');

        const $route = {
            current: {params: {id: player._id}, locals: null},
            routes: '',
            reload: jasmine.createSpy('path')
        };
        controller = _$controller_('PlayerConfigController', {$scope, Coupling, $location, $route},
            {tribe, player});


        confirmSpy.and.returnValue(false);
        controller.removePlayer();
        expect(Coupling.removePlayer).not.toHaveBeenCalled();
        expect($location.path).not.toHaveBeenCalledWith('/' + tribe.id + '/pairAssignments/current');
    }));

    describe('on location change', function () {
        let onLocationChange;

        const player = {_id: 'blarg'};
        let controller;

        beforeEach(inject(function (_$controller_) {
            const $route = {
                current: {params: {id: player._id}, locals: null},
                routes: '',
                reload: jasmine.createSpy('path')
            };
            controller = _$controller_('PlayerConfigController', {$scope, Coupling, $location, $route},
                {tribe, player});
            expect($scope.$on).toHaveBeenCalled();
            const args = $scope.$on.calls.argsFor(0);
            expect(args[0]).toBe('$locationChangeStart');
            onLocationChange = args[1];
        }));

        describe('it will prompt the user to save if the player has changed', function () {

            beforeEach(function () {
                this.confirmSpy = spyOn(window, 'confirm');

                $scope.playerForm = {
                    $dirty: true
                };
                $scope.original = {
                    name: 'O.G.'
                };
                $scope.player = {
                    name: "differentName"
                };
            });

            it('and if they confirm it will save', function () {
                this.confirmSpy.and.returnValue(true);
                onLocationChange();
                expect(Coupling.savePlayer).toHaveBeenCalledWith(controller.player);
            });

            it('and if they do not confirm it will not save', function () {
                this.confirmSpy.and.returnValue(false);
                onLocationChange();
                expect(Coupling.savePlayer).not.toHaveBeenCalledWith(controller.player);
            });

        });
        it('it will not prompt the user to save if the player is unchanged', function () {
            spyOn(window, 'confirm');
            $scope.playerForm = {
                $dirty: false
            };
            $scope.original = {
                name: 'O.G.'
            };
            $scope.player = {
                name: $scope.original.name
            };
            onLocationChange();
            expect(window.confirm).not.toHaveBeenCalled();
            expect(Coupling.savePlayer).not.toHaveBeenCalled();
        });
    });
});
