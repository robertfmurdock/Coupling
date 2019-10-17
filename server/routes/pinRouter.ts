"use strict";
import * as express from 'express'
import {handleRequest} from "./route-helper";

class PinRoutes {

    list = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performPinsQuery(request.params.tribeId),
        (response, data) => response.send(data)
    );
    savePin = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performSavePinCommand(request.body, request.params.tribeId),
        (response, data) => response.send(data)
    );
    removePin = handleRequest(
        (commandDispatcher, request) => commandDispatcher.performDeletePinCommand(request.params.pinId),
        (response, data) => {
            if (data)
                response.send(data);
            else {
                response.statusCode = 404;
                response.send({message: 'Failed to remove the pin because it did not exist.'})
            }
        }
    );
}

const pins = new PinRoutes();
const router = express.Router({mergeParams: true});
router.route('/')
    .get(pins.list)
    .post(pins.savePin);
router.delete('/:pinId', pins.removePin);

export default router