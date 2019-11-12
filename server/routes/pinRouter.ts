"use strict";
import * as express from 'express'
import {handleRequest} from "./route-helper";

class PinRoutes {

    list = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performPinsQuery(request, response),
        () => {}
    );
    savePin = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performSavePinCommand(request, response),
        () => {}
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