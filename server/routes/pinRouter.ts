"use strict";
import * as express from 'express'
import {handleRequest} from "./route-helper";

class PinRoutes {

    list = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performPinsQuery(request, response),
        () => {
        }
    );
    savePin = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performSavePinCommand(request, response),
        () => {
        }
    );
    removePin = handleRequest(
        (commandDispatcher, request, response) => commandDispatcher.performDeletePinCommand(request, response),
        () => {
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