"use strict";
import * as express from 'express'

class PinRoutes {
    list(request, response) {
        request.dataService.requestPins(request.params.tribeId).then(function (pins) {
            response.send(pins);
        });
    };

    savePin(request, response) {
        var pin = request.body;
        request.dataService.savePin(pin, function () {
            response.send(pin);
        });
    };

    removePin(request, response) {
        request.dataService.removePin(request.params.pinId, function (error) {
            if (error) {
                response.statusCode = 404;
                response.send(error);
            } else {
                response.send({});
            }
        });
    };
}

var pins = new PinRoutes();
var router = express.Router({mergeParams: true});
router.route('/')
    .get(pins.list)
    .post(pins.savePin);
router.delete('/:pinId', pins.removePin);

export default router