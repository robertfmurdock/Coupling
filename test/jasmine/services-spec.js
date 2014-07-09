"use strict";

describe('Service: ', function () {
    beforeEach(function () {
        module('coupling.services');
    });

    describe('Coupling', function () {

        var httpBackend;
        var Coupling;

        beforeEach(function () {
            inject(function (_Coupling_, $httpBackend) {
                httpBackend = $httpBackend;
                Coupling = _Coupling_;
            });
        });

        describe('get tribes', function () {
            it('calls back with tribes on success', function () {
                var expectedTribes = [
                    {_id: 'one'},
                    {_id: 'two'}
                ];

                httpBackend.whenGET('/api/tribes').respond(200, expectedTribes);

                var returnedTribes = null;
                Coupling.getTribes(function (resultTribes) {
                    returnedTribes = resultTribes;
                });

                httpBackend.flush();

                expect(returnedTribes).toEqual(expectedTribes);
            });

            it('shows error on failure', function () {
                var statusCode = 404;
                var url = '/api/tribes';
                var expectedData = 'nonsense';
                httpBackend.whenGET(url).respond(statusCode, expectedData);
                var callCount = 0;
                Coupling.getTribes(function () {
                    callCount++;
                });

                var alertSpy = spyOn(window, 'alert');
                httpBackend.flush();
                expect(callCount).toBe(0);
                expect(alertSpy).toHaveBeenCalledWith('There was a problem loading ' + url + ' ' + expectedData + ' Got status code: ' + statusCode);
            });
        });


    });
});