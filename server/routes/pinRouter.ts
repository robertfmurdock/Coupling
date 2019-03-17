"use strict";
import * as express from 'express'
import {handleRequest} from "./route-helper";

class PinRoutes {

    list = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performPinsQuery(request.params.tribeId),
        (response, data) => response.send(data)
    );

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

const pins = new PinRoutes();
const router = express.Router({mergeParams: true});
router.route('/')
    .get(pins.list)
    .post(pins.savePin);
router.delete('/:pinId', pins.removePin);

export default router